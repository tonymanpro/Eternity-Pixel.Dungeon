/*
 * Eternity Pixel Dungeon
 * Auto-Test Runner Framework
 */

package com.shatteredpixel.shatteredpixeldungeon.test;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.services.platform.PlatformManager;
import com.watabou.noosa.Game;
import com.watabou.utils.FileUtils;
import com.watabou.utils.PlatformSupport;

public class HeadlessEnvironment {

	private static boolean initialized = false;

	public static synchronized void init() {
		if (initialized) return;

		FileUtils.setDefaultFileProperties(Files.FileType.Local, "");
		PlatformManager.init();

		HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();

		PlatformSupport headlessSupport = new PlatformSupport() {
			@Override
			public void updateDisplaySize() {
				Game.dispWidth = 800;
				Game.dispHeight = 600;
				Game.width = 800;
				Game.height = 600;
			}

			@Override
			public void updateSystemUI() {}

			@Override
			public boolean connectedToUnmeteredNetwork() {
				return true;
			}

			@Override
			public boolean supportsVibration() {
				return false;
			}

			@Override
			public void setupFontGenerators(int pageSize, boolean systemFont) {}

			@Override
			protected FreeTypeFontGenerator getGeneratorForString(String input) {
				return null;
			}

			@Override
			public String[] splitforTextBlock(String text, boolean multiline) {
				if (text == null) return new String[0];
				return multiline ? text.split("(?<= )|(?= )|(?<=\\n)|(?=\\n)") : text.split("(?<=\\n)|(?=\\n)");
			}
		};

		ShatteredPixelDungeon game = new ShatteredPixelDungeon(headlessSupport);
		new HeadlessApplication(game, config);

		Game.versionCode = 1000;
		Game.version = "v1.0.0";
		Game.dispWidth = 800;
		Game.dispHeight = 600;
		Game.width = 800;
		Game.height = 600;

		initialized = true;
	}
}
