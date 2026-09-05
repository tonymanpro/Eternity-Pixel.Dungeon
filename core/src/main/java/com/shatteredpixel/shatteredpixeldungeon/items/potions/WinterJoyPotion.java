/*
 * Eternity Pixel Dungeon
 * Winter Joy Potion (Festive Event Potion)
 */

package com.shatteredpixel.shatteredpixeldungeon.items.potions;

import com.shatteredpixel.shatteredpixeldungeon.HolidayEventConfig;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class WinterJoyPotion extends Potion {

	{
		image = ItemSpriteSheet.SPARKLING_POTION;
	}

	@Override
	public void apply(Hero hero) {
		super.apply(hero);

		int heal = Math.round(hero.HT * HolidayEventConfig.get().potionHealRatio);
		hero.HP = Math.min(hero.HT, hero.HP + heal);

		Burning.detach(hero, Burning.class);
		Frost.detach(hero, Frost.class);

		GLog.p(Messages.get(this, "joy"));
		CellEmitter.get(hero.pos).burst(Speck.factory(Speck.WOOL), 12);
	}
}
