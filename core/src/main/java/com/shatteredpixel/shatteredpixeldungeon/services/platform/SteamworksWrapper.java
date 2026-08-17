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

import com.watabou.noosa.Game;

/**
 * SteamworksWrapper
 * Decoupled wrapper for Steamworks integration (Steam API).
 * Utilizes dynamic binding to avoid hard compile-time / packaging constraints in FOSS builds.
 */
public class SteamworksWrapper implements PlatformServices {

	private boolean initialized = false;
	private Object steamUserStats = null;
	private Object steamRemoteStorage = null;

	@Override
	public String getPlatformId() {
		return "STEAMWORKS";
	}

	@Override
	public boolean initialize() {
		try {
			// Check if steamworks4j is present in classpath
			Class<?> steamApiClass = Class.forName("com.codedisaster.steamworks.SteamAPI");
			Object initResult = steamApiClass.getMethod("init").invoke(null);
			if (Boolean.TRUE.equals(initResult)) {
				initialized = true;
				try {
					Class<?> statsClass = Class.forName("com.codedisaster.steamworks.SteamUserStats");
					Class<?> callbackClass = Class.forName("com.codedisaster.steamworks.SteamUserStatsCallback");
					// Initialize stats subsystem if available
					steamUserStats = statsClass.getConstructor(callbackClass).newInstance(new Object[]{null});
				} catch (Throwable ignored) {
				}
				return true;
			}
		} catch (Throwable t) {
			// SteamAPI not available in current environment (expected in non-steam builds)
			initialized = false;
		}
		return false;
	}

	@Override
	public void update() {
		if (initialized) {
			try {
				Class<?> steamApiClass = Class.forName("com.codedisaster.steamworks.SteamAPI");
				steamApiClass.getMethod("runCallbacks").invoke(null);
			} catch (Throwable ignored) {
			}
		}
	}

	@Override
	public void dispose() {
		if (initialized) {
			try {
				Class<?> steamApiClass = Class.forName("com.codedisaster.steamworks.SteamAPI");
				steamApiClass.getMethod("shutdown").invoke(null);
			} catch (Throwable ignored) {
			}
			initialized = false;
		}
	}

	@Override
	public boolean isAvailable() {
		return initialized;
	}

	@Override
	public void unlockAchievement(String achievementId) {
		if (!initialized || achievementId == null) return;
		try {
			if (steamUserStats != null) {
				steamUserStats.getClass().getMethod("setAchievement", String.class).invoke(steamUserStats, achievementId);
				steamUserStats.getClass().getMethod("storeStats").invoke(steamUserStats);
			}
		} catch (Throwable t) {
			Game.reportException(t);
		}
	}

	@Override
	public boolean isAchievementUnlocked(String achievementId) {
		if (!initialized || achievementId == null) return false;
		try {
			if (steamUserStats != null) {
				Object res = steamUserStats.getClass().getMethod("getAchievement", String.class, boolean.class)
						.invoke(steamUserStats, achievementId, false);
				return Boolean.TRUE.equals(res);
			}
		} catch (Throwable ignored) {
		}
		return false;
	}

	@Override
	public void setStat(String statName, int value) {
		if (!initialized || statName == null) return;
		try {
			if (steamUserStats != null) {
				steamUserStats.getClass().getMethod("setStatI", String.class, int.class).invoke(steamUserStats, statName, value);
				steamUserStats.getClass().getMethod("storeStats").invoke(steamUserStats);
			}
		} catch (Throwable ignored) {
		}
	}

	@Override
	public int getStat(String statName, int defaultValue) {
		if (!initialized || statName == null) return defaultValue;
		try {
			if (steamUserStats != null) {
				Object res = steamUserStats.getClass().getMethod("getStatI", String.class, int.class)
						.invoke(steamUserStats, statName, defaultValue);
				if (res instanceof Number) {
					return ((Number) res).intValue();
				}
			}
		} catch (Throwable ignored) {
		}
		return defaultValue;
	}

	@Override
	public void setRichPresence(String key, String value) {
		if (!initialized || key == null) return;
		try {
			Class<?> friendsClass = Class.forName("com.codedisaster.steamworks.SteamFriends");
			Object friends = friendsClass.getConstructor().newInstance();
			friendsClass.getMethod("setRichPresence", String.class, String.class).invoke(friends, key, value);
		} catch (Throwable ignored) {
		}
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
