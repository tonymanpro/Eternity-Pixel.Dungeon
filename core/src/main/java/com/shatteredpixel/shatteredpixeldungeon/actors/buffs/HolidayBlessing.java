/*
 * Eternity Pixel Dungeon
 * Holiday Blessing (Christmas Event Permanent/Prolonged Boon)
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class HolidayBlessing extends Buff {

	{
		type = buffType.POSITIVE;
		immunities.add(Frost.class);
		immunities.add(Chill.class);
	}

	private int turnCount = 0;

	@Override
	public boolean attachTo(Char target) {
		if (super.attachTo(target)) {
			Buff.detach(target, Frost.class);
			Buff.detach(target, Chill.class);
			if (target.sprite != null) {
				target.sprite.emitter().burst(Speck.factory(Speck.STAR), 12);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean act() {
		if (target != null && target.isAlive()) {
			turnCount++;

			// Regenerate 1 HP every 8 turns
			if (turnCount % 8 == 0 && target.HP < target.HT) {
				target.HP = Math.min(target.HT, target.HP + 1);
				if (target.sprite != null && Random.Int(2) == 0) {
					target.sprite.emitter().burst(Speck.factory(Speck.WOOL), 2);
				}
			}

			// Subtle snowflake/star ambient effect
			if (target.sprite != null && turnCount % 12 == 0) {
				CellEmitter.get(target.pos).burst(Speck.factory(Speck.STAR), 2);
			}

			spend(TICK);
		} else {
			detach();
		}
		return true;
	}


	@Override
	public int icon() {
		return BuffIndicator.BLESS;
	}

	@Override
	public void tintIcon(Image icon) {
		// Festive emerald/gold glow
		icon.hardlight(0.2f, 1f, 0.4f);
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc");
	}

	private static final String TURNS = "turns";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(TURNS, turnCount);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		turnCount = bundle.getInt(TURNS);
	}
}
