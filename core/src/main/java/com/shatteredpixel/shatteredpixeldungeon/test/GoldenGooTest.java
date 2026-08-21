/*
 * Eternity Pixel Dungeon
 * Auto-Test Suite: Golden Goo Boss Variant Test
 */

package com.shatteredpixel.shatteredpixeldungeon.test;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GoldenGoo;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerLevel;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GoldenGooSprite;

public class GoldenGooTest {

	public static TestResult run() {
		TestResult result = new TestResult("Golden Goo Boss Variant Test");

		try {
			// Test 1: Class instantiation & sprite verification
			GoldenGoo ggoo = new GoldenGoo();
			if (ggoo.spriteClass == GoldenGooSprite.class) {
				result.pass("GoldenGoo correctly uses GoldenGooSprite texture film");
			} else {
				result.fail("GoldenGoo spriteClass mismatch", null);
			}

			// Test 2: Factory creation & probability distribution test (25% chance)
			int goldenCount = 0;
			int total = 1000;
			for (int i = 0; i < total; i++) {
				Goo created = GoldenGoo.createGoo();
				if (created instanceof GoldenGoo) {
					goldenCount++;
				}
			}
			double ratio = (double) goldenCount / total;
			if (ratio >= 0.20 && ratio <= 0.30) {
				result.pass("GoldenGoo spawn chance verified (~25%): " + goldenCount + "/" + total);
			} else {
				result.fail("GoldenGoo spawn ratio out of expected 25% bounds: " + ratio, null);
			}

			// Test 3: Defeat reward drops +4 class-tailored weapon
			Dungeon.level = new SewerLevel();
			Dungeon.level.create();
			Dungeon.hero = new Hero();
			Dungeon.hero.heroClass = HeroClass.BARBARIAN;
			Dungeon.hero.sprite = new com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite();

			int pos = Dungeon.level.width() * 10 + 10;
			for (int i = Dungeon.level.width() * 5; i < Dungeon.level.length() - Dungeon.level.width() * 5; i++) {
				if (Dungeon.level.passable[i]) {
					pos = i;
					break;
				}
			}
			ggoo.pos = pos;
			ggoo.sprite = new GoldenGooSprite();

			ggoo.die(Dungeon.hero);

			boolean foundWeapon = false;
			for (Heap heap : Dungeon.level.heaps.values()) {
				for (Item item : heap.items) {
					if (item instanceof Weapon && item.level() == 4 && item.isIdentified()) {
						foundWeapon = true;
						result.pass("Defeated GoldenGoo dropped +4 identified class weapon: " + item.name());
						break;
					}
				}
			}

			if (!foundWeapon) {
				result.fail("GoldenGoo did not drop +4 class weapon on defeat", null);
			}

		} catch (Exception e) {
			result.fail("GoldenGoo test exception", e);
		}

		return result;
	}
}
