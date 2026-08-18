/*
 * Eternity Pixel Dungeon
 * Auto-Test Runner Framework - Desktop Launcher
 */

package com.shatteredpixel.shatteredpixeldungeon.desktop;

import com.shatteredpixel.shatteredpixeldungeon.test.AutoTestRunner;

public class DesktopAutoTestLauncher {

	public static void main(String[] args) {
		boolean exportMaps = false;
		if (args != null) {
			for (String arg : args) {
				if (arg.equalsIgnoreCase("--export-maps") || arg.equalsIgnoreCase("-exportMaps") || arg.equalsIgnoreCase("exportMaps")) {
					exportMaps = true;
				}
			}
		}
		if (Boolean.getBoolean("export.maps") || Boolean.getBoolean("exportImages")) {
			exportMaps = true;
		}

		int exitCode = AutoTestRunner.runAllTests(exportMaps);
		System.exit(exitCode);
	}
}
