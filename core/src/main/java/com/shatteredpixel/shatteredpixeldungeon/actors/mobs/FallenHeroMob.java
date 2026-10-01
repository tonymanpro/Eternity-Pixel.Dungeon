/*
 * Eternity Pixel Dungeon
 * Fallen Hero Mob (Nemesis Optional Duel Encounter)
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.FallenHeroRecord;
import com.shatteredpixel.shatteredpixeldungeon.NemesisConfig;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GhostSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class FallenHeroMob extends Mob {

	public FallenHeroRecord record;

	{
		spriteClass = GhostSprite.class;
		flying = true;
		state = HUNTING;
		properties.add(Property.UNDEAD);
		properties.add(Property.MINIBOSS);
	}

	public static FallenHeroMob spawnAt(FallenHeroRecord rec, int pos) {
		NemesisConfig cfg = NemesisConfig.get();
		FallenHeroMob mob = new FallenHeroMob();
		mob.record = rec;
		mob.pos = pos;

		int depth = rec != null ? rec.depth : Dungeon.depth;
		mob.HT = cfg.mobHpBase + depth * cfg.mobHpPerDepth;
		mob.HP = mob.HT;
		mob.defenseSkill = cfg.mobDefBase + depth * cfg.mobDefPerDepth;
		mob.maxLvl = depth + 3;
		mob.EXP = 1 + depth / 2;

		return mob;
	}

	@Override
	public String name() {
		return Messages.get(this, "name");
	}

	@Override
	public String description() {
		return Messages.get(this, "desc");
	}

	@Override
	public long damageRoll() {
		int depth = record != null ? record.depth : Dungeon.depth;
		return Random.NormalIntRange(1 + depth / 2, 4 + depth);
	}

	@Override
	public long attackSkill(Char target) {
		int depth = record != null ? record.depth : Dungeon.depth;
		return 8 + depth;
	}

	@Override
	public long drRoll() {
		int depth = record != null ? record.depth : Dungeon.depth;
		return Random.NormalIntRange(0, Math.max(1, depth / 4));
	}

	private static final String RECORD = "record";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		if (record != null) {
			Bundle recBundle = new Bundle();
			recBundle.put("heroClass", record.heroClass);
			recBundle.put("heroSubclass", record.heroSubclass);
			recBundle.put("level", record.level);
			recBundle.put("depth", record.depth);
			recBundle.put("causeOfDeath", record.causeOfDeath);
			recBundle.put("weaponName", record.weaponName);
			recBundle.put("armorName", record.armorName);
			recBundle.put("seed", record.seed);
			recBundle.put("date", record.date);
			recBundle.put("released", record.released);
			bundle.put(RECORD, recBundle);
		}
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(RECORD)) {
			Bundle recBundle = bundle.getBundle(RECORD);
			record = new FallenHeroRecord();
			record.heroClass = recBundle.getString("heroClass");
			record.heroSubclass = recBundle.getString("heroSubclass");
			record.level = recBundle.getInt("level");
			record.depth = recBundle.getInt("depth");
			record.causeOfDeath = recBundle.getString("causeOfDeath");
			record.weaponName = recBundle.getString("weaponName");
			record.armorName = recBundle.getString("armorName");
			record.seed = recBundle.getString("seed");
			record.date = recBundle.getString("date");
			record.released = recBundle.getBoolean("released");
		} else if (FallenHeroRecord.activeRecord != null) {
			record = FallenHeroRecord.activeRecord;
		}
	}

	@Override
	public void die(Object cause) {
		super.die(cause);

		NemesisConfig cfg = NemesisConfig.get();

		if (record == null) {
			record = FallenHeroRecord.activeRecord != null ? FallenHeroRecord.activeRecord : FallenHeroRecord.load();
		}

		if (record != null) {
			record.released = true;
			FallenHeroRecord.save(record);
		}

		GLog.p(Messages.get(cfg.purifiedKey));
		CellEmitter.get(pos).burst(Speck.factory(Speck.DISCOVER), 12);

		// Drop fallen hero reward
		Item rewardItem = Generator.random(Random.oneOf(Generator.Category.WEAPON, Generator.Category.ARMOR));
		if (rewardItem != null) {
			rewardItem.identify();
			rewardItem.upgrade(1);
			Dungeon.level.drop(rewardItem, pos);
		}

		int goldAmt = Random.IntRange(cfg.goldRewardMin, cfg.goldRewardMax + Dungeon.depth * 10);
		Dungeon.level.drop(new Gold(goldAmt), pos);
	}
}
