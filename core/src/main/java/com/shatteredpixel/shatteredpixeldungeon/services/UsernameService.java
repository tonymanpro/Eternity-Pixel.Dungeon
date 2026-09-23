/*
 * Eternity Pixel Dungeon
 * Unique Username & Multi-Device Profile Cloud Sync Service
 */

package com.shatteredpixel.shatteredpixeldungeon.services;

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.services.platform.PlatformManager;
import com.watabou.noosa.Game;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class UsernameService {

	private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");

	public enum Result {
		SUCCESS,
		ALREADY_TAKEN,
		INVALID_FORMAT,
		INVALID_KEY,
		NOT_FOUND,
		NETWORK_ERROR
	}

	public interface Callback {
		void onComplete(Result result, String username, String accountKey, String message);
	}

	public static boolean isValidUsername(String username) {
		if (username == null) return false;
		return USERNAME_PATTERN.matcher(username.trim()).matches();
	}

	/**
	 * Generates a secure multi-device account link key (e.g. EPD-USR-A1B2-C3D4).
	 */
	public static String generateAccountKey() {
		SecureRandom random = new SecureRandom();
		byte[] bytes = new byte[4];
		random.nextBytes(bytes);
		StringBuilder sb = new StringBuilder("EPD-USR-");
		for (int i = 0; i < 4; i++) {
			sb.append(String.format("%02X", bytes[i]));
			if (i == 1) sb.append("-");
		}
		return sb.toString();
	}

	public static String sha256Hex(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
			StringBuilder hexString = new StringBuilder();
			for (byte b : hash) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * Registers a new unique username with Firestore.
	 */
	public static void registerUsernameAsync(final String rawUsername, final Callback callback) {
		if (rawUsername == null || !isValidUsername(rawUsername)) {
			if (callback != null) callback.onComplete(Result.INVALID_FORMAT, rawUsername, null, "Invalid username format.");
			return;
		}

		final String endpoint = CloudConfig.getUsernameEndpoint();
		if (endpoint == null) {
			if (callback != null) callback.onComplete(Result.NETWORK_ERROR, rawUsername, null, "Cloud services not available.");
			return;
		}

		final String username = rawUsername.trim();
		final String usernameLower = username.toLowerCase(Locale.ROOT);

		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpURLConnection conn = null;
				try {
					// 1. Check if document exists
					int checkCode = getDocumentResponseCode(endpoint, usernameLower);
					if (checkCode == 200) {
						if (callback != null) callback.onComplete(Result.ALREADY_TAKEN, username, null, "Username is already taken.");
						return;
					}

					// 2. Generate Account Key
					String accountKey = generateAccountKey();
					String keyHash = sha256Hex(accountKey.trim().toUpperCase(Locale.ROOT));
					String steamId = getPlatformSteamId();

					SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
					isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
					String timestamp = isoFormat.format(new Date(Game.realTime > 0 ? Game.realTime : System.currentTimeMillis()));

					// 3. Create document in Firestore
					URL url = new URL(endpoint + "?documentId=" + usernameLower);
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type", "application/json; utf-8");
					conn.setRequestProperty("Accept", "application/json");
					conn.setConnectTimeout(4500);
					conn.setReadTimeout(4500);
					conn.setDoOutput(true);

					StringBuilder json = new StringBuilder();
					json.append("{\n");
					json.append("  \"fields\": {\n");
					json.append("    \"username\": {\"stringValue\": \"").append(escapeJson(username)).append("\"},\n");
					json.append("    \"username_lower\": {\"stringValue\": \"").append(escapeJson(usernameLower)).append("\"},\n");
					json.append("    \"account_key_hash\": {\"stringValue\": \"").append(escapeJson(keyHash)).append("\"},\n");
					json.append("    \"steam_id\": {\"stringValue\": \"").append(escapeJson(steamId)).append("\"},\n");
					json.append("    \"created_at\": {\"timestampValue\": \"").append(timestamp).append("\"},\n");
					json.append("    \"last_active\": {\"timestampValue\": \"").append(timestamp).append("\"}\n");
					json.append("  }\n");
					json.append("}");

					byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
					try (OutputStream os = conn.getOutputStream()) {
						os.write(input, 0, input.length);
					}

					int responseCode = conn.getResponseCode();
					if (responseCode >= 200 && responseCode < 300) {
						SPDSettings.customUsername(username);
						SPDSettings.accountKey(accountKey);
						if (callback != null) callback.onComplete(Result.SUCCESS, username, accountKey, "Username registered successfully.");
					} else if (responseCode == 409) {
						if (callback != null) callback.onComplete(Result.ALREADY_TAKEN, username, null, "Username was taken concurrently.");
					} else {
						if (callback != null) callback.onComplete(Result.NETWORK_ERROR, username, null, "Server error code: " + responseCode);
					}

				} catch (Throwable t) {
					if (callback != null) callback.onComplete(Result.NETWORK_ERROR, username, null, t.getMessage());
				} finally {
					if (conn != null) {
						try { conn.disconnect(); } catch (Throwable ignored) {}
					}
				}
			}
		}, "Username-RegisterThread").start();
	}

	/**
	 * Links an existing username on a new device using the Account Link Key.
	 */
	public static void linkExistingUsernameAsync(final String rawUsername, final String rawKey, final Callback callback) {
		if (rawUsername == null || rawUsername.trim().isEmpty() || rawKey == null || rawKey.trim().isEmpty()) {
			if (callback != null) callback.onComplete(Result.INVALID_FORMAT, rawUsername, null, "Username and key required.");
			return;
		}

		final String endpoint = CloudConfig.getUsernameEndpoint();
		if (endpoint == null) {
			if (callback != null) callback.onComplete(Result.NETWORK_ERROR, rawUsername, null, "Cloud services not available.");
			return;
		}

		final String username = rawUsername.trim();
		final String usernameLower = username.toLowerCase(Locale.ROOT);
		final String cleanKey = rawKey.trim().toUpperCase(Locale.ROOT);

		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpURLConnection conn = null;
				try {
					URL url = new URL(endpoint + "/" + usernameLower);
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.setRequestProperty("Accept", "application/json");
					conn.setConnectTimeout(4500);
					conn.setReadTimeout(4500);

					int code = conn.getResponseCode();
					if (code == 404) {
						if (callback != null) callback.onComplete(Result.NOT_FOUND, username, null, "Username does not exist.");
						return;
					} else if (code < 200 || code >= 300) {
						if (callback != null) callback.onComplete(Result.NETWORK_ERROR, username, null, "Server error code: " + code);
						return;
					}

					String responseBody = readStream(conn.getInputStream());
					String storedHash = extractJsonStringField(responseBody, "account_key_hash");
					String originalDisplayName = extractJsonStringField(responseBody, "username");
					if (originalDisplayName == null || originalDisplayName.isEmpty()) {
						originalDisplayName = username;
					}

					String providedKeyHash = sha256Hex(cleanKey);
					if (storedHash != null && storedHash.equalsIgnoreCase(providedKeyHash)) {
						// Key verified successfully!
						SPDSettings.customUsername(originalDisplayName);
						SPDSettings.accountKey(cleanKey);
						if (callback != null) callback.onComplete(Result.SUCCESS, originalDisplayName, cleanKey, "Account successfully linked.");
					} else {
						if (callback != null) callback.onComplete(Result.INVALID_KEY, username, null, "Invalid account link key.");
					}

				} catch (Throwable t) {
					if (callback != null) callback.onComplete(Result.NETWORK_ERROR, username, null, t.getMessage());
				} finally {
					if (conn != null) {
						try { conn.disconnect(); } catch (Throwable ignored) {}
					}
				}
			}
		}, "Username-LinkThread").start();
	}

	private static int getDocumentResponseCode(String endpoint, String usernameLower) {
		HttpURLConnection conn = null;
		try {
			URL url = new URL(endpoint + "/" + usernameLower);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(4000);
			conn.setReadTimeout(4000);
			return conn.getResponseCode();
		} catch (Throwable t) {
			return -1;
		} finally {
			if (conn != null) {
				try { conn.disconnect(); } catch (Throwable ignored) {}
			}
		}
	}

	private static String getPlatformSteamId() {
		try {
			if (PlatformManager.get() != null) {
				String steamId = PlatformManager.get().getPlatformUserId();
				return steamId != null ? steamId : "";
			}
		} catch (Throwable ignored) {}
		return "";
	}

	private static String readStream(InputStream is) throws Exception {
		if (is == null) return "";
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			return sb.toString();
		}
	}

	private static String extractJsonStringField(String json, String fieldName) {
		if (json == null) return "";
		String search = "\"" + fieldName + "\"";
		int idx = json.indexOf(search);
		if (idx == -1) return "";
		int stringValIdx = json.indexOf("\"stringValue\"", idx);
		if (stringValIdx == -1) return "";
		int colonIdx = json.indexOf(":", stringValIdx + 13);
		if (colonIdx == -1) return "";
		int firstQuote = json.indexOf("\"", colonIdx);
		if (firstQuote == -1) return "";
		int secondQuote = json.indexOf("\"", firstQuote + 1);
		if (secondQuote == -1) return "";
		return json.substring(firstQuote + 1, secondQuote);
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
