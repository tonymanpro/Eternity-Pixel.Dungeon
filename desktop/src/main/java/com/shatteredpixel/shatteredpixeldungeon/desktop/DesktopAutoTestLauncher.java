/*
 * Eternity Pixel Dungeon
 * Auto-Test Runner Framework - Desktop Launcher
 */

package com.shatteredpixel.shatteredpixeldungeon.desktop;

import com.shatteredpixel.shatteredpixeldungeon.test.AutoTestRunner;

public class DesktopAutoTestLauncher {

	public static void main(String[] args) {
		int exitCode = AutoTestRunner.runAllTests();
		System.exit(exitCode);
	}
}
