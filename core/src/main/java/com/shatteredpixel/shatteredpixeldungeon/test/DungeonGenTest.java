/*
 * Eternity Pixel Dungeon
 * Auto-Test Runner Framework
 */

package com.shatteredpixel.shatteredpixeldungeon.test;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.MiningLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;

public class DungeonGenTest {

	public static TestResult run() {
		TestResult result = new TestResult("Dungeon Generation & Reachability Test");
		long start = System.currentTimeMillis();

		try {
			com.shatteredpixel.shatteredpixeldungeon.GamesInProgress.selectedClass = HeroClass.WARRIOR;
			Dungeon.hero = new Hero();
			Dungeon.hero.heroClass = HeroClass.WARRIOR;
			Dungeon.initSeed();
			Dungeon.init();

			int[] depthsToTest = {1, 2, 3, 4, 5, 6, 10, 11, 15, 16, 20, 21, 25, 26};

			for (int d : depthsToTest) {
				Dungeon.depth = d;
				Dungeon.branch = 0;
				Level level = Dungeon.newLevel();

				if (level == null) {
					result.fail("Depth " + d + ": level generation returned null", null);
					continue;
				}

				if (level.width() <= 0 || level.height() <= 0) {
					result.fail("Depth " + d + ": invalid level dimensions (" + level.width() + "x" + level.height() + ")", null);
					continue;
				}

				int entrance = level.entrance();
				int exit = level.exit();

				if (entrance < 0 || entrance >= level.length()) {
					result.fail("Depth " + d + ": invalid entrance index (" + entrance + ")", null);
					continue;
				}

				// Check reachability on normal exploration levels (non-boss and non-last-level)
				if (!Dungeon.bossLevel(d) && d < 26 && exit >= 0 && exit < level.length()) {
					PathFinder.setMapSize(level.width(), level.height());
					boolean[] passable = new boolean[level.length()];
					for (int i = 0; i < level.length(); i++) {
						int t = level.map[i];
						passable[i] = (t != Terrain.WALL && t != Terrain.WALL_DECO && t != Terrain.STATUE && t != Terrain.STATUE_SP && t != Terrain.BOOKSHELF);
					}
					PathFinder.buildDistanceMap(entrance, passable);

					if (PathFinder.distance[exit] == Integer.MAX_VALUE) {
						result.fail("Depth " + d + ": exit (" + exit + ") is unreachable from entrance (" + entrance + ")", null);
						continue;
					}
				}

				result.pass("Depth " + d + " (" + level.getClass().getSimpleName() + "): " + level.width() + "x" + level.height() + ", entrance=" + entrance + ", exit=" + exit + ", mobs=" + level.mobs.size() + ", heaps=" + level.heaps.size);
			}

			// Test Bonus Vault / Mining Branch level
			try {
				Dungeon.depth = 1;
				Dungeon.branch = 1;
				MiningLevel mining = new MiningLevel();
				mining.create();
				if (mining.entrance() >= 0) {
					result.pass("Vault / Mining Branch Level created successfully with entrance=" + mining.entrance());
				} else {
					result.fail("Vault / Mining Level has invalid entrance", null);
				}
			} catch (Exception e) {
				result.fail("Vault Branch level generation failed", e);
			}

		} catch (Exception e) {
			result.fail("Dungeon generation test suite encountered fatal exception", e);
		}

		result.durationMs = System.currentTimeMillis() - start;
		return result;
	}
}
