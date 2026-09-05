/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Experienced Pixel Dungeon
 * Copyright (C) 2019-2024 Trashbox Bobylev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ShadowCaster;
import com.watabou.noosa.Group;
import com.watabou.utils.BArray;

import java.util.ArrayList;

/**
 * DynamicLightingEngine: 2D Raycasting Light Emission System.
 * Projects dynamic ambient light, hero torch glow, pet auras, and magical point light sources
 * across visible cells of the dungeon level.
 */
public class DynamicLightingEngine extends Group {

	public static class LightSource {
		public int cell;
		public int radius;
		public float r, g, b, a;
		public boolean pulse;

		public LightSource(int cell, int radius, float r, float g, float b, float a, boolean pulse) {
			this.cell = cell;
			this.radius = radius;
			this.r = r;
			this.g = g;
			this.b = b;
			this.a = a;
			this.pulse = pulse;
		}
	}

	private static DynamicLightingEngine instance;

	private ArrayList<LightSource> lightSources = new ArrayList<>();
	private boolean[] lightMask;
	private float pulseTime = 0f;

	public DynamicLightingEngine() {
		super();
		instance = this;
		if (Dungeon.level != null) {
			lightMask = new boolean[Dungeon.level.length()];
		}
	}

	public static DynamicLightingEngine get() {
		if (instance == null) {
			instance = new DynamicLightingEngine();
		}
		return instance;
	}

	public void clearLightSources() {
		lightSources.clear();
	}

	public void addLightSource(LightSource source) {
		lightSources.add(source);
	}

	@Override
	public void update() {
		super.update();
		if (!SPDSettings.dynamicLighting() || Dungeon.level == null || Dungeon.hero == null) {
			return;
		}

		pulseTime += 0.05f;

		// Collect dynamic light sources
		lightSources.clear();

		// 1. Hero Torch Light Source
		Hero hero = Dungeon.hero;
		if (hero.isAlive()) {
			float pulse = (float) Math.sin(pulseTime * 3.0f) * 0.08f;
			addLightSource(new LightSource(
					hero.pos,
					6,
					1.0f + pulse,
					0.85f + pulse * 0.5f,
					0.55f,
					0.35f,
					true
			));
		}

		// 2. Active Companion / Pet Light Source
		if (hero.pet != null && hero.pet.isAlive()) {
			addLightSource(new LightSource(
					hero.pet.pos,
					5,
					1.0f,
					0.6f,
					0.2f,
					0.4f,
					true
			));
		}

		// 3. Mobs with elemental / magic light (e.g. Elemental, Wisp, Golden Goo)
		if (Dungeon.level.mobs != null) {
			for (Mob mob : Dungeon.level.mobs) {
				if (mob.isAlive() && Dungeon.level.heroFOV[mob.pos]) {
					String name = mob.getClass().getSimpleName();
					if (name.contains("Elemental") || name.contains("Wisp")) {
						addLightSource(new LightSource(mob.pos, 4, 0.4f, 0.8f, 1.0f, 0.3f, false));
					} else if (name.contains("Golden")) {
						addLightSource(new LightSource(mob.pos, 5, 1.0f, 0.9f, 0.3f, 0.45f, true));
					}
				}
			}
		}

		// Compute light field using ShadowCaster raycasting for each active light source
		int w = Dungeon.level.width();
		int length = Dungeon.level.length();
		if (lightMask == null || lightMask.length != length) {
			lightMask = new boolean[length];
		}

		for (LightSource src : lightSources) {
			if (src.cell < 0 || src.cell >= length) continue;
			int x = src.cell % w;
			int y = src.cell / w;

			BArray.setFalse(lightMask);
			ShadowCaster.castShadow(x, y, w, lightMask, Dungeon.level.losBlocking, src.radius);

			// Apply subtle light emission visual flare to characters in illuminated cells
			for (int i = 0; i < length; i++) {
				if (lightMask[i] && Dungeon.level.heroFOV[i]) {
					Char ch = Actor.findChar(i);
					if (ch != null && ch.sprite != null) {
						ch.sprite.tint(src.r * src.a * 0.3f, src.g * src.a * 0.3f, src.b * src.a * 0.3f, 0.15f);
					}
				}
			}
		}
	}
}
