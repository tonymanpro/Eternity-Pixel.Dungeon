package com.shatteredpixel.shatteredpixeldungeon.test;

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

public class LanguageTest {

	public static TestResult run() {
		String testName = "Language Switching & Bundle Resolution Test";
		long startTime = System.currentTimeMillis();
		TestResult result = new TestResult(testName);

		try {
			Languages originalLang = SPDSettings.language();

			// Test 1: Portuguese (pt)
			SPDSettings.language(Languages.PORTUGUESE);
			Messages.setup(Languages.PORTUGUESE);
			String ptName = Messages.get("actors.blobs.blizzard.name");
			System.out.println("[LanguageTest] Portuguese blizzard name: " + ptName);
			if (ptName != null && ptName.toLowerCase().contains("nevasca")) {
				result.pass("Portuguese bundle resolved correctly: " + ptName);
			} else {
				result.fail("Portuguese bundle failed. Expected 'Nevasca', got: " + ptName, null);
			}

			// Test 2: Spanish (es)
			SPDSettings.language(Languages.SPANISH);
			Messages.setup(Languages.SPANISH);
			String esName = Messages.get("actors.mobs.rat.name");
			System.out.println("[LanguageTest] Spanish rat name: " + esName);
			if (esName != null && esName.equalsIgnoreCase("rata marsupial")) {
				result.pass("Spanish bundle resolved correctly: " + esName);
			} else {
				result.fail("Spanish bundle failed. Expected 'rata marsupial', got: " + esName, null);
			}

			// Test 3: French (fr)
			SPDSettings.language(Languages.FRENCH);
			Messages.setup(Languages.FRENCH);
			String frName = Messages.get("actors.mobs.rat.name");
			System.out.println("[LanguageTest] French rat name: " + frName);
			if (frName != null && !frName.equals(Messages.NO_TEXT_FOUND)) {
				result.pass("French bundle resolved correctly: " + frName);
			} else {
				result.fail("French bundle failed. Got: " + frName, null);
			}

			// Test 4: German (de)
			SPDSettings.language(Languages.GERMAN);
			Messages.setup(Languages.GERMAN);
			String deName = Messages.get("actors.mobs.rat.name");
			System.out.println("[LanguageTest] German rat name: " + deName);
			if (deName != null && !deName.equals(Messages.NO_TEXT_FOUND)) {
				result.pass("German bundle resolved correctly: " + deName);
			} else {
				result.fail("German bundle failed. Got: " + deName, null);
			}

			// Restore original language
			SPDSettings.language(originalLang);
			Messages.setup(originalLang);

		} catch (Exception e) {
			result.fail("Exception during LanguageTest: " + e.getMessage(), e);
		}

		result.durationMs = System.currentTimeMillis() - startTime;
		return result;
	}
}
