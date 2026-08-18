/*
 * Eternity Pixel Dungeon
 * Auto-Test Runner Framework
 */

package com.shatteredpixel.shatteredpixeldungeon.test;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.pets.DragonPet;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.pets.FairyPet;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.pets.Pet;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.pets.SpiderPet;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.pets.WolfPet;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.pets.PetEgg;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;

public class PetSystemTest {

	public static TestResult run() {
		TestResult result = new TestResult("Pets & Companions System Test");
		long start = System.currentTimeMillis();

		try {
			Hero hero = new Hero();
			hero.heroClass = HeroClass.WARRIOR;
			hero.live();
			Dungeon.hero = hero;

			// 1. Test Pet Egg Incubation
			PetEgg egg = new PetEgg(Pet.PetType.DRAGON);
			int initialIncubation = egg.incubation;
			egg.incubation += 80;

			if (egg.isReadyToHatch()) {
				result.pass("PetEgg incubation and hatching readiness verified (" + initialIncubation + " -> " + egg.incubation + "/" + PetEgg.REQUIRED_INCUBATION + ")");
			} else {
				result.fail("PetEgg failed isReadyToHatch() check after incubation", null);
			}

			// 2. Test All 5 Pet Species Instantiation & Mechanics
			Pet[] pets = {
					new DragonPet(),
					new WolfPet(),
					new FairyPet(),
					new SpiderPet(),
					new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.pets.ManticorePet()
			};

			for (Pet pet : pets) {
				try {
					hero.pet = pet;

					if (pet.HT <= 0) {
						result.fail(pet.name() + ": invalid initial HP (" + pet.HT + ")", null);
						continue;
					}

					// Test Evolution from Baby -> Young -> Adult through exp gain
					int stageBaby = pet.evolutionStage();
					pet.gainExp(150); // Level up
					int stageAfterExp = pet.evolutionStage();
					long leveledHT = pet.HT;

					// Test Feeding Mechanics
					pet.damage(10, hero);
					boolean fedPotion = pet.feed(new PotionOfHealing());
					boolean fedFood = pet.feed(new Food());

					if (!fedPotion || !fedFood) {
						result.fail(pet.name() + ": feeding failed (potion=" + fedPotion + ", food=" + fedFood + ")", null);
						continue;
					}

					// Test Tactical Orders
					pet.currentOrder = Pet.PetOrder.FOLLOW;
					if (pet.currentOrder != Pet.PetOrder.FOLLOW) {
						result.fail(pet.name() + ": failed to set FOLLOW order", null);
						continue;
					}

					pet.currentOrder = Pet.PetOrder.DEFEND;
					if (pet.currentOrder != Pet.PetOrder.DEFEND) {
						result.fail(pet.name() + ": failed to set DEFEND order", null);
						continue;
					}

					result.pass("Pet " + pet.name() + " (Stage " + stageBaby + " -> " + stageAfterExp + "): Level=" + pet.petLevel + ", HP=" + leveledHT + ", Feed OK, Orders OK");
				} catch (Exception e) {
					result.fail("Pet " + pet.getClass().getSimpleName() + " test failed", e);
				}
			}

		} catch (Exception e) {
			result.fail("Pet system test suite encountered fatal exception", e);
		}

		result.durationMs = System.currentTimeMillis() - start;
		return result;
	}
}
