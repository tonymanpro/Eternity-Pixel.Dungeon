/*
 * Eternity Pixel Dungeon
 * Map Screenshot & Visualizer Exporter for Marketing & Development
 */

package com.shatteredpixel.shatteredpixeldungeon.test;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class MapExporterTest {

	public static TestResult run() {
		TestResult result = new TestResult("Map Screenshot Exporter Test");
		long start = System.currentTimeMillis();

		try {
			HeadlessEnvironment.init();
			com.shatteredpixel.shatteredpixeldungeon.GamesInProgress.selectedClass = HeroClass.WARRIOR;
			Dungeon.hero = new Hero();
			Dungeon.hero.heroClass = HeroClass.WARRIOR;
			Dungeon.initSeed();
			Dungeon.init();

			File outputDir = new File("Marketing/art");
			if (!outputDir.exists()) outputDir.mkdirs();

			int[] depths = {1, 6, 11, 16, 21};
			String[] regionNames = {"sewers_depth1", "prison_depth6", "caves_depth11", "city_depth16", "halls_depth21"};

			for (int i = 0; i < depths.length; i++) {
				int d = depths[i];
				String name = regionNames[i];
				Dungeon.depth = d;
				Dungeon.branch = 0;

				Level level = Dungeon.newLevel();
				if (level == null) continue;

				BufferedImage img = renderLevelToImage(level, d);
				File outFile = new File(outputDir, "map_" + name + ".png");
				ImageIO.write(img, "png", outFile);

				result.pass("Exportado mapa visual HD: " + outFile.getPath() + " (" + img.getWidth() + "x" + img.getHeight() + "px)");
			}

		} catch (Exception e) {
			result.fail("Error exportando capturas visuales de mapas", e);
		}

		result.durationMs = System.currentTimeMillis() - start;
		return result;
	}

	private static BufferedImage renderLevelToImage(Level level, int depth) {
		int tileSize = 20;
		int width = level.width();
		int height = level.height();
		int imgW = width * tileSize;
		int imgH = height * tileSize;

		BufferedImage image = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();

		// Region Color Palettes
		Color wallColor, floorColor, waterColor, grassColor, doorColor, accentColor;

		if (depth <= 5) { // Sewers
			wallColor = new Color(35, 45, 40);
			floorColor = new Color(70, 85, 75);
			waterColor = new Color(40, 120, 90);
			grassColor = new Color(50, 160, 70);
			doorColor = new Color(130, 90, 40);
			accentColor = new Color(90, 190, 110);
		} else if (depth <= 10) { // Prison
			wallColor = new Color(40, 42, 55);
			floorColor = new Color(90, 95, 110);
			waterColor = new Color(50, 80, 140);
			grassColor = new Color(70, 110, 80);
			doorColor = new Color(100, 70, 50);
			accentColor = new Color(190, 80, 70);
		} else if (depth <= 15) { // Caves
			wallColor = new Color(50, 35, 25);
			floorColor = new Color(110, 90, 75);
			waterColor = new Color(40, 90, 150);
			grassColor = new Color(80, 140, 90);
			doorColor = new Color(150, 110, 50);
			accentColor = new Color(180, 140, 220);
		} else if (depth <= 20) { // City
			wallColor = new Color(60, 25, 35);
			floorColor = new Color(120, 80, 90);
			waterColor = new Color(100, 40, 130);
			grassColor = new Color(140, 70, 150);
			doorColor = new Color(180, 140, 40);
			accentColor = new Color(240, 190, 60);
		} else { // Demon Halls
			wallColor = new Color(30, 15, 20);
			floorColor = new Color(80, 40, 45);
			waterColor = new Color(200, 50, 20); // Lava
			grassColor = new Color(160, 40, 30);
			doorColor = new Color(140, 30, 30);
			accentColor = new Color(255, 100, 0);
		}

		// Fill Background (Void/Chasm)
		g.setColor(new Color(15, 15, 20));
		g.fillRect(0, 0, imgW, imgH);

		// Render Tiles
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int cell = x + y * width;
				int terr = level.map[cell];
				int px = x * tileSize;
				int py = y * tileSize;

				switch (terr) {
					case Terrain.WALL:
					case Terrain.WALL_DECO:
					case Terrain.SECRET_DOOR:
						g.setColor(wallColor);
						g.fillRect(px, py, tileSize, tileSize);
						g.setColor(wallColor.brighter());
						g.drawRect(px, py, tileSize - 1, tileSize - 1);
						break;

					case Terrain.EMPTY:
					case Terrain.EMPTY_DECO:
					case Terrain.EMPTY_SP:
					case Terrain.CUSTOM_DECO_EMPTY:
						g.setColor(floorColor);
						g.fillRect(px, py, tileSize, tileSize);
						g.setColor(floorColor.darker());
						g.drawRect(px, py, tileSize - 1, tileSize - 1);
						break;

					case Terrain.WATER:
						g.setColor(waterColor);
						g.fillRect(px, py, tileSize, tileSize);
						break;

					case Terrain.GRASS:
					case Terrain.HIGH_GRASS:
					case Terrain.FURROWED_GRASS:
						g.setColor(floorColor);
						g.fillRect(px, py, tileSize, tileSize);
						g.setColor(grassColor);
						g.fillOval(px + 4, py + 4, tileSize - 8, tileSize - 8);
						break;

					case Terrain.DOOR:
					case Terrain.OPEN_DOOR:
					case Terrain.LOCKED_DOOR:
					case Terrain.CRYSTAL_DOOR:
						g.setColor(doorColor);
						g.fillRect(px + 2, py + 2, tileSize - 4, tileSize - 4);
						g.setColor(Color.YELLOW);
						g.drawRect(px + 2, py + 2, tileSize - 5, tileSize - 5);
						break;

					case Terrain.ENTRANCE:
					case Terrain.ENTRANCE_SP:
						g.setColor(Color.CYAN);
						g.fillRect(px + 3, py + 3, tileSize - 6, tileSize - 6);
						break;

					case Terrain.EXIT:
					case Terrain.UNLOCKED_EXIT:
					case Terrain.LOCKED_EXIT:
						g.setColor(Color.MAGENTA);
						g.fillRect(px + 3, py + 3, tileSize - 6, tileSize - 6);
						break;

					case Terrain.STATUE:
					case Terrain.STATUE_SP:
					case Terrain.PEDESTAL:
					case Terrain.ALCHEMY:
						g.setColor(floorColor);
						g.fillRect(px, py, tileSize, tileSize);
						g.setColor(accentColor);
						g.fillOval(px + 3, py + 3, tileSize - 6, tileSize - 6);
						break;

					case Terrain.CHASM:
					default:
						// Already filled with void
						break;
				}
			}
		}

		// Render Items / Heaps
		g.setColor(Color.YELLOW);
		for (Heap heap : level.heaps.values()) {
			int hx = (heap.pos % width) * tileSize;
			int hy = (heap.pos / width) * tileSize;
			g.fillRect(hx + 7, hy + 7, 6, 6);
		}

		// Render Mobs
		g.setColor(Color.RED);
		for (Mob mob : level.mobs) {
			int mx = (mob.pos % width) * tileSize;
			int my = (mob.pos / width) * tileSize;
			g.fillOval(mx + 5, my + 5, 10, 10);
		}

		// Render Hero
		if (level.entrance() >= 0) {
			int hpos = level.entrance();
			int hx = (hpos % width) * tileSize;
			int hy = (hpos / width) * tileSize;
			g.setColor(Color.WHITE);
			g.fillOval(hx + 4, hy + 4, 12, 12);
			g.setColor(Color.BLUE);
			g.drawOval(hx + 3, hy + 3, 14, 14);
		}

		g.dispose();
		return image;
	}
}
