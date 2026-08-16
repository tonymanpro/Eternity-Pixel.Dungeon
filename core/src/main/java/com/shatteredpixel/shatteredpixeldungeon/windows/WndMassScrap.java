/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Eternity Pixel Dungeon
 * Copyright (C) 2024-2026
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class WndMassScrap extends Window {

	private static final int WIDTH = 135;
	private static final int BTN_HEIGHT = 16;
	private static final int GAP = 3;

	private enum FilterMode {
		COMMON_EQUIP,
		BELOW_UNCOMMON,
		BELOW_RARE
	}

	private FilterMode currentMode = FilterMode.COMMON_EQUIP;
	private ArrayList<Item> matchedItems = new ArrayList<>();
	private long totalGold = 0;

	private RenderedTextBlock infoBlock;
	private RedButton btnAction;

	public WndMassScrap() {
		super();

		RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
		title.hardlight(TITLE_COLOR);
		title.setPos((WIDTH - title.width()) / 2, 2);
		add(title);

		RenderedTextBlock desc = PixelScene.renderTextBlock(Messages.get(this, "desc"), 6);
		desc.maxWidth(WIDTH);
		desc.setPos(0, title.bottom() + GAP);
		add(desc);

		float pos = desc.bottom() + GAP + 2;

		RedButton btnModeCommon = new RedButton(Messages.get(this, "mode_common_equip")) {
			@Override
			public void onClick() {
				currentMode = FilterMode.COMMON_EQUIP;
				updateMatches();
			}
		};
		btnModeCommon.setRect(0, pos, WIDTH, BTN_HEIGHT);
		add(btnModeCommon);
		pos = btnModeCommon.bottom() + GAP;

		RedButton btnModeUncommon = new RedButton(Messages.get(this, "mode_below_uncommon")) {
			@Override
			public void onClick() {
				currentMode = FilterMode.BELOW_UNCOMMON;
				updateMatches();
			}
		};
		btnModeUncommon.setRect(0, pos, WIDTH, BTN_HEIGHT);
		add(btnModeUncommon);
		pos = btnModeUncommon.bottom() + GAP;

		RedButton btnModeRare = new RedButton(Messages.get(this, "mode_below_rare")) {
			@Override
			public void onClick() {
				currentMode = FilterMode.BELOW_RARE;
				updateMatches();
			}
		};
		btnModeRare.setRect(0, pos, WIDTH, BTN_HEIGHT);
		add(btnModeRare);
		pos = btnModeRare.bottom() + GAP + 2;

		infoBlock = PixelScene.renderTextBlock("", 6);
		infoBlock.maxWidth(WIDTH);
		infoBlock.setPos(0, pos);
		add(infoBlock);
		pos += 18;

		btnAction = new RedButton("") {
			@Override
			public void onClick() {
				executeScrap();
			}
		};
		btnAction.setRect(0, pos, WIDTH, BTN_HEIGHT + 2);
		add(btnAction);
		pos = btnAction.bottom() + GAP;

		RedButton btnClose = new RedButton(Messages.get(this, "cancel")) {
			@Override
			public void onClick() {
				hide();
			}
		};
		btnClose.setRect(0, pos, WIDTH, BTN_HEIGHT);
		add(btnClose);
		pos = btnClose.bottom() + 2;

		updateMatches();
		resize(WIDTH, (int) pos);
	}

	private void updateMatches() {
		matchedItems.clear();
		totalGold = 0;

		Hero hero = Dungeon.hero;
		if (hero != null && hero.belongings != null && hero.belongings.backpack != null) {
			scanBag(hero.belongings.backpack, hero);
		}

		String infoStr = Messages.get(this, "items_found", matchedItems.size(), totalGold);
		infoBlock.text(infoStr);

		if (matchedItems.isEmpty()) {
			btnAction.text(Messages.get(this, "no_items"));
			btnAction.enable(false);
		} else {
			btnAction.text(Messages.get(this, "scrap_btn", totalGold));
			btnAction.enable(true);
		}
	}

	private void scanBag(Bag container, Hero hero) {
		for (Item item : new ArrayList<>(container.items)) {
			if (item instanceof Bag) {
				scanBag((Bag) item, hero);
				continue;
			}

			// Protection rules: never scrap pinned, unique, or currently equipped items
			if (item.pinned || item.unique || item.isEquipped(hero)) {
				continue;
			}

			boolean matches = false;
			switch (currentMode) {
				case COMMON_EQUIP:
					if ((item instanceof Weapon || item instanceof Armor) && item.level() <= 0) {
						if (item.rarity == Item.Rarity.NONE || item.rarity == Item.Rarity.COMMON) {
							matches = true;
						}
					}
					break;

				case BELOW_UNCOMMON:
					if (item.rarity == Item.Rarity.NONE || item.rarity == Item.Rarity.COMMON) {
						matches = true;
					}
					break;

				case BELOW_RARE:
					if (item.rarity == Item.Rarity.NONE || item.rarity == Item.Rarity.COMMON || item.rarity == Item.Rarity.UNCOMMON) {
						matches = true;
					}
					break;
			}

			if (matches) {
				matchedItems.add(item);
				totalGold += Math.max(1, item.value());
			}
		}
	}

	private void executeScrap() {
		if (matchedItems.isEmpty()) return;

		Hero hero = Dungeon.hero;
		if (hero == null || hero.belongings == null || hero.belongings.backpack == null) return;

		int count = 0;
		for (Item item : matchedItems) {
			item.detachAll(hero.belongings.backpack);
			count++;
		}

		if (totalGold > 0) {
			Gold gold = new Gold(totalGold);
			gold.collect(hero.belongings.backpack);
		}

		Sample.INSTANCE.play(Assets.Sounds.GOLD);
		CellEmitter.get(hero.pos).burst(Speck.factory(Speck.COIN), 10);
		hero.sprite.showStatus(0xFFFF00, "+" + totalGold + " G");

		hide();

		// Refresh bag window if currently visible
		if (WndBag.INSTANCE != null) {
			WndBag.INSTANCE.hide();
			GameScene.show(new WndBag(hero.belongings.backpack));
		}
	}
}
