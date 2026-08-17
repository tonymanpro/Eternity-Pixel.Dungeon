/*
 * Eternity Pixel Dungeon
 * Proprietary Platform Achievements Test
 */

package com.shatteredpixel.shatteredpixeldungeon.test;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.pets.Pet;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WarHammer;
import com.shatteredpixel.shatteredpixeldungeon.services.platform.PlatformManager;

public class PlatformAchievementsTest {

	public static TestResult run() {
		TestResult result = new TestResult("Platform Achievements & Badges Sync Test");
		long start = System.currentTimeMillis();

		try {
			// 1. Verify PlatformServices Active Backend
			String platformId = PlatformManager.get().getPlatformId();
			result.pass("Active Platform Backend verified: " + platformId);

			// 2. Test Pet Hatching Achievement & Sync
			Badges.validatePetHatched(Pet.PetType.DRAGON);
			if (PlatformManager.get().isAchievementUnlocked("PET_FIRST_HATCH") &&
				PlatformManager.get().isAchievementUnlocked("PET_HATCH_DRAGON")) {
				result.pass("PET_FIRST_HATCH and PET_HATCH_DRAGON synced to PlatformServices");
			} else {
				result.fail("Failed to sync PET_FIRST_HATCH / PET_HATCH_DRAGON", null);
			}

			// Hatch remaining pets to trigger PET_ALL_SPECIES
			Badges.validatePetHatched(Pet.PetType.WOLF);
			Badges.validatePetHatched(Pet.PetType.FAIRY);
			Badges.validatePetHatched(Pet.PetType.SPIDER);
			if (PlatformManager.get().isAchievementUnlocked("PET_ALL_SPECIES")) {
				result.pass("PET_ALL_SPECIES meta-achievement unlocked and synced");
			} else {
				result.fail("Failed to unlock PET_ALL_SPECIES", null);
			}

			// 3. Test Mythic and Cosmic Item Rarity Badges
			WarHammer hammer = new WarHammer();
			hammer.rarity = Item.Rarity.MYTHICAL;
			Badges.validateItemRarity(hammer);

			PlateArmor armor = new PlateArmor();
			armor.rarity = Item.Rarity.COSMIC;
			Badges.validateItemRarity(armor);

			if (PlatformManager.get().isAchievementUnlocked("FIND_MYTHIC_ITEM") &&
				PlatformManager.get().isAchievementUnlocked("FIND_COSMIC_ITEM")) {
				result.pass("FIND_MYTHIC_ITEM and FIND_COSMIC_ITEM achievements verified");
			} else {
				result.fail("Failed to unlock rarity achievements", null);
			}

			// 4. Test Fully Equipped Cosmic Set Achievement
			Hero hero = new Hero();
			hero.heroClass = HeroClass.CLERIC;
			hammer.rarity = Item.Rarity.COSMIC;
			hero.belongings.weapon = hammer;
			hero.belongings.armor = armor;
			Badges.validateCosmicEquipment(hero);

			if (PlatformManager.get().isAchievementUnlocked("FULLY_EQUIPPED_COSMIC")) {
				result.pass("FULLY_EQUIPPED_COSMIC achievement unlocked and synced");
			} else {
				result.fail("Failed to unlock FULLY_EQUIPPED_COSMIC", null);
			}

			// 5. Test Cleric Victory Badge Sync
			Badges.unlock(Badges.Badge.VICTORY_CLERIC);
			if (PlatformManager.get().isAchievementUnlocked("VICTORY_CLERIC")) {
				result.pass("VICTORY_CLERIC badge unlocked and synced to PlatformServices");
			} else {
				result.fail("Failed to sync VICTORY_CLERIC", null);
			}

		} catch (Throwable t) {
			result.fail("Exception during Platform Achievements Test", t);
		}

		result.durationMs = System.currentTimeMillis() - start;
		return result;
	}
}
