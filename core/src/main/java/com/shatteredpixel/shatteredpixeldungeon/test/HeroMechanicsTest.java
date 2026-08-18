/*
 * Eternity Pixel Dungeon
 * Auto-Test Runner Framework
 */

package com.shatteredpixel.shatteredpixeldungeon.test;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWeapon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Radiance;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Smite;

public class HeroMechanicsTest {

	public static TestResult run() {
		TestResult result = new TestResult("Hero Mechanics & Classes Test");
		long start = System.currentTimeMillis();

		try {
			HeroClass[] classes = {
					HeroClass.WARRIOR,
					HeroClass.MAGE,
					HeroClass.ROGUE,
					HeroClass.HUNTRESS,
					HeroClass.DUELIST,
					HeroClass.CLERIC,
					HeroClass.BARBARIAN,
					HeroClass.RAT_KING
			};

			for (HeroClass cl : classes) {
				try {
					Hero hero = new Hero();
					hero.heroClass = cl;
					hero.live();
					Dungeon.hero = hero;

					if (hero.HT <= 0 || hero.HP <= 0 || hero.STR() <= 0) {
						result.fail(cl.title() + ": invalid base stats (HP=" + hero.HP + "/" + hero.HT + ", STR=" + hero.STR() + ")", null);
						continue;
					}

					// Test Level Up progression from lvl 1 to 30
					long initialHT = hero.HT;
					for (int lvl = 2; lvl <= 30; lvl++) {
						hero.lvl = lvl;
						hero.updateHT(true);
					}

					if (hero.HT <= initialHT) {
						result.fail(cl.title() + ": max HP did not increase with levels (HT=" + hero.HT + ")", null);
						continue;
					}

					// Test Damage & Healing
					hero.damage(10, hero);
					if (hero.HP >= hero.HT) {
						result.fail(cl.title() + ": damage reduction failed (HP=" + hero.HP + "/" + hero.HT + ")", null);
						continue;
					}

					hero.HP = Math.min(hero.HT, hero.HP + 10);

					result.pass("Class " + cl.title() + ": Base HP=" + initialHT + " -> Lvl 30 HP=" + hero.HT + ", STR=" + hero.STR());
				} catch (Exception e) {
					result.fail("Hero class " + cl.title() + " initialization/progression failed", e);
				}
			}

			// Specific Divine Mechanics Test for the Cleric Class
			try {
				Hero cleric = new Hero();
				cleric.heroClass = HeroClass.CLERIC;
				cleric.live();
				Dungeon.hero = cleric;

				// Test Subclasses
				cleric.subClass = HeroSubClass.PRIEST;
				result.pass("Cleric Subclass Priest set successfully");
				cleric.subClass = HeroSubClass.PALADIN;
				result.pass("Cleric Subclass Paladin set successfully");

				// Test Talent Initialization
				Talent.initClassTalents(cleric);
				result.pass("Cleric talent tree initialized with " + cleric.talents.size() + " tiers");

				// Test Spells instantiation
				Smite smite = new Smite();
				Radiance radiance = new Radiance();
				HolyWeapon holyWeapon = new HolyWeapon();

				result.pass("Cleric Spells verified: " + smite.name() + ", " + radiance.name() + ", " + holyWeapon.name());
			} catch (Exception e) {
				result.fail("Cleric Divine Mechanics verification failed", e);
			}

			// Specific Barbarian Mechanics & Subclasses Test
			try {
				Hero barbarian = new Hero();
				barbarian.heroClass = HeroClass.BARBARIAN;
				barbarian.live();
				Dungeon.hero = barbarian;

				// Test Subclasses
				barbarian.subClass = HeroSubClass.WARMONGER;
				result.pass("Barbarian Subclass Warmonger set successfully");
				barbarian.subClass = HeroSubClass.BEASTMASTER;
				result.pass("Barbarian Subclass Beastmaster set successfully");

				// Test Talent Initialization
				Talent.initClassTalents(barbarian);
				result.pass("Barbarian talent tree initialized with " + barbarian.talents.size() + " tiers");

				// Test Subclass Talents
				Talent.initSubclassTalents(barbarian);
				result.pass("Barbarian subclass talents initialized successfully");
			} catch (Exception e) {
				result.fail("Barbarian Mechanics verification failed", e);
			}

		} catch (Exception e) {
			result.fail("Hero mechanics test suite encountered fatal exception", e);
		}

		result.durationMs = System.currentTimeMillis() - start;
		return result;
	}
}
