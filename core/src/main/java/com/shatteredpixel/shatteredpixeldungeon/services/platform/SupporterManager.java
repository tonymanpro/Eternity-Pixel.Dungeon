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

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.services.CloudConfig;
import com.shatteredpixel.shatteredpixeldungeon.services.UsernameService;

import com.watabou.noosa.Game;
import com.watabou.utils.Callback;
import com.watabou.utils.DeviceCompat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SupporterManager {

	public enum SupporterTier {
		NONE(0, "tier_none", 0xFFFFFF, "supporter_none"),
		GOLD(1, "tier_gold", 0xFFD700, "supporter_gold");

		public final int rank;
		public final String key;
		public final int color;
		public final String sku;

		SupporterTier(int rank, String key, int color, String sku) {
			this.rank = rank;
			this.key = key;
			this.color = color;
			this.sku = sku;
		}

		public String displayName() {
			return Messages.get(SupporterManager.class, key);
		}

		public static SupporterTier fromRank(int rank) {
			return rank > 0 ? GOLD : NONE;
		}
	}

	// Cryptographically obfuscated salt via XOR masking to prevent plain-text string extraction
	private static final byte[] _SALT_M = new byte[]{ 0x1F, 0x6B, 0x3B, 0x7E, (byte)0xC5, 0x5D, (byte)0xC9, 0x38, 0x77, 0x6F, 0x3A, 0x01, (byte)0xD8, 0x41, (byte)0xCD, 0x31, 0x15, 0x6D, 0x2A, 0x69, (byte)0xD9, 0x39, (byte)0xAF, 0x51, 0x68, 0x09, 0x53, 0x78, (byte)0xC4, 0x5F, (byte)0xD8, 0x2F, 0x77, 0x6C, 0x3F, 0x60, (byte)0xDF, 0x39, (byte)0xCB, 0x50 };
	private static final byte[] _SALT_K = new byte[]{ 0x5A, 0x3F, 0x7E, 0x2C, (byte)0x8B, 0x14, (byte)0x9D, 0x61 };

	public static String getSecretSalt() {
		byte[] out = new byte[_SALT_M.length];
		for (int i = 0; i < _SALT_M.length; i++) {
			out[i] = (byte) (_SALT_M[i] ^ _SALT_K[i % _SALT_K.length]);
		}
		return new String(out, StandardCharsets.UTF_8);
	}

	/**
	 * Returns true if the game is running as an authenticated Steam release.
	 */
	public static boolean isSteam() {
		PlatformServices platform = PlatformManager.get();
		return platform != null && platform.isAvailable() && "STEAMWORKS".equalsIgnoreCase(platform.getPlatformId());
	}

	/**
	 * Returns true if running on Android / Google Play build.
	 */
	public static boolean isGooglePlay() {
		PlatformServices platform = PlatformManager.get();
		if (platform != null && "GOOGLE_PLAY".equalsIgnoreCase(platform.getPlatformId())) {
			return true;
		}
		return DeviceCompat.isAndroid();
	}

	/**
	 * Checks if the player has Supporter / Premium access via any verified channel:
	 * 1. Steam (running with Steamworks backend)
	 * 2. Google Play In-App Purchase (Full Unlock Product ID '01')
	 * 3. Verified Device-Bound Token
	 * 4. Verified Algorithmic License Key (with or without username)
	 */
	public static boolean isSupporter() {
		return getActiveTier() != SupporterTier.NONE;
	}

	/**
	 * Returns the active Supporter Tier (NONE, BRONZE, SILVER, GOLD, PLATINUM).
	 */
	public static SupporterTier getActiveTier() {
		// 1. Steam check: Running on Steam gives automatic Gold supporter entitlement
		if (isSteam()) {
			return SupporterTier.GOLD;
		}

		// 2. Platform native supporter check (e.g. Google Play In-App Purchase "01" / "full_unlock")
		PlatformServices platform = PlatformManager.get();
		if (platform != null && platform.getSupporterTier() > 0) {
			return SupporterTier.GOLD;
		}

		// 3. Device-bound activation token check
		String token = SPDSettings.supporterToken();
		if (token != null && !token.isEmpty() && isTokenValid(token)) {
			return getTierFromToken(token);
		}

		// 4. Local validated Supporter / Direct License Key + Username
		String key = SPDSettings.supporterKey();
		String user = SPDSettings.supporterUsername();
		if (user != null && !user.isEmpty()) {
			if (isLicenseValid(user, key)) {
				return getTierFromKey(key);
			}
		} else if (isKeyValid(key)) {
			return getTierFromKey(key);
		}

		// 5. Stored supporter tier check (cloud sync / restore)
		if (SPDSettings.supporterTier() > 0) {
			return SupporterTier.GOLD;
		}

		return SupporterTier.NONE;
	}

	public static void purchase(SupporterTier tier, Callback callback) {
		PlatformServices platform = PlatformManager.get();
		if (platform != null) {
			platform.purchaseSupporter(tier.rank, callback);
		} else if (callback != null) {
			callback.call();
		}
	}

	public static void restore(Callback callback) {
		PlatformServices platform = PlatformManager.get();
		if (platform != null) {
			platform.restorePurchases(callback);
		} else if (callback != null) {
			callback.call();
		}
	}

	/**
	 * Validates a formatted Supporter or Patreon license key.
	 * Standard format: EPD-XXXX-XXXX-XXXX
	 */
	public static boolean isKeyValid(String key) {
		if (key == null) return false;
		String cleanKey = key.trim().toUpperCase(Locale.ROOT);
		if (cleanKey.isEmpty()) return false;

		// Standard Key: EPD-XXXX-XXXX-XXXX or EPD-(PREFIX)-XXXX-XXXX-XXXX
		if (cleanKey.matches("^EPD(-[A-Z0-9]+)?(-[A-Z0-9]{4}){2,3}$")) {
			return verifyKeyChecksum(cleanKey);
		}

		// Extended token with valid modulus hash
		if (cleanKey.length() >= 16 && cleanKey.matches("^[A-Z0-9-]+$")) {
			return verifyHashToken(cleanKey);
		}

		return false;
	}

	/**
	 * Generates a device-unique identifier based on hardware and environment properties.
	 */
	public static String getDeviceId() {
		try {
			String user = System.getProperty("user.name", "unknown");
			String os = System.getProperty("os.name", "unknown");
			String arch = System.getProperty("os.arch", "unknown");
			String computer = System.getenv("COMPUTERNAME");
			if (computer == null) computer = System.getenv("HOSTNAME");
			if (computer == null) computer = "localhost";

			String raw = user + ":" + os + ":" + arch + ":" + computer;
			String hash = sha256Hex(raw).toUpperCase(Locale.ROOT);
			return hash.length() >= 16 ? hash.substring(0, 16) : hash;
		} catch (Throwable ignored) {
			return "DEV-GENERIC-0001";
		}
	}

	/**
	 * Generates a signed activation token bound to a specific device.
	 */
	public static String generateDeviceToken(String key, String deviceId) {
		String salt = getSecretSalt();
		String keyHash = sha256Hex(key + ":" + salt).substring(0, 8).toUpperCase(Locale.ROOT);
		String devPart = (deviceId.length() >= 8 ? deviceId.substring(0, 8) : deviceId).toUpperCase(Locale.ROOT);
		String rawSign = "SUPPORTER:" + keyHash + ":" + devPart + ":" + salt;
		String signature = sha256Hex(rawSign).substring(0, 8).toUpperCase(Locale.ROOT);
		return "EPDTOK-SUPPORTER-" + keyHash + "-" + devPart + "-" + signature;
	}

	/**
	 * Verifies whether a device token is genuine and bound to this physical machine.
	 */
	public static boolean isTokenValid(String token) {
		try {
			if (token == null || !token.startsWith("EPDTOK-")) return false;
			String[] parts = token.split("-");
			if (parts.length < 5) return false;

			String tierName = parts[1];
			String keyHash = parts[2];
			String devPart = parts[3];
			String signature = parts[4];

			String currentDev = getDeviceId().toUpperCase(Locale.ROOT);
			String expectedDevPart = currentDev.length() >= 8 ? currentDev.substring(0, 8) : currentDev;

			// Verify device binding match
			if (!devPart.equalsIgnoreCase(expectedDevPart)) {
				return false;
			}

			// Verify cryptographic signature
			String rawSign = tierName + ":" + keyHash + ":" + devPart + ":" + getSecretSalt();
			String expectedSignature = sha256Hex(rawSign).substring(0, 8).toUpperCase(Locale.ROOT);
			return expectedSignature.equalsIgnoreCase(signature);
		} catch (Throwable ignored) {
			return false;
		}
	}

	public static SupporterTier getTierFromToken(String token) {
		if (!isTokenValid(token)) return SupporterTier.NONE;
		return SupporterTier.GOLD;
	}

	public static SupporterTier getTierFromKey(String key) {
		if (!isKeyValid(key)) return SupporterTier.NONE;
		return SupporterTier.GOLD;
	}

	/**
	 * Generates a valid key for a given patron seed (email or ID).
	 * Formats: EPD-XXXX-XXXX-XXXX
	 */
	public static String generateKey(String patronSeed) {
		return generateKey(patronSeed, "EPD");
	}

	public static String generateKey(String patronSeed, String tierPrefix) {
		if (tierPrefix == null || tierPrefix.isEmpty()) tierPrefix = "EPD";
		String raw = tierPrefix + ":" + patronSeed.toUpperCase(Locale.ROOT) + ":" + getSecretSalt();
		String hash = sha256Hex(raw).toUpperCase(Locale.ROOT);
		String part1 = hash.substring(0, 4);
		String part2 = hash.substring(4, 8);
		String prefix = tierPrefix + "-" + part1 + "-" + part2;
		int sum = 0;
		for (char c : prefix.toCharArray()) {
			if (c != '-') sum += c;
		}
		String checksum = String.format(Locale.ROOT, "%04X", sum % 0xFFFF);
		return prefix + "-" + checksum;
	}

	private static boolean verifyKeyChecksum(String key) {
		try {
			int lastHyphen = key.lastIndexOf('-');
			if (lastHyphen <= 0) return false;
			String prefix = key.substring(0, lastHyphen);
			String checksumPart = key.substring(lastHyphen + 1);
			int expectedSum = 0;
			for (char c : prefix.toCharArray()) {
				if (c != '-') expectedSum += c;
			}
			String expectedChecksum = String.format(Locale.ROOT, "%04X", expectedSum % 0xFFFF);
			return expectedChecksum.equalsIgnoreCase(checksumPart);
		} catch (Throwable ignored) {
			return false;
		}
	}

	private static boolean verifyHashToken(String token) {
		try {
			String clean = token.replace("-", "");
			if (clean.length() < 12) return false;
			int sum = 0;
			for (int i = 0; i < clean.length(); i++) {
				sum += clean.charAt(i) * (i + 1);
			}
			return (sum % 7) == 0;
		} catch (Throwable ignored) {
			return false;
		}
	}

	/**
	 * Validates a license key bound to a specific username or email.
	 */
	public static boolean isLicenseValid(String username, String key) {
		if (key == null) return false;
		String cleanKey = key.trim().toUpperCase(Locale.ROOT);
		if (cleanKey.isEmpty()) return false;

		if (username == null || username.trim().isEmpty()) {
			return isKeyValid(cleanKey);
		}

		String cleanUser = username.trim();
		String[] prefixes = {"EPD", "EPD-GOLD", "EPD-PLAT", "EPD-SILV", "EPD-BRON", "EPD-FULL"};
		for (String prefix : prefixes) {
			if (cleanKey.equalsIgnoreCase(generateKey(cleanUser.toLowerCase(Locale.ROOT), prefix))) {
				return true;
			}
			if (cleanKey.equalsIgnoreCase(generateKey(cleanUser.toUpperCase(Locale.ROOT), prefix))) {
				return true;
			}
			if (cleanKey.equalsIgnoreCase(generateKey(cleanUser, prefix))) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Attempts to activate a license with username and key.
	 */
	public static boolean activateLicense(String username, String key) {
		if (key == null) return false;
		String cleanKey = key.trim().toUpperCase(Locale.ROOT);
		String cleanUser = username != null ? username.trim() : "";

		if (!cleanUser.isEmpty()) {
			if (isLicenseValid(cleanUser, cleanKey)) {
				SPDSettings.supporterUsername(cleanUser);
				SPDSettings.supporterKey(cleanKey);
				String deviceToken = generateDeviceToken(cleanKey, getDeviceId());
				SPDSettings.supporterToken(deviceToken);
				SPDSettings.supporterTier(SupporterTier.GOLD.rank);
				return true;
			}
		} else if (isKeyValid(cleanKey)) {
			SPDSettings.supporterUsername("");
			SPDSettings.supporterKey(cleanKey);
			String deviceToken = generateDeviceToken(cleanKey, getDeviceId());
			SPDSettings.supporterToken(deviceToken);
			SPDSettings.supporterTier(SupporterTier.GOLD.rank);
			return true;
		}

		return false;
	}

	/**
	 * Attempts to activate a license key, creates a device-bound token and stores it in settings.
	 */
	public static boolean activateKey(String key) {
		return activateLicense(null, key);
	}

	/**
	 * Deactivates the currently stored supporter key and device token.
	 */
	public static void deactivate() {
		SPDSettings.supporterUsername("");
		SPDSettings.supporterKey("");
		SPDSettings.supporterToken("");
		SPDSettings.supporterTier(0);
	}

	public enum RedeemResult {
		SUCCESS,
		ALREADY_CLAIMED,
		DEVICE_LIMIT_REACHED,
		INVALID_KEY,
		INVALID_USERNAME,
		NETWORK_REQUIRED,
		SERVER_ERROR
	}

	public interface RedeemCallback {
		void onComplete(RedeemResult result, String message);
	}

	/**
	 * Validates, redeems, and permanently burns a license key in Firestore.
	 * Binds the license to the specified username and registers the current device ID.
	 */
	public static void redeemAndBurnLicenseAsync(final String rawUsername, final String rawKey, final RedeemCallback callback) {
		if (rawUsername == null || rawUsername.trim().isEmpty() || !rawUsername.trim().matches("^[a-zA-Z0-9_]{3,16}$")) {
			if (callback != null) callback.onComplete(RedeemResult.INVALID_USERNAME, "Nombre de usuario inválido. Debe tener entre 3 y 16 caracteres alfanuméricos.");
			return;
		}
		if (rawKey == null || !isKeyValid(rawKey)) {
			if (callback != null) callback.onComplete(RedeemResult.INVALID_KEY, "Clave de licencia inválida.");
			return;
		}

		final String cleanUser = rawUsername.trim();
		final String cleanKey = rawKey.trim().toUpperCase(Locale.ROOT);
		final String keyHash = sha256Hex(cleanKey);
		final String deviceId = getDeviceId().toUpperCase(Locale.ROOT);

		final String endpoint = CloudConfig.getLicensesEndpoint();
		if (endpoint == null) {
			if (callback != null) callback.onComplete(RedeemResult.NETWORK_REQUIRED, "Se requiere conexión a internet para verificar y vincular tu licencia en la nube.");
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpURLConnection conn = null;
				try {
					URL url = new URL(endpoint + "/" + keyHash);
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.setRequestProperty("Accept", "application/json");
					conn.setConnectTimeout(5000);
					conn.setReadTimeout(5000);

					int code = conn.getResponseCode();
					if (code == 200) {
						String responseBody = readStream(conn.getInputStream());
						boolean used = extractJsonBooleanField(responseBody, "used");
						String claimedBy = extractJsonStringField(responseBody, "claimed_by");
						if (claimedBy.isEmpty()) {
							claimedBy = extractJsonStringField(responseBody, "username");
						}
						int maxDevices = extractJsonIntegerField(responseBody, "max_devices");
						if (maxDevices <= 0) maxDevices = 3;
						ArrayList<String> deviceIds = extractJsonStringArray(responseBody, "device_ids");

						if (used) {
							if (!claimedBy.equalsIgnoreCase(cleanUser)) {
								if (callback != null) callback.onComplete(RedeemResult.ALREADY_CLAIMED, "Esta licencia ya fue activada y vinculada a otra cuenta.");
								return;
							}

							boolean deviceFound = false;
							for (String dev : deviceIds) {
								if (dev.equalsIgnoreCase(deviceId)) {
									deviceFound = true;
									break;
								}
							}

							if (!deviceFound) {
								if (deviceIds.size() >= maxDevices) {
									if (callback != null) callback.onComplete(RedeemResult.DEVICE_LIMIT_REACHED, "Esta licencia ya alcanzó el límite máximo de " + maxDevices + " dispositivos permitidos.");
									return;
								}
								deviceIds.add(deviceId);
								updateLicenseDevices(endpoint, keyHash, deviceIds);
							}
						} else {
							// Unused key: Burn it now!
							if (!burnExistingLicense(endpoint, keyHash, cleanUser, deviceId, maxDevices)) {
								if (callback != null) callback.onComplete(RedeemResult.SERVER_ERROR, "No se pudo registrar la licencia en la nube.");
								return;
							}
						}
					} else if (code == 404) {
						// Itch.io batch key used for the first time: Create and burn document
						if (!createNewBurnedLicense(endpoint, keyHash, cleanKey, cleanUser, deviceId, 3)) {
							if (callback != null) callback.onComplete(RedeemResult.SERVER_ERROR, "No se pudo registrar la licencia en la nube.");
							return;
						}
					} else {
						if (callback != null) callback.onComplete(RedeemResult.SERVER_ERROR, "Error de comunicación con el servidor (Código: " + code + ").");
						return;
					}

					// Successfully verified and burned in Firestore!
					SPDSettings.supporterUsername(cleanUser);
					SPDSettings.supporterKey(cleanKey);
					String deviceToken = generateDeviceToken(cleanKey, getDeviceId());
					SPDSettings.supporterToken(deviceToken);
					SPDSettings.supporterTier(SupporterTier.GOLD.rank);

					if (SPDSettings.customUsername().isEmpty()) {
						SPDSettings.customUsername(cleanUser);
					}

					SPDSettings.accountKey(cleanKey);

					// Always sync and guarantee username, username_lower, and created_at in /usernames
					UsernameService.syncSupporterStatusAsync(cleanUser);

					if (callback != null) callback.onComplete(RedeemResult.SUCCESS, "¡Licencia Gold activada y vinculada con éxito a @" + cleanUser + "!");

				} catch (Throwable t) {
					if (callback != null) callback.onComplete(RedeemResult.NETWORK_REQUIRED, "Se requiere conexión a internet para verificar y vincular tu licencia en la nube.");
				} finally {
					if (conn != null) {
						try { conn.disconnect(); } catch (Throwable ignored) {}
					}
				}
			}
		}, "License-RedeemBurnThread").start();
	}

	private static boolean burnExistingLicense(String endpoint, String keyHash, String username, String deviceId, int maxDevices) {
		HttpURLConnection conn = null;
		try {
			SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
			isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			String timestamp = isoFormat.format(new Date(Game.realTime > 0 ? Game.realTime : System.currentTimeMillis()));
			String usernameLower = username.toLowerCase(Locale.ROOT);

			String urlStr = endpoint + "/" + keyHash +
					"?updateMask.fieldPaths=used" +
					"&updateMask.fieldPaths=username" +
					"&updateMask.fieldPaths=username_lower" +
					"&updateMask.fieldPaths=claimed_by" +
					"&updateMask.fieldPaths=claimed_at" +
					"&updateMask.fieldPaths=updated_at" +
					"&updateMask.fieldPaths=device_ids";

			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
			conn.setRequestProperty("Content-Type", "application/json; utf-8");
			conn.setRequestProperty("Accept", "application/json");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			conn.setDoOutput(true);

			StringBuilder json = new StringBuilder();
			json.append("{\n");
			json.append("  \"fields\": {\n");
			json.append("    \"used\": {\"booleanValue\": true},\n");
			json.append("    \"username\": {\"stringValue\": \"").append(escapeJson(username)).append("\"},\n");
			json.append("    \"username_lower\": {\"stringValue\": \"").append(escapeJson(usernameLower)).append("\"},\n");
			json.append("    \"claimed_by\": {\"stringValue\": \"").append(escapeJson(username)).append("\"},\n");
			json.append("    \"claimed_at\": {\"timestampValue\": \"").append(timestamp).append("\"},\n");
			json.append("    \"updated_at\": {\"timestampValue\": \"").append(timestamp).append("\"},\n");
			json.append("    \"device_ids\": {\"arrayValue\": {\"values\": [{\"stringValue\": \"").append(escapeJson(deviceId)).append("\"}]}}\n");
			json.append("  }\n");
			json.append("}");

			byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
			try (OutputStream os = conn.getOutputStream()) {
				os.write(input, 0, input.length);
			}

			int resp = conn.getResponseCode();
			return resp >= 200 && resp < 300;
		} catch (Throwable t) {
			return false;
		} finally {
			if (conn != null) {
				try { conn.disconnect(); } catch (Throwable ignored) {}
			}
		}
	}

	private static boolean createNewBurnedLicense(String endpoint, String keyHash, String key, String username, String deviceId, int maxDevices) {
		HttpURLConnection conn = null;
		try {
			SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
			isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			String timestamp = isoFormat.format(new Date(Game.realTime > 0 ? Game.realTime : System.currentTimeMillis()));
			String usernameLower = username.toLowerCase(Locale.ROOT);

			URL url = new URL(endpoint + "?documentId=" + keyHash);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json; utf-8");
			conn.setRequestProperty("Accept", "application/json");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			conn.setDoOutput(true);

			StringBuilder json = new StringBuilder();
			json.append("{\n");
			json.append("  \"fields\": {\n");
			json.append("    \"license_key\": {\"stringValue\": \"").append(escapeJson(key)).append("\"},\n");
			json.append("    \"key_hash\": {\"stringValue\": \"").append(escapeJson(keyHash)).append("\"},\n");
			json.append("    \"username\": {\"stringValue\": \"").append(escapeJson(username)).append("\"},\n");
			json.append("    \"username_lower\": {\"stringValue\": \"").append(escapeJson(usernameLower)).append("\"},\n");
			json.append("    \"used\": {\"booleanValue\": true},\n");
			json.append("    \"claimed_by\": {\"stringValue\": \"").append(escapeJson(username)).append("\"},\n");
			json.append("    \"claimed_at\": {\"timestampValue\": \"").append(timestamp).append("\"},\n");
			json.append("    \"created_at\": {\"timestampValue\": \"").append(timestamp).append("\"},\n");
			json.append("    \"tier\": {\"stringValue\": \"GOLD\"},\n");
			json.append("    \"source\": {\"stringValue\": \"ITCH_OR_DIRECT\"},\n");
			json.append("    \"max_devices\": {\"integerValue\": \"").append(maxDevices).append("\"},\n");
			json.append("    \"device_ids\": {\"arrayValue\": {\"values\": [{\"stringValue\": \"").append(escapeJson(deviceId)).append("\"}]}}\n");
			json.append("  }\n");
			json.append("}");

			byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
			try (OutputStream os = conn.getOutputStream()) {
				os.write(input, 0, input.length);
			}

			int resp = conn.getResponseCode();
			return resp >= 200 && resp < 300;
		} catch (Throwable t) {
			return false;
		} finally {
			if (conn != null) {
				try { conn.disconnect(); } catch (Throwable ignored) {}
			}
		}
	}

	private static void updateLicenseDevices(String endpoint, String keyHash, ArrayList<String> deviceIds) {
		HttpURLConnection conn = null;
		try {
			String urlStr = endpoint + "/" + keyHash + "?updateMask.fieldPaths=device_ids";
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
			conn.setRequestProperty("Content-Type", "application/json; utf-8");
			conn.setRequestProperty("Accept", "application/json");
			conn.setConnectTimeout(4000);
			conn.setReadTimeout(4000);
			conn.setDoOutput(true);

			StringBuilder json = new StringBuilder();
			json.append("{\n");
			json.append("  \"fields\": {\n");
			json.append("    \"device_ids\": {\"arrayValue\": {\"values\": [");
			for (int i = 0; i < deviceIds.size(); i++) {
				if (i > 0) json.append(",");
				json.append("{\"stringValue\": \"").append(escapeJson(deviceIds.get(i))).append("\"}");
			}
			json.append("]}}\n");
			json.append("  }\n");
			json.append("}");

			byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
			try (OutputStream os = conn.getOutputStream()) {
				os.write(input, 0, input.length);
			}
			conn.getResponseCode();
		} catch (Throwable ignored) {
		} finally {
			if (conn != null) {
				try { conn.disconnect(); } catch (Throwable ignored) {}
			}
		}
	}

	private static String escapeJson(String s) {
		if (s == null) return "";
		return s.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\b", "\\b")
				.replace("\f", "\\f")
				.replace("\n", "\\n")
				.replace("\r", "\\r")
				.replace("\t", "\\t");
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

	private static boolean extractJsonBooleanField(String json, String fieldName) {
		if (json == null) return false;
		String search = "\"" + fieldName + "\"";
		int idx = json.indexOf(search);
		if (idx == -1) return false;
		int boolValIdx = json.indexOf("\"booleanValue\"", idx);
		if (boolValIdx == -1 || boolValIdx - idx > 60) return false;
		int colonIdx = json.indexOf(":", boolValIdx + 14);
		if (colonIdx == -1) return false;
		String rest = json.substring(colonIdx + 1).trim();
		return rest.startsWith("true");
	}

	private static int extractJsonIntegerField(String json, String fieldName) {
		if (json == null) return 0;
		String search = "\"" + fieldName + "\"";
		int idx = json.indexOf(search);
		if (idx == -1) return 0;
		int intValIdx = json.indexOf("\"integerValue\"", idx);
		if (intValIdx == -1 || intValIdx - idx > 60) return 0;
		int colonIdx = json.indexOf(":", intValIdx + 14);
		if (colonIdx == -1) return 0;
		int firstQuote = json.indexOf("\"", colonIdx);
		if (firstQuote == -1) return 0;
		int secondQuote = json.indexOf("\"", firstQuote + 1);
		if (secondQuote == -1) return 0;
		try {
			return Integer.parseInt(json.substring(firstQuote + 1, secondQuote));
		} catch (Exception e) {
			return 0;
		}
	}

	private static ArrayList<String> extractJsonStringArray(String json, String fieldName) {
		ArrayList<String> list = new ArrayList<>();
		if (json == null) return list;
		int idx = json.indexOf("\"" + fieldName + "\"");
		if (idx == -1) return list;
		int arrayIdx = json.indexOf("\"arrayValue\"", idx);
		if (arrayIdx == -1 || arrayIdx - idx > 80) return list;
		int valuesIdx = json.indexOf("\"values\"", arrayIdx);
		if (valuesIdx == -1) return list;
		int endBracket = json.indexOf("]", valuesIdx);
		if (endBracket == -1) return list;
		String slice = json.substring(valuesIdx, endBracket);
		int p = 0;
		while ((p = slice.indexOf("\"stringValue\"", p)) != -1) {
			int q1 = slice.indexOf("\"", p + 13);
			if (q1 != -1) {
				int colon = slice.indexOf(":", p + 13);
				int strStart = slice.indexOf("\"", colon + 1);
				int strEnd = slice.indexOf("\"", strStart + 1);
				if (strStart != -1 && strEnd != -1) {
					list.add(slice.substring(strStart + 1, strEnd));
					p = strEnd + 1;
					continue;
				}
			}
			p += 13;
		}
		return list;
	}

	private static String sha256Hex(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder();
			for (byte b : digest) {
				hex.append(String.format("%02x", b));
			}
			return hex.toString();
		} catch (Exception e) {
			return Integer.toHexString(input.hashCode());
		}
	}
}
