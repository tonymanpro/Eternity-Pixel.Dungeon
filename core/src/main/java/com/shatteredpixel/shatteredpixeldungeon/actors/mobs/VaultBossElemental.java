/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Eternity Pixel Dungeon
 * Copyright (C) 2026 Eternity PD Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Freezing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RainbowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DwarfToken;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ElementalSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class VaultBossElemental extends Mob {

	{
		spriteClass = ElementalSprite.Chaos.class;

		int powerLevel = Dungeon.hero != null ? Dungeon.hero.lvl : 20;

		HP = HT = (long) ((bossMaxHPMulti + 1) * (350 + Math.round(powerLevel * 14 * Math.pow(2.2, Dungeon.cycle))));
		defenseSkill = 16 + powerLevel / 4;
		EXP = Dungeon.getCycleMultiplier(100);

		flying = true;

		properties.add(Property.BOSS);
		properties.add(Property.INORGANIC);
		properties.add(Property.LARGE);
	}

	public enum ElementType {
		FIRE, FROST, SHOCK, CHAOS
	}

	private ElementType currentElement = ElementType.FIRE;
	private int turnsSinceAbility = 0;
	private boolean battleAnnounced = false;

	@Override
	public long damageRoll() {
		return Random.NormalLongRange(
				Math.round((12 + Dungeon.hero.lvl * 0.8f) * Math.pow(1.5, Dungeon.cycle)),
				Math.round((28 + Dungeon.hero.lvl * 1.5f) * Math.pow(1.5, Dungeon.cycle)));
	}

	@Override
	public long attackSkill(Char target) {
		return 24 + Math.round(Dungeon.hero != null ? Dungeon.hero.lvl : 20);
	}

	@Override
	public long drRoll() {
		return Random.NormalLongRange(4, 14);
	}

	@Override
	protected boolean act() {
		if (Dungeon.level != null && Dungeon.hero != null && fieldOfView[Dungeon.hero.pos]) {
			if (!battleAnnounced) {
				battleAnnounced = true;
				yell(Messages.get(this, "awakened"));
				Dungeon.level.seal();
				Camera.main.shake(3, 0.5f);
				Sample.INSTANCE.play(Assets.Sounds.ROCKS);
			}

			turnsSinceAbility++;
			if (turnsSinceAbility >= 3 && distance(Dungeon.hero) <= 6 && canAttack(Dungeon.hero)) {
				turnsSinceAbility = 0;
				useElementalBurst(Dungeon.hero);
				spend(TICK);
				return true;
			}
		}

		return super.act();
	}

	private void useElementalBurst(Hero hero) {
		// Cycle between elements
		ElementType[] types = ElementType.values();
		currentElement = types[(currentElement.ordinal() + 1) % types.length];

		sprite.zap(hero.pos);
		switch (currentElement) {
			case FIRE:
				GLog.w(Messages.get(this, "fire_burst"));
				Sample.INSTANCE.play(Assets.Sounds.BURNING);
				hero.damage(damageRoll(), this);
				Buff.affect(hero, Burning.class).reignite(hero);
				CellEmitter.get(hero.pos).burst(FlameParticle.FACTORY, 8);
				break;
			case FROST:
				GLog.w(Messages.get(this, "frost_burst"));
				Sample.INSTANCE.play(Assets.Sounds.SHATTER);
				hero.damage(Math.round(damageRoll() * 0.85f), this);
				Buff.prolong(hero, Chill.class, 4f);
				CellEmitter.get(hero.pos).burst(ElmoParticle.FACTORY, 8);
				break;
			case SHOCK:
				GLog.w(Messages.get(this, "shock_burst"));
				Sample.INSTANCE.play(Assets.Sounds.ZAP);
				hero.damage(Math.round(damageRoll() * 0.9f), this);
				CellEmitter.get(hero.pos).burst(SparkParticle.STATIC, 8);
				if (Random.Int(3) == 0) {
					Buff.prolong(hero, Paralysis.class, 2f);
				}
				break;
			case CHAOS:
			default:
				GLog.w(Messages.get(this, "chaos_burst"));
				Sample.INSTANCE.play(Assets.Sounds.RAY);
				hero.damage(Math.round(damageRoll() * 1.1f), this);
				CellEmitter.get(hero.pos).burst(RainbowParticle.BURST, 10);
				break;
		}
	}

	@Override
	public void die(Object cause) {
		super.die(cause);

		GameScene.bossSlain();
		Dungeon.level.unseal();

		// Drop 5 Dwarf Tokens for the Imp quest
		DwarfToken tokens = new DwarfToken();
		tokens.quantity(5);
		Dungeon.level.drop(tokens, pos).sprite.drop(pos);

		// Drop a high quality ring
		Ring reward = (Ring) Generator.random(Generator.Category.RING);
		if (reward != null) {
			reward.cursed = false;
			reward.upgrade(2 + Dungeon.cycle);
			Dungeon.level.drop(reward, pos).sprite.drop(pos);
		}

		// Drop a generous pile of gold
		Gold gold = new Gold();
		gold.quantity((int) Random.NormalLongRange(500, 1200));
		Dungeon.level.drop(gold, pos).sprite.drop(pos);

		yell(Messages.get(this, "defeated"));
		GLog.p(Messages.get(this, "victory_msg"));
	}

	private static final String BATTLE_ANNOUNCED = "battle_announced";
	private static final String ELEMENT_TYPE = "element_type";
	private static final String TURNS_ABILITY = "turns_ability";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(BATTLE_ANNOUNCED, battleAnnounced);
		bundle.put(ELEMENT_TYPE, currentElement.name());
		bundle.put(TURNS_ABILITY, turnsSinceAbility);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		battleAnnounced = bundle.getBoolean(BATTLE_ANNOUNCED);
		if (bundle.contains(ELEMENT_TYPE)) {
			currentElement = ElementType.valueOf(bundle.getString(ELEMENT_TYPE));
		}
		turnsSinceAbility = bundle.getInt(TURNS_ABILITY);
	}
}
