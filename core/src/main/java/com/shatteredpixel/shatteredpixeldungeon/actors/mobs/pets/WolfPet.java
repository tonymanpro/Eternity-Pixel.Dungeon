/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Eternity Pixel Dungeon
 * Copyright (C) 2026 Eternity PD Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.pets;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class WolfPet extends Pet {

	{
		spriteClass = GnollSprite.class;
	}

	public WolfPet() {
		super(PetType.WOLF);
	}

	@Override
	public long attackSkill(Char target) {
		return 12L + (petLevel * 2L);
	}

	@Override
	public long damageRoll() {
		int stage = evolutionStage();
		int min = 3 + petLevel + (stage * 2);
		int max = 8 + (petLevel * 3) + (stage * 5);
		return (long) Random.NormalIntRange(min, max);
	}

	@Override
	public long attackProc(Char enemy, long damage) {
		damage = super.attackProc(enemy, damage);
		// Mordida desgarradora con sangrado
		if (Random.Int(3) == 0) {
			Bleeding b = Buff.affect(enemy, Bleeding.class);
			b.set(damage / 2f);
			CellEmitter.get(enemy.pos).burst(Speck.factory(Speck.HEALING), 2);
		}
		return damage;
	}

	@Override
	public boolean act() {
		// Olfato pasivo: Detección de puertas secretas y trampas en casillas adyacentes
		if (evolutionStage() >= 1 && Dungeon.hero != null) {
			for (int n : PathFinder.NEIGHBOURS8) {
				int cell = pos + n;
				if (Dungeon.level != null && Dungeon.level.insideMap(cell)) {
					int terrain = Dungeon.level.map[cell];
					if (terrain == Terrain.SECRET_DOOR || terrain == Terrain.SECRET_TRAP) {
						Dungeon.level.discover(cell);
						CellEmitter.get(cell).burst(Speck.factory(Speck.QUESTION), 3);
						Sample.INSTANCE.play(Assets.Sounds.ALERT);
					}
				}
			}
		}
		return super.act();
	}
}
