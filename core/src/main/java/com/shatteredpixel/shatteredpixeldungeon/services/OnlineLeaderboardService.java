/*
 * Eternity Pixel Dungeon
 * Online Leaderboard & Hall of Fame Fault-Tolerant Sync Service
 */

package com.shatteredpixel.shatteredpixeldungeon.services;

import com.shatteredpixel.shatteredpixeldungeon.Rankings;
import com.shatteredpixel.shatteredpixeldungeon.VictoryBuild;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.pets.Pet;
import com.watabou.noosa.Game;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class OnlineLeaderboardService {

	private static final String FIRESTORE_ENDPOINT =
			"https://firestore.googleapis.com/v1/projects/eternity-pixel-dungeon/databases/eternitypd/documents/hall_of_fame";

	/**
	 * Submits a victory record to the online Firestore Hall of Fame.
	 * Executes strictly on a background daemon thread with full try-catch isolation
	 * to prevent ANY crash or freeze if the player has no internet connection.
	 */
	public static void submitRecordAsync(final Rankings.Record record, final VictoryBuild build) {
		if (record == null) return;

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					syncRecordToFirestore(record, build);
				} catch (Throwable t) {
					// Safe offline tolerance fallback: log error silently without crashing game
					System.err.println("[OnlineLeaderboardService] Offline / Sync skipped: " + t.getMessage());
				}
			}
		}, "HallOfFame-SyncThread").start();
	}

	private static void syncRecordToFirestore(Rankings.Record record, VictoryBuild build) throws Exception {
		URL url = new URL(FIRESTORE_ENDPOINT);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json; utf-8");
		conn.setRequestProperty("Accept", "application/json");
		conn.setConnectTimeout(5000); // 5s connection timeout
		conn.setReadTimeout(5000);    // 5s read timeout
		conn.setDoOutput(true);

		String heroClass = build != null && !build.heroClass.isEmpty() ? build.heroClass : record.heroClass.name();
		String heroSubclass = build != null ? build.heroSubclass : "";
		String weapon = build != null ? build.weaponName : "";
		String armor = build != null ? build.armorName : "";
		String pet = build != null && !build.petSpecies.isEmpty() ?
				build.petSpecies + " (Niv " + build.petLevel + ")" : "";
		String seed = build != null ? build.seed : "";

		SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
		isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		String timestamp = isoFormat.format(new Date(Game.realTime));

		StringBuilder json = new StringBuilder();
		json.append("{\n");
		json.append("  \"fields\": {\n");
		json.append("    \"hero_class\": {\"stringValue\": \"").append(escapeJson(heroClass)).append("\"},\n");
		json.append("    \"hero_subclass\": {\"stringValue\": \"").append(escapeJson(heroSubclass)).append("\"},\n");
		json.append("    \"level\": {\"integerValue\": \"").append(record.herolevel).append("\"},\n");
		int depth = Math.max(record.depth, build != null ? build.depth : record.depth);
		json.append("    \"depth\": {\"integerValue\": \"").append(depth).append("\"},\n");
		json.append("    \"score\": {\"integerValue\": \"").append(record.score).append("\"},\n");
		json.append("    \"date\": {\"stringValue\": \"").append(escapeJson(record.date)).append("\"},\n");
		json.append("    \"seed\": {\"stringValue\": \"").append(escapeJson(seed)).append("\"},\n");
		json.append("    \"win\": {\"booleanValue\": ").append(record.win).append("},\n");
		json.append("    \"weapon\": {\"stringValue\": \"").append(escapeJson(weapon)).append("\"},\n");
		json.append("    \"armor\": {\"stringValue\": \"").append(escapeJson(armor)).append("\"},\n");
		json.append("    \"pet\": {\"stringValue\": \"").append(escapeJson(pet)).append("\"},\n");
		json.append("    \"game_id\": {\"stringValue\": \"").append(escapeJson(record.gameID)).append("\"},\n");
		json.append("    \"created_at\": {\"timestampValue\": \"").append(timestamp).append("\"}\n");
		json.append("  }\n");
		json.append("}");

		byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
		try (OutputStream os = conn.getOutputStream()) {
			os.write(input, 0, input.length);
		}

		int code = conn.getResponseCode();
		if (code >= 200 && code < 300) {
			System.out.println("[OnlineLeaderboardService] Record successfully synced to Hall of Fame!");
		} else {
			System.err.println("[OnlineLeaderboardService] Firestore returned response code: " + code);
		}
	}

	private static String escapeJson(String text) {
		if (text == null) return "";
		return text.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\b", "\\b")
				.replace("\f", "\\f")
				.replace("\n", "\\n")
				.replace("\r", "\\r")
				.replace("\t", "\\t");
	}
}
