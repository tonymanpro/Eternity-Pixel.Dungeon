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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ScorpioSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class SpiderPet extends Pet {

	{
		spriteClass = ScorpioSprite.class;
	}

	public SpiderPet() {
		super(PetType.SPIDER);
		immunities.add(Poison.class);
	}

	@Override
	public long attackSkill(Char target) {
		return 11L + (petLevel * 2L);
	}

	@Override
	public long damageRoll() {
		int stage = evolutionStage();
		int min = 2 + petLevel + stage;
		int max = 6 + (petLevel * 2) + (stage * 4);
		return (long) Random.NormalIntRange(min, max);
	}

	@Override
	public long attackProc(Char enemy, long damage) {
		damage = super.attackProc(enemy, damage);
		// Picadura venenosa
		if (Random.Int(2) == 0) {
			Poison p = Buff.affect(enemy, Poison.class);
			p.set(2 + (petLevel / 2f));
			CellEmitter.get(enemy.pos).burst(Speck.factory(Speck.TOXIC), 3);
		}
		return damage;
	}

	@Override
	public boolean act() {
		// Disparo de telaraña ralentizadora a distancia en etapas Joven / Adulto
		if (evolutionStage() >= 1 && enemy != null && enemy.isAlive() && Dungeon.level.heroFOV[enemy.pos]) {
			int dist = Dungeon.level.distance(pos, enemy.pos);
			if (dist > 1 && dist <= 4 && enemy.buff(Cripple.class) == null && enemy.buff(Roots.class) == null) {
				Ballistica bolt = new Ballistica(pos, enemy.pos, Ballistica.MAGIC_BOLT);
				if (bolt.collisionPos == enemy.pos) {
					Sample.INSTANCE.play(Assets.Sounds.HIT);
					CellEmitter.get(enemy.pos).burst(Speck.factory(Speck.WOOL), 6);
					if (evolutionStage() == 2) {
						Buff.affect(enemy, Roots.class, 3f);
					} else {
						Buff.affect(enemy, Cripple.class, 4f);
					}
					spend(TICK);
					return true;
				}
			}
		}
		return super.act();
	}
}
