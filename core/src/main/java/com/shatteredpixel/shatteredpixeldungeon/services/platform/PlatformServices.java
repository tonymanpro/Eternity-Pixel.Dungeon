/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Eternity Pixel Dungeon
 * Copyright (C) 2026 Eternity Pixel Dungeon Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.services.platform;

public interface PlatformServices {

	/**
	 * Identifier of the current platform backend (e.g. "NULL", "STEAMWORKS", "GOG").
	 */
	String getPlatformId();

	/**
	 * Initializes the platform API.
	 * @return true if initialized successfully, false otherwise.
	 */
	boolean initialize();

	/**
	 * Updates the platform API loop (called every frame if needed).
	 */
	void update();

	/**
	 * Disposes and cleans up platform resources on game exit.
	 */
	void dispose();

	/**
	 * Checks if the platform service is currently available and connected.
	 */
	boolean isAvailable();

	/**
	 * Unlocks an achievement on the platform.
	 * @param achievementId Platform achievement identifier.
	 */
	void unlockAchievement(String achievementId);

	/**
	 * Checks if an achievement is unlocked on the platform.
	 */
	boolean isAchievementUnlocked(String achievementId);

	/**
	 * Stores a platform statistic.
	 */
	void setStat(String statName, int value);

	/**
	 * Retrieves a platform statistic.
	 */
	int getStat(String statName, int defaultValue);

	/**
	 * Updates the player's Rich Presence status (e.g. "Descending Floor 12 - Cleric").
	 */
	void setRichPresence(String key, String value);

	/**
	 * Writes data to the platform's Cloud Save storage.
	 */
	boolean saveToCloud(String fileName, byte[] data);

	/**
	 * Reads data from the platform's Cloud Save storage.
	 */
	byte[] loadFromCloud(String fileName);

	/**
	 * Checks if the current installation has Supporter / Premium entitlement.
	 * Returns true if running from a commercial store (Steam / Google Play Premium)
	 * or if a valid Supporter License Key is active.
	 */
	boolean isSupporter();
}
