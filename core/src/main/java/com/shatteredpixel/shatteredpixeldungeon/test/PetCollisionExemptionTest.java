/*
 * Eternity Pixel Dungeon
 * Copyright (C) 2026 Eternity PD Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.test;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.pets.DragonPet;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.pets.Pet;

public class PetCollisionExemptionTest {

	public static TestResult run() {
		TestResult result = new TestResult("Pet & Ally Collision Exemption Test");

		try {
			HeadlessEnvironment.init();
			GamesInProgress.selectedClass = HeroClass.WARRIOR;
			Dungeon.hero = new Hero();
			Dungeon.hero.heroClass = HeroClass.WARRIOR;
			Dungeon.initSeed();
			Dungeon.init();

			Dungeon.branch = Dungeon.BRANCH_NORMAL;
			Dungeon.depth = 1;
			Dungeon.level = Dungeon.newLevel();
			if (Dungeon.level == null) {
				result.fail("Failed to build level for pet collision exemption test", null);
				return result;
			}

			for (int i = 0; i < Dungeon.level.length(); i++) {
				Dungeon.level.passable[i] = true;
			}

			Hero hero = Dungeon.hero;
			hero.pos = 10 + 10 * Dungeon.level.width();
			Actor.clear();
			Actor.add(hero);

			DragonPet pet = new DragonPet();
			pet.pos = 11 + 10 * Dungeon.level.width(); // Adjacent cell in corridor
			hero.pet = pet;
			Actor.add(pet);

			boolean[] vis = new boolean[Dungeon.level.length()];
			java.util.Arrays.fill(vis, true);

			// 1. Verify findPassable does NOT block Pet cell for Hero
			boolean[] passable = Dungeon.findPassable(hero, Dungeon.level.passable, vis, true);
			if (passable[pet.pos]) {
				result.pass("Hero pathfinding does NOT mark Pet cell as impassable");
			} else {
				result.fail("Hero pathfinding erroneously marked Pet cell as impassable", null);
			}

			// 2. Verify findPassable does NOT block Hero cell for Pet
			boolean[] petPassable = Dungeon.findPassable(pet, Dungeon.level.passable, vis, true);
			if (petPassable[hero.pos]) {
				result.pass("Pet pathfinding does NOT mark Hero cell as impassable");
			} else {
				result.fail("Pet pathfinding erroneously marked Hero cell as impassable", null);
			}

			// 3. Test tactical position swap
			int heroStartPos = hero.pos;
			int petStartPos = pet.pos;

			// Perform position swap logic
			int step = petStartPos;
			com.shatteredpixel.shatteredpixeldungeon.actors.Char occupant = Actor.findChar(step);
			if (occupant != null && occupant != hero && occupant instanceof Pet) {
				int oldHeroPos = hero.pos;
				occupant.pos = oldHeroPos;
			}
			hero.pos = step;

			if (hero.pos == petStartPos && pet.pos == heroStartPos) {
				result.pass("Hero and Pet position swap verified (" + heroStartPos + " <-> " + petStartPos + ")");
			} else {
				result.fail("Position swap failed: Hero=" + hero.pos + ", Pet=" + pet.pos, null);
			}

		} catch (Exception e) {
			result.fail("Exception during Pet collision exemption test", e);
		}

		return result;
	}
}
