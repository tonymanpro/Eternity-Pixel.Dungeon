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
import com.watabou.utils.Random;

public class FallenHeroMob extends Mob {

	public FallenHeroRecord record;

	{
		spriteClass = GhostSprite.class;
		flying = true;
		state = HUNTING;
		properties.add(Property.UNDEAD);
		properties.add(Property.BOSS);
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

		return mob;
	}

	@Override
	public long damageRoll() {
		int depth = record != null ? record.depth : Dungeon.depth;
		return Random.NormalIntRange(4 + depth, 8 + depth * 2);
	}

	@Override
	public long attackSkill(Char target) {
		int depth = record != null ? record.depth : Dungeon.depth;
		return 12 + depth * 2;
	}

	@Override
	public long drRoll() {
		int depth = record != null ? record.depth : Dungeon.depth;
		return Random.NormalIntRange(0, 2 + depth / 2);
	}

	@Override
	public void die(Object cause) {
		super.die(cause);

		NemesisConfig cfg = NemesisConfig.get();

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

		int goldAmt = Random.IntRange(cfg.goldRewardMin, cfg.goldRewardMax + Dungeon.depth * 20);
		Dungeon.level.drop(new Gold(goldAmt), pos);
	}
}
