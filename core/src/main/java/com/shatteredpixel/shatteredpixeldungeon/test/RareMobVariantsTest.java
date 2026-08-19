/*
 * Eternity Pixel Dungeon
 * Auto-Test Runner Framework
 */

package com.shatteredpixel.shatteredpixeldungeon.test;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Acidic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Albino;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ArmoredBrute;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Bandit;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Brute;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CausticSlime;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Crab;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM200;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM201;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollExile;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.HermitCrab;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.MobSpawner;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Monk;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Necromancer;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Scorpio;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Senior;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Slime;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.SpectralNecromancer;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief;

public class RareMobVariantsTest {

	public static TestResult run() {
		TestResult result = new TestResult("Rare Enemies & Variant Mapping Test");
		long start = System.currentTimeMillis();

		try {
			assertAlt(result, Rat.class, Albino.class);
			assertAlt(result, Gnoll.class, GnollExile.class);
			assertAlt(result, Crab.class, HermitCrab.class);
			assertAlt(result, Slime.class, CausticSlime.class);
			assertAlt(result, Thief.class, Bandit.class);
			assertAlt(result, Necromancer.class, SpectralNecromancer.class);
			assertAlt(result, Brute.class, ArmoredBrute.class);
			assertAlt(result, DM200.class, DM201.class);
			assertAlt(result, Monk.class, Senior.class);
			assertAlt(result, Scorpio.class, Acidic.class);

			if (MobSpawner.RARE_ALTS.size() >= 10) {
				result.pass("Rare alt table exposes " + MobSpawner.RARE_ALTS.size() + " mappings");
			} else {
				result.fail("Rare alt table too small: expected >= 10 mappings, got " + MobSpawner.RARE_ALTS.size(), null);
			}

		} catch (Exception e) {
			result.fail("Rare enemy mapping test suite encountered fatal exception", e);
		}

		result.durationMs = System.currentTimeMillis() - start;
		return result;
	}

	private static void assertAlt(TestResult result, Class<? extends Mob> base, Class<? extends Mob> expectedAlt) {
		Class<? extends Mob> actual = MobSpawner.RARE_ALTS.get(base);
		if (actual == expectedAlt) {
			result.pass(base.getSimpleName() + " -> " + expectedAlt.getSimpleName());
		} else {
			String actualName = actual == null ? "null" : actual.getSimpleName();
			result.fail(base.getSimpleName() + " expected alt " + expectedAlt.getSimpleName() + " but got " + actualName, null);
		}
	}
}
