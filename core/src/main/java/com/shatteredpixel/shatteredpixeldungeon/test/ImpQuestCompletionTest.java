/*
 * Eternity Pixel Dungeon
 * Auto-Test Runner Framework
 */

package com.shatteredpixel.shatteredpixeldungeon.test;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Golem;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Monk;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;

import java.lang.reflect.Field;

public class ImpQuestCompletionTest {

	public static TestResult run() {
		TestResult result = new TestResult("Imp Quest Completion Test");
		long start = System.currentTimeMillis();

		try {
			HeadlessEnvironment.init();
			GamesInProgress.selectedClass = HeroClass.WARRIOR;
			Dungeon.hero = new Hero();
			Dungeon.hero.heroClass = HeroClass.WARRIOR;
			Dungeon.initSeed();
			Dungeon.init();

			Dungeon.branch = Dungeon.BRANCH_NORMAL;
			Dungeon.depth = 17;
			if (Dungeon.newLevel() == null) {
				result.fail("Could not build baseline level for imp quest completion test", null);
				result.durationMs = System.currentTimeMillis() - start;
				return result;
			}

			setQuestState(true, true, false, true, new Ring());
			boolean monkPathOk = isQuestTargetForCurrentPath(new Monk())
					&& !isQuestTargetForCurrentPath(new Golem());
			if (monkPathOk) {
				result.pass("Alternative path targets monks and excludes golems");
			} else {
				result.fail("Alternative path target selection did not match expected monk-only behavior", null);
			}

			setQuestState(true, true, false, false, new Ring());
			boolean golemPathOk = isQuestTargetForCurrentPath(new Golem())
					&& !isQuestTargetForCurrentPath(new Monk());
			if (golemPathOk) {
				result.pass("Non-alternative path targets golems and excludes monks");
			} else {
				result.fail("Non-alternative path target selection did not match expected golem-only behavior", null);
			}

			setQuestState(true, true, false, true, new Ring());
			Statistics.questScores[3] = 0;
			Imp.Quest.complete();
			if (Imp.Quest.isCompleted() && Statistics.questScores[3] == 4000) {
				result.pass("Quest completion marks imp quest as completed and grants expected score");
			} else {
				result.fail("Quest completion did not set expected completion state/score", null);
			}

		} catch (Exception e) {
			result.fail("Imp quest completion test encountered fatal exception", e);
		}

		result.durationMs = System.currentTimeMillis() - start;
		return result;
	}

	private static boolean isQuestTargetForCurrentPath(Object mob) throws Exception {
		Field field = Imp.Quest.class.getDeclaredField("alternative");
		field.setAccessible(true);
		boolean alternative = field.getBoolean(null);
		return (alternative && mob instanceof Monk) || (!alternative && mob instanceof Golem);
	}

	private static void setQuestState(boolean spawned, boolean given, boolean completed, boolean alternative, Ring reward) throws Exception {
		setQuestField("spawned", spawned);
		setQuestField("given", given);
		setQuestField("completed", completed);
		setQuestField("alternative", alternative);
		setQuestField("reward", reward);
	}

	private static void setQuestField(String fieldName, Object value) throws Exception {
		Field field = Imp.Quest.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(null, value);
	}
}
