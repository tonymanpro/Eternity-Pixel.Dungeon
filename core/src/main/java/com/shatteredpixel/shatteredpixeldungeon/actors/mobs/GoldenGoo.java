/*
 * Eternity Pixel Dungeon
 * Copyright (C) 2026 Eternity Pixel Dungeon Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.*;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GoldenGooSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

public class GoldenGoo extends Goo {

	public GoldenGoo() {
		super();
		spriteClass = GoldenGooSprite.class;
	}

	public static Goo createGoo() {
		if (Random.Float() < 0.25f) {
			return new GoldenGoo();
		} else {
			return new Goo();
		}
	}

	@Override
	public void die(Object cause) {
		super.die(cause);

		Weapon weapon = createClassWeapon();
		weapon.upgrade(4);
		weapon.identify();

		Heap heap = Dungeon.level.drop(weapon, pos);
		if (heap.sprite != null) {
			heap.sprite.drop();
		}
		GLog.p(Messages.get(GoldenGoo.class, "reward", weapon.name()));
	}

	private Weapon createClassWeapon() {
		HeroClass cl = Dungeon.hero != null ? Dungeon.hero.heroClass : null;
		if (cl == HeroClass.WARRIOR) {
			return Random.element(new Weapon[]{new Sword(), new Mace(), new Longsword(), new BattleAxe()});
		} else if (cl == HeroClass.MAGE) {
			return Random.element(new Weapon[]{new Quarterstaff(), new RunicBlade(), new Scimitar()});
		} else if (cl == HeroClass.ROGUE) {
			return Random.element(new Weapon[]{new Dirk(), new AssassinsBlade(), new Dagger()});
		} else if (cl == HeroClass.HUNTRESS) {
			return Random.element(new Weapon[]{new Whip(), new Scimitar(), new Spear()});
		} else if (cl == HeroClass.DUELIST) {
			return Random.element(new Weapon[]{new Rapier(), new Scimitar(), new Sai()});
		} else if (cl == HeroClass.CLERIC) {
			return Random.element(new Weapon[]{new Mace(), new Flail(), new WarHammer()});
		} else if (cl == HeroClass.BARBARIAN) {
			return Random.element(new Weapon[]{new BattleAxe(), new Greataxe(), new WarHammer(), new Greatsword()});
		} else {
			return Generator.randomWeapon(2);
		}
	}
}
