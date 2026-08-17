/*
 * Eternity Pixel Dungeon
 * Auto-Test Runner Framework
 */

package com.shatteredpixel.shatteredpixeldungeon.test;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.pets.DragonPet;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class AutonomousBotSim {

	public static TestResult run(int maxTurns) {
		TestResult result = new TestResult("Autonomous Bot AI Simulation (" + maxTurns + " turns)");
		long start = System.currentTimeMillis();

		try {
			// Initialize Hero
			com.shatteredpixel.shatteredpixeldungeon.GamesInProgress.selectedClass = HeroClass.WARRIOR;
			Hero hero = new Hero();
			hero.heroClass = HeroClass.WARRIOR;
			hero.live();
			Dungeon.hero = hero;
			Dungeon.initSeed();
			Dungeon.init();

			// Generate Floor 1
			Dungeon.depth = 1;
			Dungeon.branch = 0;
			Level level = Dungeon.newLevel();
			Dungeon.level = level;
			hero.pos = level.entrance();

			Actor.clear();
			Actor.init();
			Actor.add(hero);

			// Add a loyal Pet companion
			DragonPet pet = new DragonPet();
			hero.pet = pet;
			for (int n : PathFinder.NEIGHBOURS8) {
				int c = hero.pos + n;
				if (c >= 0 && c < level.length() && level.passable[c] && Actor.findChar(c) == null) {
					pet.pos = c;
					Actor.add(pet);
					break;
				}
			}

			int turnsCompleted = 0;
			int attacksExecuted = 0;
			int itemsCollected = 0;
			int floorsDescended = 0;
			Set<Integer> visitedCells = new HashSet<>();

			PathFinder.setMapSize(level.width(), level.height());

			for (int turn = 0; turn < maxTurns; turn++) {
				if (hero.HP <= 0) {
					// Revive for continuous stress testing
					hero.HP = hero.HT;
				}

				visitedCells.add(hero.pos);

				// 1. Check if standing on an item
				Heap heap = level.heaps.get(hero.pos);
				if (heap != null && !heap.isEmpty()) {
					Item item = heap.peek();
					if (item != null && item.doPickUp(hero)) {
						heap.pickUp();
						itemsCollected++;
					}
				}

				// 2. Check for adjacent enemies to attack
				Char target = null;
				for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
					int neighbor = hero.pos + PathFinder.NEIGHBOURS8[i];
					if (neighbor >= 0 && neighbor < level.length()) {
						Char ch = Actor.findChar(neighbor);
						if (ch instanceof Mob && !(ch == pet)) {
							target = ch;
							break;
						}
					}
				}

				if (target != null) {
					hero.attack(target);
					attacksExecuted++;
					hero.spend(1f);
				} else if (hero.pos == level.exit() && Dungeon.depth < 25) {
					// Descend to next floor
					Dungeon.depth++;
					floorsDescended++;
					level = Dungeon.newLevel();
					Dungeon.level = level;
					hero.pos = level.entrance();
					Actor.clear();
					Actor.init();
					Actor.add(hero);
					if (pet.HP > 0) {
						for (int n : PathFinder.NEIGHBOURS8) {
							int c = hero.pos + n;
							if (c >= 0 && c < level.length() && level.passable[c] && Actor.findChar(c) == null) {
								pet.pos = c;
								Actor.add(pet);
								break;
							}
						}
					}
					PathFinder.setMapSize(level.width(), level.height());
					visitedCells.clear();
					hero.spend(1f);
				} else {
					// Move towards exit or random unvisited neighbor
					int nextStep = -1;
					boolean[] passable = BArray.not(level.solid, null);
					PathFinder.buildDistanceMap(level.exit(), passable);

					if (PathFinder.distance[hero.pos] < Integer.MAX_VALUE) {
						int bestDist = PathFinder.distance[hero.pos];
						for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
							int neighbor = hero.pos + PathFinder.NEIGHBOURS8[i];
							if (neighbor >= 0 && neighbor < level.length() && passable[neighbor]) {
								if (PathFinder.distance[neighbor] < bestDist && Actor.findChar(neighbor) == null) {
									bestDist = PathFinder.distance[neighbor];
									nextStep = neighbor;
								}
							}
						}
					}

					if (nextStep != -1) {
						hero.pos = nextStep;
					}
					hero.spend(1f);
				}

				turnsCompleted++;
			}

			result.pass("Completed " + turnsCompleted + " turns across " + (floorsDescended + 1) + " floors (" +
					attacksExecuted + " attacks, " + itemsCollected + " items collected, " + visitedCells.size() + " cells explored)");

		} catch (Exception e) {
			result.fail("Autonomous Bot Simulation encountered an exception", e);
		}

		result.durationMs = System.currentTimeMillis() - start;
		return result;
	}
}
