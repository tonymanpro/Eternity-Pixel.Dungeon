/*
 * Eternity Pixel Dungeon
 * Candy Cane Item (Winter Event Festive Food)
 */

package com.shatteredpixel.shatteredpixeldungeon.items.food;

import com.shatteredpixel.shatteredpixeldungeon.HolidayEventConfig;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class CandyCane extends Food {

	{
		image = ItemSpriteSheet.PASTY;
		energy = 100;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (action.equals(AC_EAT)) {
			int heal = HolidayEventConfig.get().candyCaneHeal;
			hero.HP = Math.min(hero.HT, hero.HP + heal);
			Buff.prolong(hero, Haste.class, 10f);

			GLog.p(Messages.get(this, "taste"));
			CellEmitter.get(hero.pos).burst(Speck.factory(Speck.WOOL), 6);
		}
	}
}
