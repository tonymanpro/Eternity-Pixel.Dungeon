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

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

public class WndLootFilter extends Window {

	private static final int WIDTH = 130;
	private static final int BTN_HEIGHT = 16;
	private static final int GAP = 3;

	private static final Item.Rarity[] RARITY_STEPS = {
			Item.Rarity.NONE,
			Item.Rarity.COMMON,
			Item.Rarity.UNCOMMON,
			Item.Rarity.RARE,
			Item.Rarity.EPIC,
			Item.Rarity.LEGENDARY,
			Item.Rarity.MYTHICAL
	};

	public WndLootFilter() {
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

		CheckBox cbIgnoreCommon = new CheckBox(Messages.get(this, "ignore_common")) {
			@Override
			public void onClick() {
				super.onClick();
				SPDSettings.lootFilterIgnoreCommon(checked());
			}
		};
		cbIgnoreCommon.checked(SPDSettings.lootFilterIgnoreCommon());
		cbIgnoreCommon.setRect(0, pos, WIDTH, BTN_HEIGHT);
		add(cbIgnoreCommon);
		pos = cbIgnoreCommon.bottom() + GAP;

		RedButton btnMinRarity = new RedButton(getRarityLabel()) {
			@Override
			public void onClick() {
				int cur = SPDSettings.lootFilterMinRarity();
				int next = (cur + 1) % RARITY_STEPS.length;
				SPDSettings.lootFilterMinRarity(next);
				text(getRarityLabel());
			}
		};
		btnMinRarity.setRect(0, pos, WIDTH, BTN_HEIGHT);
		add(btnMinRarity);
		pos = btnMinRarity.bottom() + GAP;

		CheckBox cbAutoScrap = new CheckBox(Messages.get(this, "auto_scrap")) {
			@Override
			public void onClick() {
				super.onClick();
				SPDSettings.lootFilterAutoScrap(checked());
			}
		};
		cbAutoScrap.checked(SPDSettings.lootFilterAutoScrap());
		cbAutoScrap.setRect(0, pos, WIDTH, BTN_HEIGHT);
		add(cbAutoScrap);
		pos = cbAutoScrap.bottom() + GAP + 2;

		RedButton btnMassScrap = new RedButton(Messages.get(this, "open_mass_scrap")) {
			@Override
			public void onClick() {
				hide();
				GameScene.show(new WndMassScrap());
			}
		};
		btnMassScrap.setRect(0, pos, WIDTH, BTN_HEIGHT);
		add(btnMassScrap);
		pos = btnMassScrap.bottom() + GAP + 2;

		RedButton btnClose = new RedButton(Messages.get(this, "close")) {
			@Override
			public void onClick() {
				hide();
			}
		};
		btnClose.setRect(0, pos, WIDTH, BTN_HEIGHT);
		add(btnClose);
		pos = btnClose.bottom() + 2;

		resize(WIDTH, (int) pos);
	}

	private String getRarityLabel() {
		int idx = SPDSettings.lootFilterMinRarity();
		if (idx < 0 || idx >= RARITY_STEPS.length) idx = 0;
		Item.Rarity r = RARITY_STEPS[idx];
		String rName = (r == Item.Rarity.NONE) ? Messages.get(this, "rarity_all") : Messages.titleCase(r.name);
		return Messages.get(this, "min_rarity", rName);
	}
}
