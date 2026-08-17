/*
 * Eternity Pixel Dungeon
 * Auto-Test Runner Framework
 */

package com.shatteredpixel.shatteredpixeldungeon.test;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;

import java.util.EnumMap;
import java.util.Map;

public class ItemRarityTest {

	public static TestResult run() {
		TestResult result = new TestResult("Item Rarity & Generation Test");
		long start = System.currentTimeMillis();

		try {
			Hero hero = new Hero();
			hero.heroClass = HeroClass.WARRIOR;
			hero.live();
			Dungeon.hero = hero;
			Dungeon.depth = 10;
			Dungeon.initSeed();

			int totalItems = 1000;
			Map<Item.Rarity, Integer> rarityCounts = new EnumMap<>(Item.Rarity.class);
			for (Item.Rarity r : Item.Rarity.values()) {
				rarityCounts.put(r, 0);
			}

			int validItems = 0;

			for (int i = 0; i < totalItems; i++) {
				Item item = Generator.random();
				if (item == null) {
					result.fail("Generator.random() returned null item at index " + i, null);
					continue;
				}

				if (item.name() == null || item.name().isEmpty()) {
					result.fail("Item at index " + i + " has empty name: " + item.getClass().getSimpleName(), null);
					continue;
				}

				Item.Rarity rarity = item.rarity;
				if (rarity == null) {
					rarity = Item.Rarity.COMMON;
				}

				rarityCounts.put(rarity, rarityCounts.get(rarity) + 1);
				validItems++;
			}

			result.pass("Successfully generated and validated " + validItems + "/" + totalItems + " random items");

			StringBuilder sb = new StringBuilder("Sample Rarity Distribution: ");
			int displayed = 0;
			for (Map.Entry<Item.Rarity, Integer> entry : rarityCounts.entrySet()) {
				if (entry.getValue() > 0 && displayed < 8) {
					sb.append(entry.getKey().name()).append("=").append(entry.getValue()).append(" (")
							.append(String.format("%.1f", (entry.getValue() * 100.0 / totalItems))).append("%), ");
					displayed++;
				}
			}
			result.info(sb.toString());

		} catch (Exception e) {
			result.fail("Item rarity test suite encountered fatal exception", e);
		}

		result.durationMs = System.currentTimeMillis() - start;
		return result;
	}
}
