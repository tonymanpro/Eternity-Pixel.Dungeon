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

/**
 * NullPlatformServices
 * Default No-Op implementation of PlatformServices for standalone, FOSS, or dev builds.
 */
public class NullPlatformServices implements PlatformServices {

	public static final NullPlatformServices INSTANCE = new NullPlatformServices();

	@Override
	public String getPlatformId() {
		return "STANDALONE";
	}

	@Override
	public boolean initialize() {
		return true;
	}

	@Override
	public void update() {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isAvailable() {
		return false;
	}

	private final java.util.HashSet<String> unlockedAchievements = new java.util.HashSet<>();
	private final java.util.HashMap<String, Integer> stats = new java.util.HashMap<>();

	@Override
	public void unlockAchievement(String achievementId) {
		if (achievementId != null) {
			unlockedAchievements.add(achievementId);
		}
	}

	@Override
	public boolean isAchievementUnlocked(String achievementId) {
		return achievementId != null && unlockedAchievements.contains(achievementId);
	}

	@Override
	public void setStat(String statName, int value) {
		if (statName != null) {
			stats.put(statName, value);
		}
	}

	@Override
	public int getStat(String statName, int defaultValue) {
		return statName != null && stats.containsKey(statName) ? stats.get(statName) : defaultValue;
	}

	@Override
	public void setRichPresence(String key, String value) {
	}

	@Override
	public boolean saveToCloud(String fileName, byte[] data) {
		return false;
	}

	@Override
	public byte[] loadFromCloud(String fileName) {
		return null;
	}
}
