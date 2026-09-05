/*
 * Eternity Pixel Dungeon
 * Nemesis System Configuration & Data-Driven Package Loader
 */

package com.shatteredpixel.shatteredpixeldungeon;

import com.badlogic.gdx.files.FileHandle;
import com.watabou.utils.AssetPackResolver;

public class NemesisConfig {

	private static final String CONFIG_PATH = "config/nemesis.json";

	public boolean enabled = false;

	public String titleKey = "windows.wndfallenheroghost.title";
	public String storyKey = "windows.wndfallenheroghost.story";
	public String prayLabelKey = "windows.wndfallenheroghost.pray";
	public String duelLabelKey = "windows.wndfallenheroghost.duel";
	public String prayDoneKey = "windows.wndfallenheroghost.pray_done";
	public String duelStartKey = "windows.wndfallenheroghost.duel_start";
	public String purifiedKey = "actors.fallenheromob.purified";

	public float healRatio = 1.0f;
	public int mobHpBase = 45;
	public int mobHpPerDepth = 8;
	public int mobDefBase = 10;
	public int mobDefPerDepth = 2;
	public int goldRewardMin = 50;
	public int goldRewardMax = 150;

	private static NemesisConfig cachedConfig = null;

	public static NemesisConfig get() {
		if (cachedConfig != null) return cachedConfig;

		NemesisConfig cfg = new NemesisConfig();

		try {
			FileHandle handle = AssetPackResolver.resolveHandle(CONFIG_PATH);
			if (handle != null && handle.exists()) {
				String json = handle.readString("UTF-8");
				if (json != null && !json.trim().isEmpty()) {
					cfg.enabled = parseBool(json, "enabled", true);
					cfg.titleKey = parseString(json, "title_key", cfg.titleKey);
					cfg.storyKey = parseString(json, "story_key", cfg.storyKey);
					cfg.prayLabelKey = parseString(json, "pray_label_key", cfg.prayLabelKey);
					cfg.duelLabelKey = parseString(json, "duel_label_key", cfg.duelLabelKey);
					cfg.prayDoneKey = parseString(json, "pray_done_key", cfg.prayDoneKey);
					cfg.duelStartKey = parseString(json, "duel_start_key", cfg.duelStartKey);
					cfg.purifiedKey = parseString(json, "purified_key", cfg.purifiedKey);
					cfg.healRatio = parseFloat(json, "heal_ratio", cfg.healRatio);
					cfg.mobHpBase = parseInt(json, "mob_hp_base", cfg.mobHpBase);
					cfg.mobHpPerDepth = parseInt(json, "mob_hp_per_depth", cfg.mobHpPerDepth);
					cfg.mobDefBase = parseInt(json, "mob_def_base", cfg.mobDefBase);
					cfg.mobDefPerDepth = parseInt(json, "mob_def_per_depth", cfg.mobDefPerDepth);
					cfg.goldRewardMin = parseInt(json, "gold_reward_min", cfg.goldRewardMin);
					cfg.goldRewardMax = parseInt(json, "gold_reward_max", cfg.goldRewardMax);
				}
			} else {
				cfg.enabled = true;
			}
		} catch (Exception e) {
			cfg.enabled = true;
		}

		cachedConfig = cfg;
		return cfg;
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

	private static String parseString(String json, String key, String def) {
		String val = extractVal(json, key);
		if (val == null) return def;
		if (val.startsWith("\"") && val.endsWith("\"")) {
			return val.substring(1, val.length() - 1);
		}
		return val;
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
