/*
 * Eternity Pixel Dungeon
 * Auto-Test Suite: Supporter Licensing & Entitlements Test
 */

package com.shatteredpixel.shatteredpixeldungeon.test;

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.services.platform.NullPlatformServices;
import com.shatteredpixel.shatteredpixeldungeon.services.platform.PlatformManager;
import com.shatteredpixel.shatteredpixeldungeon.services.platform.SupporterManager;

public class SupporterLicensingTest {

	public static TestResult run() {
		long start = System.currentTimeMillis();
		TestResult result = new TestResult("Supporter Licensing & Entitlement Test");

		try {
			// Test 1: Clean initial state
			SupporterManager.deactivate();
			NullPlatformServices nullPlatform = new NullPlatformServices();
			nullPlatform.setSupporter(false);
			PlatformManager.setService(nullPlatform);

			if (!SupporterManager.isSupporter() && SupporterManager.getActiveTier() == SupporterManager.SupporterTier.NONE) {
				result.pass("Initial unactivated state verified (Standard Edition)");
			} else {
				result.fail("Expected unactivated state by default", null);
			}

			// Test 2: Standard Key generation and checksum validation
			String generatedKey = SupporterManager.generateKey("patron@test.org");
			if (generatedKey != null && SupporterManager.isKeyValid(generatedKey)) {
				result.pass("Valid standard key generated and verified: " + generatedKey);
			} else {
				result.fail("Failed to validate generated key: " + generatedKey, null);
			}

			// Test 3: Key activation & full access entitlement
			boolean activated = SupporterManager.activateKey(generatedKey);
			if (activated && SupporterManager.isSupporter() && SupporterManager.getActiveTier() == SupporterManager.SupporterTier.SUPPORTER) {
				result.pass("Key successfully activated with full Supporter/Premium access: " + generatedKey);
			} else {
				result.fail("Expected Supporter tier activation", null);
			}

			// Test 4: Device-bound token validation and hardware fingerprinting
			String deviceId = SupporterManager.getDeviceId();
			String validToken = SupporterManager.generateDeviceToken(generatedKey, deviceId);
			String foreignToken = SupporterManager.generateDeviceToken(generatedKey, "DEV_FOREIGN_9999");

			if (SupporterManager.isTokenValid(validToken) && !SupporterManager.isTokenValid(foreignToken)) {
				result.pass("Device fingerprinting verified: Genuine token accepted, foreign device token rejected");
			} else {
				result.fail("Device token validation failed", null);
			}

			// Test 5: Invalid and forged keys rejected
			boolean invalidKey1 = SupporterManager.isKeyValid("INVALID-KEY-1234");
			boolean invalidKey2 = SupporterManager.isKeyValid("EPD-1234-5678-0000"); // Wrong checksum
			boolean invalidKey3 = SupporterManager.isKeyValid("");
			boolean invalidKey4 = SupporterManager.isKeyValid(null);

			if (!invalidKey1 && !invalidKey2 && !invalidKey3 && !invalidKey4) {
				result.pass("Invalid and forged keys correctly rejected");
			} else {
				result.fail("Failed to reject invalid keys", null);
			}

			// Test 6: Key and device token deactivation
			SupporterManager.deactivate();
			if (!SupporterManager.isSupporter() && SPDSettings.supporterToken().isEmpty()) {
				result.pass("Key and device token successfully deactivated back to Standard Edition");
			} else {
				result.fail("Deactivation failed", null);
			}

			// Test 7: Platform In-App Purchase / Steam Entitlement
			nullPlatform.setSupporter(true);
			if (SupporterManager.isSupporter() && SupporterManager.getActiveTier() == SupporterManager.SupporterTier.SUPPORTER) {
				result.pass("Platform In-App Purchase / Google Play / Steam entitlement verified");
			} else {
				result.fail("Platform entitlement check failed", null);
			}

			// Reset platform back to clean state
			nullPlatform.setSupporter(false);
			SupporterManager.deactivate();

		} catch (Throwable t) {
			result.fail("Unexpected exception during Supporter Licensing Test", t);
		}

		result.durationMs = System.currentTimeMillis() - start;
		return result;
	}
}
