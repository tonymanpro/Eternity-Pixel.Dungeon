/*
 * Eternity Pixel Dungeon
 * Holiday Imp (Festive Mini-Boss)
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HolidayBlessing;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.food.CandyCane;
import com.shatteredpixel.shatteredpixeldungeon.items.holiday.HolidayGift;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.WinterJoyPotion;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HolidayImpSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

public class HolidayImp extends Mob {

	{
		spriteClass = HolidayImpSprite.class;

		HP = HT = Dungeon.getCycleMultiplier(70);
		defenseSkill = Dungeon.getCycleMultiplier(16);
		baseSpeed = 1.2f;

		EXP = Dungeon.getCycleMultiplier(20);
		maxLvl = 20;

		flying = true;

		properties.add(Property.MINIBOSS);
		properties.add(Property.DEMONIC);
	}

	@Override
	public long damageRoll() {
		return Dungeon.getCycleMultiplier(Dungeon.NormalLongRange(7, 15));
	}

	@Override
	public long attackSkill(Char target) {
		return Dungeon.getCycleMultiplier(18);
	}

	@Override
	public long cycledDrRoll() {
		return Dungeon.NormalLongRange(Dungeon.getCycleMultiplier(1), Dungeon.getCycleMultiplier(4));
	}

	@Override
	public long attackProc(Char enemy, long damage) {
		damage = super.attackProc(enemy, damage);
		if (Random.Int(3) == 0) {
			Buff.prolong(enemy, Chill.class, 3f);
			CellEmitter.get(enemy.pos).burst(Speck.factory(Speck.WOOL), 5);
		}
		return damage;
	}

	@Override
	public long defenseProc(Char enemy, long damage) {
		// Teleport escape when damaged occasionally
		if (HP < HT * 0.5f && Random.Int(4) == 0) {
			teleportAway();
		}
		return super.defenseProc(enemy, damage);
	}

	private void teleportAway() {
		int newPos = -1;
		for (int i = 0; i < 10; i++) {
			int p = Dungeon.level.randomRespawnCell(this);
			if (p != -1 && p != pos && Dungeon.level.passable[p] && Actor.findChar(p) == null) {
				newPos = p;
				break;
			}
		}
		if (newPos != -1) {
			CellEmitter.get(pos).burst(Speck.factory(Speck.STAR), 8);
			CellEmitter.get(pos).burst(Speck.factory(Speck.WOOL), 8);
			move(newPos);
			CellEmitter.get(newPos).burst(Speck.factory(Speck.STAR), 8);
			if (sprite != null) {
				sprite.place(newPos);
			}
		}
	}

	@Override
	public void notice() {
		super.notice();
		yell(Messages.get(this, "notice"));
	}

	@Override
	public void die(Object cause) {
		flying = false;
		super.die(cause);

		// Drop festive holiday items & food
		Dungeon.level.drop(new HolidayGift(), pos).sprite.drop();
		Dungeon.level.drop(new HolidayGift(), pos).sprite.drop();
		Dungeon.level.drop(new CandyCane(), pos).sprite.drop();
		Dungeon.level.drop(new CandyCane(), pos).sprite.drop();
		Dungeon.level.drop(new WinterJoyPotion(), pos).sprite.drop();

		// Bestow Holiday Blessing to the Hero
		if (Dungeon.hero != null && Dungeon.hero.isAlive()) {
			Buff.affect(Dungeon.hero, HolidayBlessing.class);
			GLog.p(Messages.get(this, "blessing_received"));
		}
	}
}
