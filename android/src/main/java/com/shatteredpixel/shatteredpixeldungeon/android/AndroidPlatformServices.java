/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.android;

import android.app.Activity;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.services.platform.PlatformServices;
import com.watabou.utils.Callback;

import java.util.HashMap;
import java.util.HashSet;

public class AndroidPlatformServices implements PlatformServices {

	private final Activity activity;
	private AndroidBillingHandler billingHandler;

	private final HashSet<String> unlockedAchievements = new HashSet<>();
	private final HashMap<String, Integer> stats = new HashMap<>();

	public AndroidPlatformServices(Activity activity) {
		this.activity = activity;
		this.billingHandler = new AndroidBillingHandler(activity);
	}

	@Override
	public String getPlatformId() {
		return "GOOGLE_PLAY";
	}

	@Override
	public boolean initialize() {
		if (billingHandler != null) {
			billingHandler.startConnection();
		}
		return true;
	}

	@Override
	public void update() {
	}

	@Override
	public void dispose() {
		if (billingHandler != null) {
			billingHandler.destroy();
			billingHandler = null;
		}
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

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

	@Override
	public boolean isSupporter() {
		return getSupporterTier() > 0;
	}

	@Override
	public int getSupporterTier() {
		return billingHandler != null ? billingHandler.getSupporterTier() : SPDSettings.supporterTier();
	}

	@Override
	public void purchaseSupporter(int tierRank, Callback callback) {
		if (billingHandler != null) {
			billingHandler.purchaseSupporter(tierRank, callback);
		} else if (callback != null) {
			callback.call();
		}
	}

	@Override
	public void restorePurchases(Callback callback) {
		if (billingHandler != null) {
			billingHandler.restorePurchases(callback);
		} else if (callback != null) {
			callback.call();
		}
	}
}
