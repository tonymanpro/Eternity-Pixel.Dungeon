/*
 * Eternity Pixel Dungeon
 * Data-Driven Holiday & Seasonal Events Manager
 */

package com.shatteredpixel.shatteredpixeldungeon;

import com.badlogic.gdx.files.FileHandle;
import com.watabou.utils.AssetPackResolver;

import java.util.Calendar;

public class HolidayEventConfig {

	private static final String CONFIG_PATH = "config/holiday_events.json";

	public boolean winterEventEnabled = true;
	public boolean forceActive = false;
	public int activeMonth = 12; // December (1-indexed)
	public boolean santaHatEnabled = true;
	public float giftDropChance = 0.15f;
	public int candyCaneHeal = 10;
	public float potionHealRatio = 0.4f;

	private static HolidayEventConfig cachedConfig = null;

	public static HolidayEventConfig get() {
		if (cachedConfig != null) return cachedConfig;

		HolidayEventConfig cfg = new HolidayEventConfig();

		try {
			FileHandle handle = AssetPackResolver.resolveHandle(CONFIG_PATH);
			if (handle != null && handle.exists()) {
				String json = handle.readString("UTF-8");
				if (json != null && !json.trim().isEmpty()) {
					cfg.winterEventEnabled = parseBool(json, "winter_event_enabled", true);
					cfg.forceActive = parseBool(json, "force_active", false);
					cfg.activeMonth = parseInt(json, "active_month", 12);
					cfg.santaHatEnabled = parseBool(json, "santa_hat_enabled", true);
					cfg.giftDropChance = parseFloat(json, "gift_drop_chance", 0.15f);
					cfg.candyCaneHeal = parseInt(json, "candy_cane_heal", 10);
					cfg.potionHealRatio = parseFloat(json, "potion_heal_ratio", 0.4f);
				}
			}
		} catch (Exception e) {
			cfg.winterEventEnabled = true;
		}

		cachedConfig = cfg;
		return cfg;
	}

	public boolean isWinterEventActive() {
		if (forceActive) return true;
		if (!winterEventEnabled) return false;

		Calendar cal = Calendar.getInstance();
		int currentMonth = cal.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-indexed
		return currentMonth == activeMonth;
	}

	private static boolean parseBool(String json, String key, boolean def) {
		String val = extractVal(json, key);
		return val != null ? Boolean.parseBoolean(val) : def;
	}

	private static int parseInt(String json, String key, int def) {
		String val = extractVal(json, key);
		try {
			return val != null ? Integer.parseInt(val.trim()) : def;
		} catch (Exception e) {
			return def;
		}
	}

	private static float parseFloat(String json, String key, float def) {
		String val = extractVal(json, key);
		try {
			return val != null ? Float.parseFloat(val.trim()) : def;
		} catch (Exception e) {
			return def;
		}
	}

	private static String extractVal(String json, String key) {
		String target = "\"" + key + "\"";
		int idx = json.indexOf(target);
		if (idx == -1) return null;
		int colon = json.indexOf(":", idx);
		if (colon == -1) return null;
		int end = json.indexOf("\n", colon);
		if (end == -1) end = json.indexOf(",", colon);
		if (end == -1) end = json.length();
		String raw = json.substring(colon + 1, end).trim();
		if (raw.endsWith(",")) raw = raw.substring(0, raw.length() - 1).trim();
		return raw;
	}
}
