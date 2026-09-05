/*
 * Eternity Pixel Dungeon
 * Fallen Hero / Nemesis System Record Persistence
 */

package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FallenHeroRecord {

	private static final String FILE_NAME = "fallen_hero.dat";

	public String heroClass = "";
	public String heroSubclass = "";
	public int level = 1;
	public int depth = 1;
	public String causeOfDeath = "";
	public String weaponName = "";
	public String armorName = "";
	public String date = "";
	public String seed = "";
	public boolean released = false;

	public static FallenHeroRecord activeRecord = null;

	public static void captureDeath(Hero hero, int depth, String cause) {
		if (Dungeon.daily || hero == null) return;

		FallenHeroRecord record = new FallenHeroRecord();
		record.heroClass = hero.heroClass.name();
		record.heroSubclass = hero.subClass != null ? hero.subClass.name() : "";
		record.level = hero.lvl;
		record.depth = depth;
		record.causeOfDeath = cause != null && !cause.isEmpty() ? cause : "Oscuridad de la Mazmorra";
		if (hero.belongings.weapon != null) {
			record.weaponName = hero.belongings.weapon.name();
		}
		if (hero.belongings.armor != null) {
			record.armorName = hero.belongings.armor.name();
		}
		record.seed = Dungeon.seed != -1 ? DungeonSeed.convertToCode(Dungeon.seed) : "";
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
		record.date = format.format(new Date());
		record.released = false;

		activeRecord = record;
		save(record);
	}

	public static void save(FallenHeroRecord record) {
		if (record == null) return;
		Bundle bundle = new Bundle();
		bundle.put("heroClass", record.heroClass);
		bundle.put("heroSubclass", record.heroSubclass);
		bundle.put("level", record.level);
		bundle.put("depth", record.depth);
		bundle.put("causeOfDeath", record.causeOfDeath);
		bundle.put("weaponName", record.weaponName);
		bundle.put("armorName", record.armorName);
		bundle.put("date", record.date);
		bundle.put("seed", record.seed);
		bundle.put("released", record.released);

		try {
			FileUtils.bundleToFile(FILE_NAME, bundle);
		} catch (IOException e) {
			ShatteredPixelDungeon.reportException(e);
		}
	}

	public static FallenHeroRecord load() {
		try {
			Bundle bundle = FileUtils.bundleFromFile(FILE_NAME);
			if (bundle == null || !bundle.contains("heroClass")) return null;

			FallenHeroRecord record = new FallenHeroRecord();
			record.heroClass = bundle.getString("heroClass");
			record.heroSubclass = bundle.getString("heroSubclass");
			record.level = bundle.getInt("level");
			record.depth = bundle.getInt("depth");
			record.causeOfDeath = bundle.getString("causeOfDeath");
			record.weaponName = bundle.getString("weaponName");
			record.armorName = bundle.getString("armorName");
			record.date = bundle.getString("date");
			record.seed = bundle.getString("seed");
			record.released = bundle.getBoolean("released");

			activeRecord = record;
			return record;
		} catch (Exception e) {
			return null;
		}
	}

	public static void clear() {
		activeRecord = null;
		FileUtils.deleteFile(FILE_NAME);
	}
}
