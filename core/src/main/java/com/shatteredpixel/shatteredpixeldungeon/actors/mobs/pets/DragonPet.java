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
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ElementalSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class DragonPet extends Pet {

	{
		spriteClass = ElementalSprite.Fire.class;
		flying = true;
	}

	public DragonPet() {
		super(PetType.DRAGON);
		immunities.add(Burning.class);
	}

	@Override
	public long attackSkill(Char target) {
		return 10L + (petLevel * 2L);
	}

	@Override
	public long damageRoll() {
		int stage = evolutionStage();
		int min = 2 + petLevel + (stage * 3);
		int max = 6 + (petLevel * 3) + (stage * 6);
		return (long) Random.NormalIntRange(min, max);
	}

	@Override
	public long attackProc(Char enemy, long damage) {
		damage = super.attackProc(enemy, damage);
		if (Random.Int(2) == 0) {
			Buff.affect(enemy, Burning.class).reignite(enemy);
			CellEmitter.get(enemy.pos).burst(Speck.factory(Speck.INFERNO), 4);
		}
		return damage;
	}

	@Override
	public boolean act() {
		// Aliento ígneo a distancia en etapas Joven / Adulto
		if (evolutionStage() >= 1 && enemy != null && enemy.isAlive() && Dungeon.level.heroFOV[enemy.pos]) {
			int dist = Dungeon.level.distance(pos, enemy.pos);
			if (dist > 1 && dist <= (evolutionStage() == 2 ? 4 : 3)) {
				Ballistica bolt = new Ballistica(pos, enemy.pos, Ballistica.MAGIC_BOLT);
				if (bolt.collisionPos == enemy.pos) {
					Sample.INSTANCE.play(Assets.Sounds.BURNING);
					CellEmitter.get(enemy.pos).burst(Speck.factory(Speck.INFERNO), 6);
					int fireDmg = Random.NormalIntRange(3 + petLevel, 8 + (petLevel * 2));
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
