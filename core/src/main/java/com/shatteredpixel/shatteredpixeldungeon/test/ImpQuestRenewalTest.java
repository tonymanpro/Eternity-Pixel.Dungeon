/*
 * Eternity Pixel Dungeon
 * Auto-Test Runner Framework
 */

package com.shatteredpixel.shatteredpixeldungeon.test;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.items.PsycheChest;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.VaultLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;

public class ImpQuestRenewalTest {

	public static TestResult run() {
		TestResult result = new TestResult("Imp Quest Renewal Flow Test");
		long start = System.currentTimeMillis();

		try {
			HeadlessEnvironment.init();
			GamesInProgress.selectedClass = HeroClass.WARRIOR;
			Dungeon.hero = new Hero();
			Dungeon.hero.heroClass = HeroClass.WARRIOR;
			Dungeon.initSeed();
			Dungeon.init();

			int[] depths = {17, 18, 19};
			for (int depth : depths) {
				Imp.Quest.reset();
				PsycheChest.questDepth = -1;

				Dungeon.depth = depth;
				Dungeon.branch = Dungeon.BRANCH_NORMAL;
				Level city = Dungeon.newLevel();

				if (!(city instanceof CityLevel)) {
					result.fail("Depth " + depth + ": expected CityLevel, got " + city.getClass().getSimpleName(), null);
					continue;
				}

				boolean hasImp = false;
				for (Mob mob : city.mobs) {
					if (mob instanceof Imp) {
						hasImp = true;
						break;
					}
				}
				if (!hasImp) {
					result.fail("Depth " + depth + ": no Imp found in city level mobs", null);
					continue;
				}

				if (Imp.Quest.reward == null) {
					result.fail("Depth " + depth + ": quest reward not initialized", null);
					continue;
				}

				boolean hasQuestBranchExit = false;
				for (LevelTransition transition : city.transitions) {
					if (transition.type == LevelTransition.Type.BRANCH_EXIT
							&& transition.destBranch == Dungeon.BRANCH_QUESTS
							&& transition.destDepth == depth
							&& transition.destType == LevelTransition.Type.BRANCH_ENTRANCE) {
						hasQuestBranchExit = true;
						break;
					}
				}
				if (!hasQuestBranchExit) {
					result.fail("Depth " + depth + ": no branch exit transition to quest branch found", null);
					continue;
				}

				Dungeon.depth = depth;
				Dungeon.branch = Dungeon.BRANCH_QUESTS;
				Level questBranch = Dungeon.newLevel();
				if (!(questBranch instanceof VaultLevel)) {
					result.fail("Depth " + depth + ": expected VaultLevel in quest branch, got " + questBranch.getClass().getSimpleName(), null);
					continue;
				}

				result.pass("Depth " + depth + ": city imp room + quest branch vault flow validated");
			}

		} catch (Exception e) {
			result.fail("Imp quest renewal flow test encountered fatal exception", e);
		}

		result.durationMs = System.currentTimeMillis() - start;
		return result;
	}
}
