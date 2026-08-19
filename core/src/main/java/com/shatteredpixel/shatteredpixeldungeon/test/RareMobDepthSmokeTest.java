/*
 * Eternity Pixel Dungeon
 * Auto-Test Runner Framework
 */

package com.shatteredpixel.shatteredpixeldungeon.test;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Bat;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Brute;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Crab;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Eye;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Ghoul;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.MobSpawner;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Monk;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Skeleton;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Snake;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Spinner;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Succubus;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Swarm;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warlock;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RareMobDepthSmokeTest {

	private static final int SAMPLES_PER_DEPTH = 120;
	private static final long BASE_SEED = 0xE7D6000L;

	private static final HashMap<Class<? extends Mob>, Class<? extends Mob>> ALT_TO_BASE = new HashMap<>();
	static {
		for (Map.Entry<Class<? extends Mob>, Class<? extends Mob>> entry : MobSpawner.RARE_ALTS.entrySet()) {
			ALT_TO_BASE.put(entry.getValue(), entry.getKey());
		}
	}

	public static TestResult run() {
		TestResult result = new TestResult("Rare Enemy Depth Smoke Test");
		long start = System.currentTimeMillis();

		try {
			HeadlessEnvironment.init();
			Dungeon.initSeed();
			Dungeon.init();

			int[] depths = {1, 6, 11, 16, 21};
			for (int depth : depths) {
				runDepthSampling(result, depth);
			}

		} catch (Exception e) {
			result.fail("Rare enemy depth smoke test encountered fatal exception", e);
		}

		result.durationMs = System.currentTimeMillis() - start;
		return result;
	}

	private static void runDepthSampling(TestResult result, int depth) {
		Set<Class<? extends Mob>> allowedBase = allowedBaseForDepth(depth);

		int seenAlts = 0;
		int totalEntries = 0;

		for (int i = 0; i < SAMPLES_PER_DEPTH; i++) {
			Random.pushGenerator(BASE_SEED + depth * 1000L + i);
			ArrayList<Class<? extends Mob>> rotation = MobSpawner.getMobRotation(depth);
			Random.popGenerator();

			for (Class<? extends Mob> mobClass : rotation) {
				totalEntries++;
				Class<? extends Mob> normalized = normalizeMobClass(mobClass);
				if (normalized == null || !allowedBase.contains(normalized)) {
					result.fail("Depth " + depth + ": unexpected mob in rotation -> " + mobClass.getSimpleName(), null);
					return;
				}
				if (ALT_TO_BASE.containsKey(mobClass)) {
					seenAlts++;
				}
			}
		}

		if (seenAlts > 0) {
			result.pass("Depth " + depth + ": validated " + totalEntries + " entries; rare alts observed=" + seenAlts);
		} else {
			result.pass("Depth " + depth + ": validated " + totalEntries + " entries; no rare alts sampled in this run");
		}
	}

	private static Set<Class<? extends Mob>> allowedBaseForDepth(int depth) {
		switch (depth) {
			case 1:
				return setOf(Rat.class, Snake.class);
			case 6:
				return setOf(Skeleton.class, Thief.class, Swarm.class);
			case 11:
				return setOf(Bat.class, Brute.class, Shaman.class, Spinner.class);
			case 16:
				return setOf(Ghoul.class, Elemental.class, Warlock.class, Monk.class);
			case 21:
				return setOf(Succubus.class, Eye.class);
			default:
				return new HashSet<>();
		}
	}

	private static Class<? extends Mob> normalizeMobClass(Class<? extends Mob> mobClass) {
		if (mobClass == null) {
			return null;
		}
		Class<? extends Mob> base = ALT_TO_BASE.get(mobClass);
		if (base != null) {
			return base;
		}
		if (Shaman.class.isAssignableFrom(mobClass)) {
			return Shaman.class;
		}
		if (Elemental.class.isAssignableFrom(mobClass)) {
			return Elemental.class;
		}
		return mobClass;
	}

	@SafeVarargs
	private static Set<Class<? extends Mob>> setOf(Class<? extends Mob>... classes) {
		HashSet<Class<? extends Mob>> out = new HashSet<>();
		if (classes == null) {
			return out;
		}
		out.addAll(Arrays.asList(classes));
		out.remove(null);
		return out;
	}
}
