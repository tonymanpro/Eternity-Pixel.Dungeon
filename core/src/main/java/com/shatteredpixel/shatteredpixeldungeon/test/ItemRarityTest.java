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

			// Test HD Sprites and Glowing effects on Mythical / Cosmic equipment
			com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WarHammer hammer =
					new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WarHammer();
			hammer.rarity = Item.Rarity.MYSTICAL;
			if (hammer.glowing() == null) {
				result.fail("Mystical WarHammer should have a glowing aura", null);
			} else {
				result.pass("Mystical WarHammer glowing aura verified (Color: 0x" + Integer.toHexString(hammer.glowing().color) + ")");
			}

			com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor plate =
					new com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor();
			plate.rarity = Item.Rarity.COSMIC;
			if (plate.glowing() == null) {
				result.fail("Cosmic PlateArmor should have a glowing aura", null);
			} else {
				result.pass("Cosmic PlateArmor glowing aura verified (Color: 0x" + Integer.toHexString(plate.glowing().color) + ")");
			}

			if (plate.emitter() == null) {
				result.fail("Cosmic PlateArmor should have a star particle emitter", null);
			} else {
				result.pass("Cosmic PlateArmor star particle emitter verified");
			}

		} catch (Exception e) {
			result.fail("Item rarity test suite encountered fatal exception", e);
		}

		result.durationMs = System.currentTimeMillis() - start;
		return result;
	}
}
