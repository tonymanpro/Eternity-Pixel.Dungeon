/*
 * Eternity Pixel Dungeon
 * Auto-Test Runner Framework
 */

package com.shatteredpixel.shatteredpixeldungeon.test;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.MobSpawner;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.RatSkull;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RareMobFrequencyStatsTest {

	private static final int SAMPLES_PER_DEPTH = 250;
	private static final long BASE_SEED = 0xE7D6400L;
	private static final int[] DEPTHS = {1, 6, 11, 16, 21};
	private static final double MIN_ELIGIBLE_RATE = 0.005;
	private static final double MAX_ELIGIBLE_RATE = 0.04;

	public static TestResult run() {
		TestResult result = new TestResult("Rare Enemy Frequency Stats Test");
		long start = System.currentTimeMillis();

		try {
			HeadlessEnvironment.init();
			Dungeon.initSeed();
			Dungeon.init();

			double totalAlt = 0;
			double totalEntries = 0;
			double totalEligible = 0;

			for (int depth : DEPTHS) {
				DepthStats stats = sampleDepth(depth);
				totalAlt += stats.altCount;
				totalEntries += stats.totalCount;
				totalEligible += stats.eligibleCount;

				double totalRate = stats.totalAltRate();
				double eligibleRate = stats.eligibleAltRate();
				String line = String.format(Locale.US,
						"Depth %d: alts=%d / eligible=%d (eligibleRate=%.3f%%), total=%d (totalRate=%.3f%%)",
						depth,
						stats.altCount,
						stats.eligibleCount,
						eligibleRate * 100d,
						stats.totalCount,
						totalRate * 100d);

				if (stats.eligibleCount == 0) {
					result.pass(line + " (no eligible mobs at this depth)");
				} else if (eligibleRate < MIN_ELIGIBLE_RATE || eligibleRate > MAX_ELIGIBLE_RATE) {
					result.fail(line + " out of expected eligible range [0.5%%, 4.0%%]", null);
				} else {
					result.pass(line);
				}
			}

			double globalTotalRate = totalAlt / Math.max(1d, totalEntries);
			double globalEligibleRate = totalAlt / Math.max(1d, totalEligible);
			double expectedEligibleRate = (1d / 50d) * RatSkull.exoticChanceMultiplier();
			String globalLine = String.format(Locale.US,
					"Global rates: eligible=%.3f%% (%d/%d), total=%.3f%% (%d/%d), expectedEligible=%.3f%%",
					globalEligibleRate * 100d,
					(long) totalAlt,
					(long) totalEligible,
					globalTotalRate * 100d,
					(long) totalAlt,
					(long) totalEntries,
					expectedEligibleRate * 100d);

			if (globalEligibleRate < MIN_ELIGIBLE_RATE || globalEligibleRate > MAX_ELIGIBLE_RATE) {
				result.fail(globalLine + " out of expected eligible range [0.5%%, 4.0%%]", null);
			} else {
				result.pass(globalLine);
			}

		} catch (Exception e) {
			result.fail("Rare enemy frequency stats test encountered fatal exception", e);
		}

		result.durationMs = System.currentTimeMillis() - start;
		return result;
	}

	private static DepthStats sampleDepth(int depth) {
		DepthStats stats = new DepthStats();

		HashMap<Class<? extends Mob>, Class<? extends Mob>> altToBase = new HashMap<>();
		for (Map.Entry<Class<? extends Mob>, Class<? extends Mob>> entry : MobSpawner.RARE_ALTS.entrySet()) {
			altToBase.put(entry.getValue(), entry.getKey());
		}

		for (int i = 0; i < SAMPLES_PER_DEPTH; i++) {
			Random.pushGenerator(BASE_SEED + depth * 10_000L + i);
			ArrayList<Class<? extends Mob>> rotation = MobSpawner.getMobRotation(depth);
			Random.popGenerator();

			for (Class<? extends Mob> mobClass : rotation) {
				stats.totalCount++;
				if (MobSpawner.RARE_ALTS.containsKey(mobClass)) {
					stats.eligibleCount++;
				}
				if (altToBase.containsKey(mobClass)) {
					stats.altCount++;
				}
			}
		}

		return stats;
	}

	private static class DepthStats {
		int altCount;
		int totalCount;
		int eligibleCount;

		double totalAltRate() {
			return altCount / (double) Math.max(1, totalCount);
		}

		double eligibleAltRate() {
			return altCount / (double) Math.max(1, eligibleCount);
		}
	}
}
