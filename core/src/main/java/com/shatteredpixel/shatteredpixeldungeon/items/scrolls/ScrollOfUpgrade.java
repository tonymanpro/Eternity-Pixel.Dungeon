/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Experienced Pixel Dungeon
 * Copyright (C) 2019-2024 Trashbox Bobylev
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

package com.shatteredpixel.shatteredpixeldungeon.items.scrolls;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Degrade;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.UnstableSpellbook;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUpgrade;

import java.util.ArrayList;

public class ScrollOfUpgrade extends InventoryScroll {

    public static final String AC_UPGRADE = "UPGRADE";
	public static final String AC_UPGRADE_AMOUNT = "UPGRADE_AMOUNT";

	{
		icon = ItemSpriteSheet.Icons.SCROLL_UPGRADE;
		preferredBag = Belongings.Backpack.class;

		unique = true;

		talentFactor = 2f;
	}

	@Override
	protected boolean usableOnItem(Item item) {
		return item != null && item.isUpgradable();
	}

	private long customAmount = 0;

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (action.equals(AC_UPGRADE) || action.equals(AC_UPGRADE_AMOUNT)){
			if (hero.buff(MagicImmune.class) != null){
				GLog.w( Messages.get(this, "no_magic") );
			} else if (hero.buff( Blindness.class ) != null) {
				GLog.w( Messages.get(this, "blinded") );
			} else if (hero.buff(UnstableSpellbook.bookRecharge.class) != null
					&& hero.buff(UnstableSpellbook.bookRecharge.class).isCursed()){
				GLog.n( Messages.get(this, "cursed") );
			} else {
				curUser = hero;
				curItem = this;
				if (action.equals(AC_UPGRADE)) {
					customAmount = quantity();
					GameScene.selectItem(itemSelector2);
				} else {
					GameScene.show(new WndTextInput("Enter amount of upgrades to be used:", null, "", 15, false,
							"Accept", "Cancel") {
						@Override public void onSelect(boolean positive, String text) {
							if(!positive) return;
							long number;
							try {
								number = Long.parseLong(text);
							} catch (NumberFormatException e){
								GLog.w("No valid number was entered.");
								return;
							}
							if (number > 0){
								curUser = Dungeon.hero;
								curItem = ScrollOfUpgrade.this;
								customAmount = Math.min(number, quantity());
								GameScene.selectItem(itemSelector2);
							}
						}
					});
				}
			}
		}
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions( hero );
		if (isIdentified() && !anonymous) {
			actions.add(AC_UPGRADE);
			if (quantity() > 1)
				actions.add(AC_UPGRADE_AMOUNT);
		}
		return actions;
	}

	@Override
	protected void onItemSelected( Item item ) {
		if (curUser == null) curUser = Dungeon.hero;
		curItem = this;
		GameScene.show(new WndUpgrade(this, item, identifiedByUse));

	}

	public void reShowSelector(boolean force, boolean multiUpgrade){
		identifiedByUse = force;
		if (curUser == null) curUser = Dungeon.hero;
		curItem = this;
		GameScene.selectItem(multiUpgrade ? itemSelector2 : itemSelector);
	}

	public Item upgradeItem(Item item, long amount) {
		Hero user = curUser != null ? curUser : Dungeon.hero;
		if (user != null) {
			Degrade.detach( user, Degrade.class );
		}

		//logic for telling the user when item properties change from upgrades
		//...yes this is rather messy
		if (item instanceof Weapon){
			Weapon w = (Weapon) item;
			boolean wasCursed = w.cursed;
			boolean wasHardened = w.enchantHardened;
			boolean hadCursedEnchant = w.hasCurseEnchant();
			boolean hadGoodEnchant = w.hasGoodEnchant();

			item = w.upgrade(amount);

			if (user != null) {
				if (w.cursedKnown && hadCursedEnchant && !w.hasCurseEnchant()){
					removeCurse( user );
				} else if (w.cursedKnown && wasCursed && !w.cursed){
					weakenCurse( user );
				}
			}
			if (wasHardened && !w.enchantHardened){
				GLog.w( Messages.get(Weapon.class, "hardening_gone") );
			} else if (hadGoodEnchant && !w.hasGoodEnchant()){
				GLog.w( Messages.get(Weapon.class, "incompatible") );
			}

		} else if (item instanceof Armor){
			Armor a = (Armor) item;
			boolean wasCursed = a.cursed;
			boolean wasHardened = a.glyphHardened;
			boolean hadCursedGlyph = a.hasCurseGlyph();
			boolean hadGoodGlyph = a.hasGoodGlyph();

			item = a.upgrade(amount);

			if (user != null) {
				if (a.cursedKnown && hadCursedGlyph && !a.hasCurseGlyph()){
					removeCurse( user );
				} else if (a.cursedKnown && wasCursed && !a.cursed){
					weakenCurse( user );
				}
			}
			if (wasHardened && !a.glyphHardened){
				GLog.w( Messages.get(Armor.class, "hardening_gone") );
			} else if (hadGoodGlyph && !a.hasGoodGlyph()){
				GLog.w( Messages.get(Armor.class, "incompatible") );
			}

		} else if (item instanceof Wand || item instanceof Ring) {
			boolean wasCursed = item.cursed;

			item = item.upgrade(amount);

			if (user != null && item.cursedKnown && wasCursed && !item.cursed){
				removeCurse( user );
			}

		} else {
			item = item.upgrade(amount);
		}

		Badges.validateItemLevelAquired(item);
		Statistics.upgradesUsed += amount;
		Badges.validateMageUnlock();

		Catalog.countUse(item.getClass());
		Catalog.countUses(ScrollOfUpgrade.class, amount);

		return item;
	}

	public static void upgrade( Hero hero ) {
		if (hero != null && hero.sprite != null && hero.sprite.emitter() != null) {
			hero.sprite.emitter().start( Speck.factory( Speck.UP ), 0.2f, 3 );
		}
	}

	public static void weakenCurse( Hero hero ){
		GLog.p( Messages.get(ScrollOfUpgrade.class, "weaken_curse") );
		if (hero != null && hero.sprite != null && hero.sprite.emitter() != null) {
			hero.sprite.emitter().start( ShadowParticle.UP, 0.05f, 5 );
		}
	}

	public static void removeCurse( Hero hero ){
		GLog.p( Messages.get(ScrollOfUpgrade.class, "remove_curse") );
		if (hero != null && hero.sprite != null && hero.sprite.emitter() != null) {
			hero.sprite.emitter().start( ShadowParticle.UP, 0.05f, 10 );
		}
	}
	
	@Override
	public long value() {
		return isKnown() ? 50 * quantity : super.value();
	}

	@Override
	public long energyVal() {
		return isKnown() ? 10 * quantity : super.energyVal();
	}

	public WndBag.ItemSelector itemSelector2 = new WndBag.ItemSelector() {

		@Override
		public String textPrompt() {
			return inventoryTitle();
		}

		@Override
		public Class<? extends Bag> preferredBag() {
			return preferredBag;
		}

		@Override
		public boolean itemSelectable(Item item) {
			return item != null && usableOnItem(item);
		}

		@Override
		public void onSelect( Item item ) {
			if (curUser == null) curUser = Dungeon.hero;
			if (curItem == null) curItem = ScrollOfUpgrade.this;

			if (item != null) {
				long amt = (customAmount > 0) ? customAmount : curItem.quantity();
				GameScene.show(new WndUpgrade(curItem, item, identifiedByUse, amt));

			} else if (identifiedByUse && !((Scroll)curItem).anonymous) {

				((InventoryScroll)curItem).confirmCancelation();

			}
		}
	};
}
