/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Eternity Pixel Dungeon
 * Copyright (C) 2026 Eternity PD Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.pets;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.DirectableAlly;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndPet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public abstract class Pet extends DirectableAlly {

	public enum PetType {
		DRAGON,
		WOLF,
		FAIRY,
		SPIDER
	}

	public enum PetOrder {
		FOLLOW,
		DEFEND,
		STAY
	}

	public PetType petType;
	public String customName = null;
	public int petLevel = 1;
	public int exp = 0;
	public int maxExp = 25;

	public int hunger = 100;
	public static final int MAX_HUNGER = 100;

	public PetOrder currentOrder = PetOrder.FOLLOW;

	{
		alignment = Char.Alignment.ALLY;
		intelligentAlly = true;
		attacksAutomatically = true;
	}

	public Pet(PetType type) {
		super();
		this.petType = type;
		updateStats();
		HP = HT;
	}

	public int evolutionStage() {
		if (petLevel >= 10) return 2; // Adulto
		if (petLevel >= 5) return 1;  // Joven
		return 0;                     // Cría
	}

	public String getStageName() {
		int stage = evolutionStage();
		switch (stage) {
			case 2: return Messages.get(Pet.class, "stage_adult");
			case 1: return Messages.get(Pet.class, "stage_young");
			default: return Messages.get(Pet.class, "stage_baby");
		}
	}

	public void updateStats() {
		int stage = evolutionStage();
		HT = 20L + (petLevel * 6L) + (stage * 15L);
		defenseSkill = 4L + (petLevel * 2L) + (stage * 3L);
	}

	public void gainExp(int amount) {
		if (petLevel >= 15) return; // Nivel máximo

		exp += amount;
		while (exp >= maxExp && petLevel < 15) {
			exp -= maxExp;
			petLevel++;
			maxExp = 25 + (petLevel * 15);
			long prevHT = HT;
			updateStats();
			HP = Math.min(HT, HP + (HT - prevHT) + 5);

			if (sprite != null) {
				sprite.emitter().burst(Speck.factory(Speck.STAR), 8);
				sprite.showStatus(CharSprite.POSITIVE, Messages.get(Pet.class, "levelup"));
			}
			Sample.INSTANCE.play(Assets.Sounds.ITEM);
			GLog.p(Messages.get(Pet.class, "levelup_log", name(), petLevel));
		}
	}

	public boolean feed(Item item) {
		if (item == null) return false;

		boolean accepted = false;
		long healAmount = 0;
		int hungerGain = 0;

		if (item instanceof PotionOfHealing) {
			healAmount = HT / 2;
			hungerGain = 30;
			accepted = true;
		} else if (item instanceof Food) {
			healAmount = 10L + (petLevel * 2L);
			hungerGain = 40;
			accepted = true;
		} else if (item.name().toLowerCase().contains("meat") || item.name().toLowerCase().contains("berry")) {
			healAmount = 15L;
			hungerGain = 35;
			accepted = true;
		}

		if (accepted) {
			HP = Math.min(HT, HP + healAmount);
			hunger = Math.min(MAX_HUNGER, hunger + hungerGain);
			if (sprite != null) {
				sprite.emitter().burst(Speck.factory(Speck.HEALING), 6);
			}
			Sample.INSTANCE.play(Assets.Sounds.EAT);
			GLog.i(Messages.get(Pet.class, "fed", name()));
			return true;
		}

		GLog.w(Messages.get(Pet.class, "refuses_food", name()));
		return false;
	}

	public void setOrder(PetOrder order) {
		this.currentOrder = order;
		switch (order) {
			case FOLLOW:
				followHero();
				break;
			case DEFEND:
			case STAY:
				defendPos(pos);
				break;
		}
	}

	public static int getEmptyCellNear(int centerPos) {
		for (int n : PathFinder.NEIGHBOURS8) {
			int cell = centerPos + n;
			if (Dungeon.level != null && Dungeon.level.insideMap(cell) && Dungeon.level.passable[cell] && Actor.findChar(cell) == null) {
				return cell;
			}
		}
		return centerPos;
	}

	@Override
	public boolean act() {
		// Teletransporte si se aleja demasiado del héroe en modo FOLLOW
		if (currentOrder == PetOrder.FOLLOW && Dungeon.hero != null && Dungeon.hero.isAlive()) {
			if (Dungeon.level.distance(pos, Dungeon.hero.pos) > 12) {
				int nearCell = getEmptyCellNear(Dungeon.hero.pos);
				if (nearCell != Dungeon.hero.pos && Actor.findChar(nearCell) == null) {
					CellEmitter.get(pos).burst(Speck.factory(Speck.LIGHT), 4);
					move(nearCell);
					CellEmitter.get(pos).burst(Speck.factory(Speck.LIGHT), 4);
					if (sprite != null) sprite.place(pos);
				}
			}
		}

		// Desgaste de saciedad muy lento
		if (Random.Int(30) == 0 && hunger > 0) {
			hunger--;
		}

		return super.act();
	}

	@Override
	public boolean interact(Char chr) {
		if (chr == Dungeon.hero) {
			if (Dungeon.hero != null && Dungeon.hero.pet == null) {
				Dungeon.hero.pet = this;
			}
			com.watabou.noosa.Game.runOnRenderThread(new com.watabou.utils.Callback() {
				@Override
				public void call() {
					GameScene.show(new WndPet(Pet.this));
				}
			});
			return true;
		}
		return super.interact(chr);
	}

	@Override
	public String name() {
		if (customName != null && !customName.trim().isEmpty()) {
			return customName;
		}
		return Messages.get(this, "name");
	}

	@Override
	public String description() {
		return Messages.get(this, "desc");
	}

	@Override
	public void die(Object cause) {
		super.die(cause);
		if (Dungeon.hero != null && Dungeon.hero.pet == this) {
			Dungeon.hero.pet = null;
		}
		GLog.n(Messages.get(Pet.class, "died", name()));
	}

	private static final String TYPE = "pet_type";
	private static final String CUSTOM_NAME = "custom_name";
	private static final String LEVEL = "pet_level";
	private static final String EXP = "pet_exp";
	private static final String MAX_EXP = "pet_max_exp";
	private static final String HUNGER = "pet_hunger";
	private static final String ORDER = "pet_order";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		if (petType != null) bundle.put(TYPE, petType.name());
		if (customName != null) bundle.put(CUSTOM_NAME, customName);
		bundle.put(LEVEL, petLevel);
		bundle.put(EXP, exp);
		bundle.put(MAX_EXP, maxExp);
		bundle.put(HUNGER, hunger);
		if (currentOrder != null) bundle.put(ORDER, currentOrder.name());
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(TYPE)) {
			petType = PetType.valueOf(bundle.getString(TYPE));
		}
		if (bundle.contains(CUSTOM_NAME)) {
			customName = bundle.getString(CUSTOM_NAME);
		}
		petLevel = bundle.getInt(LEVEL);
		exp = bundle.getInt(EXP);
		maxExp = bundle.getInt(MAX_EXP);
		hunger = bundle.getInt(HUNGER);
		if (bundle.contains(ORDER)) {
			currentOrder = PetOrder.valueOf(bundle.getString(ORDER));
		}
		updateStats();
	}

	public static Pet create(PetType type) {
		switch (type) {
			case DRAGON: return new DragonPet();
			case WOLF:   return new WolfPet();
			case FAIRY:  return new FairyPet();
			case SPIDER: return new SpiderPet();
			default:     return new WolfPet();
		}
	}
}
