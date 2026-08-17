/*
 * Eternity Pixel Dungeon
 * Auto-Test Runner Framework
 */

package com.shatteredpixel.shatteredpixeldungeon.test;

import java.util.ArrayList;
import java.util.List;

public class TestResult {

	public final String suiteName;
	public boolean passed = true;
	public long durationMs = 0;
	public int testsRun = 0;
	public int testsPassed = 0;
	public int testsFailed = 0;
	public final List<String> details = new ArrayList<>();
	public final List<String> failures = new ArrayList<>();

	public TestResult(String suiteName) {
		this.suiteName = suiteName;
	}

	public void pass(String message) {
		testsRun++;
		testsPassed++;
		details.add("[PASS] " + message);
	}

	public void fail(String message, Throwable t) {
		testsRun++;
		testsFailed++;
		passed = false;
		String err = "[FAIL] " + message + (t != null ? " -> " + t.getClass().getSimpleName() + ": " + t.getMessage() : "");
		details.add(err);
		failures.add(err);
	}

	public void info(String message) {
		details.add("  " + message);
	}
}
