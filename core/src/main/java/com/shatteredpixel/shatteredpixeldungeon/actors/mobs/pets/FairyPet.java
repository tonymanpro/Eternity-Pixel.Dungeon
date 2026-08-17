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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.sprites.BeeSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class FairyPet extends Pet {

	private int healCooldown = 0;

	{
		spriteClass = BeeSprite.class;
		flying = true;
	}

	public FairyPet() {
		super(PetType.FAIRY);
	}

	@Override
	public long attackSkill(Char target) {
		return 8L + (petLevel * 2L);
	}

	@Override
	public long damageRoll() {
		int stage = evolutionStage();
		int min = 1 + petLevel + stage;
		int max = 4 + (petLevel * 2) + (stage * 3);
		return (long) Random.NormalIntRange(min, max);
	}

	@Override
	public boolean act() {
		Hero hero = Dungeon.hero;
		if (hero != null && hero.isAlive() && evolutionStage() >= 1) {
			// Pulso de curación periódica si el héroe está herido
			if (healCooldown <= 0 && hero.HP < hero.HT && Dungeon.level != null && Dungeon.level.distance(pos, hero.pos) <= 4) {
				long heal = 4L + petLevel + (evolutionStage() * 3L);
				hero.HP = Math.min(hero.HT, hero.HP + heal);
				if (hero.sprite != null) {
					hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), 5);
				}
				CellEmitter.get(pos).burst(Speck.factory(Speck.LIGHT), 5);
				Sample.INSTANCE.play(Assets.Sounds.WATER);
				healCooldown = 12; // Turnos de recarga
			} else if (healCooldown > 0) {
				healCooldown--;
			}
		}

		// Revelación continua de luz en 2 casillas a la redonda
		for (int n : PathFinder.NEIGHBOURS8) {
			int cell = pos + n;
			if (Dungeon.level != null && Dungeon.level.insideMap(cell)) {
				Dungeon.level.mapped[cell] = true;
			}
		}

		return super.act();
	}
}
