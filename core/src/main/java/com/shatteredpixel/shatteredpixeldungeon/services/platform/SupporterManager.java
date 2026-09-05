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

import com.watabou.utils.Callback;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

public class SupporterManager {

	public enum SupporterTier {
		NONE(0, "tier_none", 0xFFFFFF, "supporter_none"),
		BRONZE(1, "tier_bronze", 0xCD7F32, "supporter_bronze"),
		SILVER(2, "tier_silver", 0xC0C0C0, "supporter_silver"),
		GOLD(3, "tier_gold", 0xFFD700, "supporter_gold"),
		PLATINUM(4, "tier_platinum", 0x00FFFF, "supporter_platinum");

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
			for (SupporterTier t : values()) {
				if (t.rank == rank) return t;
			}
			return rank > 0 ? PLATINUM : NONE;
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
	 * Checks if the player has Supporter / Premium access via any verified channel:
	 * 1. Steam (running with Steamworks backend)
	 * 2. In-App Purchase / Google Play Premium
	 * 3. Verified Device-Bound Token
	 * 4. Verified Algorithmic License Key
	 */
	public static boolean isSupporter() {
		return getActiveTier() != SupporterTier.NONE;
	}

	/**
	 * Returns the active Supporter Tier (NONE, BRONZE, SILVER, GOLD, PLATINUM).
	 */
	public static SupporterTier getActiveTier() {
		// 1. Steam check: Running on Steam gives automatic Gold supporter entitlement
		PlatformServices platform = PlatformManager.get();
		if (platform != null && platform.isAvailable() && "STEAMWORKS".equalsIgnoreCase(platform.getPlatformId())) {
			return SupporterTier.GOLD;
		}

		// 2. Platform native supporter check (e.g., Google Play In-App Purchase)
		if (platform != null && platform.getSupporterTier() > 0) {
			return SupporterTier.fromRank(platform.getSupporterTier());
		}

		// 3. Device-bound activation token check
		String token = SPDSettings.supporterToken();
		if (token != null && !token.isEmpty() && isTokenValid(token)) {
			return getTierFromToken(token);
		}

		// 4. Local validated Patreon / Direct Supporter License Key
		String key = SPDSettings.supporterKey();
		if (isKeyValid(key)) {
			return getTierFromKey(key);
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
		try {
			String[] parts = token.split("-");
			if (parts.length >= 2) {
				String tierName = parts[1].toUpperCase(Locale.ROOT);
				if (tierName.contains("PLATINUM")) return SupporterTier.PLATINUM;
				if (tierName.contains("GOLD")) return SupporterTier.GOLD;
				if (tierName.contains("SILVER")) return SupporterTier.SILVER;
				if (tierName.contains("BRONZE")) return SupporterTier.BRONZE;
				if (tierName.contains("SUPPORTER")) return SupporterTier.GOLD;
			}
		} catch (Throwable ignored) {}
		return SupporterTier.BRONZE;
	}

	public static SupporterTier getTierFromKey(String key) {
		if (!isKeyValid(key)) return SupporterTier.NONE;
		String upper = key.toUpperCase(Locale.ROOT);
		if (upper.contains("PLAT")) return SupporterTier.PLATINUM;
		if (upper.contains("GOLD")) return SupporterTier.GOLD;
		if (upper.contains("SILV")) return SupporterTier.SILVER;
		if (upper.contains("BRON")) return SupporterTier.BRONZE;
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
	 * Attempts to activate a license key, creates a device-bound token and stores it in settings.
	 */
	public static boolean activateKey(String key) {
		if (key == null) return false;
		String cleanKey = key.trim().toUpperCase(Locale.ROOT);
		if (isKeyValid(cleanKey)) {
			SPDSettings.supporterKey(cleanKey);
			String deviceToken = generateDeviceToken(cleanKey, getDeviceId());
			SPDSettings.supporterToken(deviceToken);
			return true;
		}
		return false;
	}

	/**
	 * Deactivates the currently stored supporter key and device token.
	 */
	public static void deactivate() {
		SPDSettings.supporterKey("");
		SPDSettings.supporterToken("");
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
