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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.BatSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class ManticorePet extends Pet {

	{
		spriteClass = BatSprite.class;
		flying = true;
	}

	public ManticorePet() {
		super(PetType.MANTICORE);
		immunities.add(Burning.class);
		immunities.add(Poison.class);
	}

	@Override
	public long attackSkill(Char target) {
		return 12L + (petLevel * 2L);
	}

	@Override
	public long damageRoll() {
		int stage = evolutionStage();
		int min = 3 + petLevel + (stage * 3);
		int max = 8 + (petLevel * 3) + (stage * 5);
		return (long) Random.NormalIntRange(min, max);
	}

	@Override
	public long attackProc(Char enemy, long damage) {
		damage = super.attackProc(enemy, damage);

		// Hybrid Dragon Fire + Spider Venom Tail Proc
		if (Random.Int(2) == 0) {
			Buff.affect(enemy, Burning.class).reignite(enemy);
			CellEmitter.get(enemy.pos).burst(Speck.factory(Speck.INFERNO), 4);
		}
		if (Random.Int(2) == 0) {
			Poison p = Buff.affect(enemy, Poison.class);
			p.set(2 + (petLevel / 2f));
			CellEmitter.get(enemy.pos).burst(Speck.factory(Speck.TOXIC), 3);
		}
		return damage;
	}

	@Override
	public boolean act() {
		// Hybrid Active Abilities: Venom Stinger & Dragon Fire Breath at Range
		if (evolutionStage() >= 1 && enemy != null && enemy.isAlive() && Dungeon.level.heroFOV[enemy.pos]) {
			int dist = Dungeon.level.distance(pos, enemy.pos);
			int maxRange = evolutionStage() == 2 ? 5 : 4;

			if (dist > 1 && dist <= maxRange) {
				Ballistica bolt = new Ballistica(pos, enemy.pos, Ballistica.MAGIC_BOLT);
				if (bolt.collisionPos == enemy.pos) {
					// 1. Spider Hybrid: Inmobilizing Venom Stinger
					if (enemy.buff(Cripple.class) == null && enemy.buff(Roots.class) == null) {
						Sample.INSTANCE.play(Assets.Sounds.HIT);
						CellEmitter.get(enemy.pos).burst(Speck.factory(Speck.WOOL), 6);
						CellEmitter.get(enemy.pos).burst(Speck.factory(Speck.TOXIC), 4);

						Poison p = Buff.affect(enemy, Poison.class);
						p.set(3 + (petLevel / 2f));

						if (evolutionStage() == 2) {
							Buff.affect(enemy, Roots.class, 3.5f);
						} else {
							Buff.affect(enemy, Cripple.class, 4.5f);
						}
						spend(TICK);
						return true;
					}

					// 2. Dragon Hybrid: Searing Crimson Fire Breath
					Sample.INSTANCE.play(Assets.Sounds.BURNING);
					CellEmitter.get(enemy.pos).burst(Speck.factory(Speck.INFERNO), 8);
					int fireDmg = Random.NormalIntRange(4 + petLevel, 10 + (petLevel * 2));
					enemy.damage(fireDmg, this);
					Buff.affect(enemy, Burning.class).reignite(enemy);
					spend(TICK);
					return true;
				}
			}
		}
		return super.act();
	}
}
