/*
 * Eternity Pixel Dungeon
 * Victory Build and Hall of Fame Loadout Representation
 */

package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.pets.Pet;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class VictoryBuild implements Bundlable {

	public String heroClass = "";
	public String heroSubclass = "";
	public int level = 1;
	public int depth = 26;
	public long score = 0;
	public String date = "";
	public String seed = "";
	public boolean win = true;

	public String weaponName = "";
	public String armorName = "";
	public ArrayList<String> rings = new ArrayList<>();
	public ArrayList<String> artifacts = new ArrayList<>();

	public String petSpecies = "";
	public int petLevel = 1;
	public int petStage = 0;

	public VictoryBuild() {}

	public static VictoryBuild captureCurrentRun(Rankings.Record record) {
		VictoryBuild vb = new VictoryBuild();
		if (Dungeon.hero == null) return vb;

		Hero hero = Dungeon.hero;
		vb.heroClass = hero.heroClass.name();
		vb.heroSubclass = hero.subClass != null ? hero.subClass.name() : "";
		vb.level = hero.lvl;
		vb.depth = Math.max(record.depth, Statistics.deepestFloor);
		vb.score = record.score;
		vb.date = record.date;
		vb.seed = Dungeon.seed != -1 ? DungeonSeed.convertToCode(Dungeon.seed) : "";
		vb.win = record.win;

		Belongings belongings = hero.belongings;
		if (belongings.weapon != null) {
			vb.weaponName = belongings.weapon.name();
		}
		if (belongings.armor != null) {
			vb.armorName = belongings.armor.name();
		}

		if (belongings.rings != null) {
			for (Ring ring : belongings.rings) {
				if (ring != null) vb.rings.add(ring.name());
			}
		}

		if (belongings.artifacts != null) {
			for (Artifact art : belongings.artifacts) {
				if (art != null) vb.artifacts.add(art.name());
			}
		}

		if (Dungeon.level != null && Dungeon.level.mobs != null) {
			for (Mob mob : Dungeon.level.mobs) {
				if (mob instanceof Pet) {
					Pet pet = (Pet) mob;
					vb.petSpecies = pet.name();
					vb.petLevel = pet.petLevel;
					vb.petStage = pet.evolutionStage();
					break;
				}
			}
		}

		return vb;
	}

	private static final String HERO_CLASS    = "hero_class";
	private static final String HERO_SUBCLASS = "hero_subclass";
	private static final String LEVEL        = "level";
	private static final String DEPTH        = "depth";
	private static final String SCORE        = "score";
	private static final String DATE         = "date";
	private static final String SEED         = "seed";
	private static final String WIN          = "win";
	private static final String WEAPON       = "weapon";
	private static final String ARMOR        = "armor";
	private static final String RINGS        = "rings";
	private static final String ARTIFACTS    = "artifacts";
	private static final String PET_SPECIES  = "pet_species";
	private static final String PET_LEVEL    = "pet_level";
	private static final String PET_STAGE    = "pet_stage";

	@Override
	public void restoreFromBundle(Bundle bundle) {
		heroClass    = bundle.getString(HERO_CLASS);
		heroSubclass = bundle.getString(HERO_SUBCLASS);
		level        = bundle.getInt(LEVEL);
		depth        = bundle.getInt(DEPTH);
		score        = bundle.getLong(SCORE);
		date         = bundle.getString(DATE);
		seed         = bundle.getString(SEED);
		win          = bundle.getBoolean(WIN);

		weaponName   = bundle.getString(WEAPON);
		armorName    = bundle.getString(ARMOR);

		String[] ringArray = bundle.getStringArray(RINGS);
		rings.clear();
		if (ringArray != null) {
			for (String r : ringArray) rings.add(r);
		}

		String[] artArray = bundle.getStringArray(ARTIFACTS);
		artifacts.clear();
		if (artArray != null) {
			for (String a : artArray) artifacts.add(a);
		}

		petSpecies   = bundle.getString(PET_SPECIES);
		petLevel     = bundle.getInt(PET_LEVEL);
		petStage     = bundle.getInt(PET_STAGE);
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put(HERO_CLASS, heroClass);
		bundle.put(HERO_SUBCLASS, heroSubclass);
		bundle.put(LEVEL, level);
		bundle.put(DEPTH, depth);
		bundle.put(SCORE, score);
		bundle.put(DATE, date);
		bundle.put(SEED, seed);
		bundle.put(WIN, win);

		bundle.put(WEAPON, weaponName);
		bundle.put(ARMOR, armorName);

		bundle.put(RINGS, rings.toArray(new String[0]));
		bundle.put(ARTIFACTS, artifacts.toArray(new String[0]));

		bundle.put(PET_SPECIES, petSpecies);
		bundle.put(PET_LEVEL, petLevel);
		bundle.put(PET_STAGE, petStage);
	}
}
