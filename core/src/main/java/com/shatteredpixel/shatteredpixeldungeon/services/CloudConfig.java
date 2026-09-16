/*
 * Eternity Pixel Dungeon
 * Cloud Services Dynamic Configuration (Resolves from private submodule asset pack)
 */

package com.shatteredpixel.shatteredpixeldungeon.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public final class CloudConfig {

	private static final String CONFIG_ASSET_PATH = "packages/eternity/config/cloud_services.json";

	private static boolean initialized = false;
	private static boolean available = false;
	private static String firestoreProject = null;
	private static String firestoreDatabase = null;

	private static synchronized void init() {
		if (initialized) return;
		initialized = true;

		try {
			if (Gdx.files != null) {
				FileHandle file = Gdx.files.internal(CONFIG_ASSET_PATH);
				if (file != null && file.exists()) {
					JsonValue json = new JsonReader().parse(file.readString("UTF-8"));
					if (json != null) {
						firestoreProject = json.getString("firestore_project", null);
						firestoreDatabase = json.getString("firestore_database", null);
						if (firestoreProject != null && !firestoreProject.isEmpty() &&
								firestoreDatabase != null && !firestoreDatabase.isEmpty()) {
							available = true;
						}
					}
				}
			}
		} catch (Throwable t) {
			available = false;
		}
	}

	public static boolean isAvailable() {
		init();
		return available;
	}

	public static String getUsernameEndpoint() {
		init();
		if (!available) return null;
		return "https://firestore.googleapis.com/v1/projects/" + firestoreProject +
				"/databases/" + firestoreDatabase + "/documents/usernames";
	}

	public static String getHallOfFameEndpoint() {
		init();
		if (!available) return null;
		return "https://firestore.googleapis.com/v1/projects/" + firestoreProject +
				"/databases/" + firestoreDatabase + "/documents/hall_of_fame";
	}
}
