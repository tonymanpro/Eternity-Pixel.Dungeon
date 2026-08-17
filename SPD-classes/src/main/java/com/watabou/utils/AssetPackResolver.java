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

package com.watabou.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AssetPackResolver
 * Permite desacoplar recursos propietarios/comerciales (logos, música, arte, historia)
 * del paquete base libre (GPLv3).
 * Busca primero en paquetes registrados (ej. "packages/eternity/") y hace fallback automático.
 */
public class AssetPackResolver {

	private static final List<String> activePacks = new ArrayList<>();
	private static final Map<String, String> directOverrides = new HashMap<>();

	public static boolean musicOverrideEnabled = true;

	static {
		// Paquete propietario por defecto de Eternity Pixel Dungeon
		activePacks.add("packages/eternity/");
	}

	public static synchronized void addPack(String packPrefix) {
		if (packPrefix != null && !packPrefix.isEmpty()) {
			if (!packPrefix.endsWith("/")) {
				packPrefix += "/";
			}
			if (!activePacks.contains(packPrefix)) {
				activePacks.add(0, packPrefix); // Mayor prioridad al inicio
			}
		}
	}

	public static synchronized void removePack(String packPrefix) {
		if (packPrefix != null) {
			if (!packPrefix.endsWith("/")) packPrefix += "/";
			activePacks.remove(packPrefix);
		}
	}

	public static synchronized void setOverride(String originalPath, String overridePath) {
		if (originalPath != null) {
			if (overridePath == null) {
				directOverrides.remove(originalPath);
			} else {
				directOverrides.put(originalPath, overridePath);
			}
		}
	}

	/**
	 * Resuelve la mejor ruta para un asset.
	 * 1. Verifica directOverrides
	 * 2. Verifica existencia en activePacks ("packages/eternity/...")
	 * 3. Retorna la ruta original como fallback transparente.
	 */
	public static synchronized String resolvePath(String path) {
		if (path == null || path.isEmpty()) {
			return path;
		}

		if (path.startsWith("music/") && !musicOverrideEnabled) {
			return path;
		}

		if (directOverrides.containsKey(path)) {
			return directOverrides.get(path);
		}

		if (Gdx.files == null) {
			return path;
		}

		for (String pack : activePacks) {
			String candidate = pack + path;
			try {
				FileHandle handle = Gdx.files.internal(candidate);
				if (handle != null && handle.exists()) {
					return candidate;
				}
			} catch (Exception ignored) {
			}
		}

		return path;
	}

	/**
	 * Obtiene el FileHandle resuelto automáticamente con fallback seguro.
	 */
	public static FileHandle resolveHandle(String path) {
		String resolved = resolvePath(path);
		return Gdx.files.internal(resolved);
	}

	/**
	 * Comprueba si un asset tiene versión propietaria o personalizada activa.
	 */
	public static synchronized boolean hasOverride(String path) {
		if (path == null) return false;
		if (directOverrides.containsKey(path)) return true;
		if (Gdx.files == null) return false;

		for (String pack : activePacks) {
			try {
				FileHandle handle = Gdx.files.internal(pack + path);
				if (handle != null && handle.exists()) {
					return true;
				}
			} catch (Exception ignored) {
			}
		}
		return false;
	}
}
