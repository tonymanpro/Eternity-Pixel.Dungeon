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

package com.shatteredpixel.shatteredpixeldungeon.items.pets;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.pets.Pet;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class PetEgg extends Item {

	public static final String AC_HATCH   = "HATCH";
	public static final String AC_WARM    = "WARM";

	public Pet.PetType eggType = Pet.PetType.WOLF;
	public int incubation = 0;
	public static final int REQUIRED_INCUBATION = 80;

	{
		image = ItemSpriteSheet.HONEYPOT;
		stackable = false;
		defaultAction = AC_WARM;
	}

	public PetEgg() {
		this(Pet.PetType.values()[Random.Int(Pet.PetType.values().length)]);
	}

	public PetEgg(Pet.PetType type) {
		super();
		this.eggType = type;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if (isReadyToHatch()) {
			actions.add(AC_HATCH);
		} else {
			actions.add(AC_WARM);
		}
		return actions;
	}

	public boolean isReadyToHatch() {
		return incubation >= REQUIRED_INCUBATION;
	}

	public void advanceIncubation(int amount) {
		incubation = Math.min(REQUIRED_INCUBATION, incubation + amount);
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (action.equals(AC_WARM)) {
			advanceIncubation(15);
			if (hero.sprite != null) {
				hero.sprite.operate(hero.pos);
			}
			Sample.INSTANCE.play(Assets.Sounds.WATER);
			CellEmitter.get(hero.pos).burst(Speck.factory(Speck.HEALING), 4);
			GLog.i(Messages.get(this, "warmed", incubation * 100 / REQUIRED_INCUBATION));
			hero.spendAndNext(1f);

		} else if (action.equals(AC_HATCH)) {
			if (eggType == Pet.PetType.MANTICORE && !com.shatteredpixel.shatteredpixeldungeon.services.platform.SupporterManager.isSupporter()) {
				GLog.w(Messages.get(this, "manticore_supporter_locked"));
				GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndSupporterUnlock());
				return;
			}

			if (!isReadyToHatch()) {
				GLog.w(Messages.get(this, "not_ready"));
				return;
			}

			if (hero.pet != null && hero.pet.isAlive()) {
				GLog.w(Messages.get(this, "already_has_pet"));
				return;
			}

			int spawnCell = Pet.getEmptyCellNear(hero.pos);
			if (Actor.findChar(spawnCell) != null) {
				spawnCell = hero.pos;
			}

			Pet newPet = Pet.create(eggType);
			newPet.pos = spawnCell;
			GameScene.add(newPet);
			hero.pet = newPet;

			detach(hero.belongings.backpack);

			if (hero.sprite != null) {
				hero.sprite.operate(hero.pos);
			}
			newPet.playVoice();
			Sample.INSTANCE.play(Assets.Sounds.EVOKE);
			CellEmitter.get(spawnCell).burst(Speck.factory(Speck.STAR), 10);
			GLog.p(Messages.get(this, "hatched", newPet.name()));
			com.shatteredpixel.shatteredpixeldungeon.Badges.validatePetHatched(eggType);
			hero.spendAndNext(1f);
		}
	}

	@Override
	public String name() {
		return Messages.get(this, "name_" + eggType.name().toLowerCase());
	}

	@Override
	public String info() {
		int percent = incubation * 100 / REQUIRED_INCUBATION;
		return Messages.get(this, "desc_" + eggType.name().toLowerCase(), percent);
	}

	@Override
	public com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing glowing() {
		int glowColor;
		switch (eggType) {
			case DRAGON:
				glowColor = 0xFF4500; // Fire Orange
				break;
			case WOLF:
				glowColor = 0x00E5FF; // Frost Cyan
				break;
			case FAIRY:
				glowColor = 0x00FF88; // Emerald Fairy Glow
				break;
			case MANTICORE:
				glowColor = 0xFFD700; // Mythic Gold
				break;
			default:
				glowColor = 0x9932CC; // Mystic Purple
				break;
		}
		return new com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing(glowColor, 1.2f);
	}

	@Override
	public com.watabou.noosa.particles.Emitter emitter() {
		com.watabou.noosa.particles.Emitter emitter = new com.watabou.noosa.particles.Emitter();
		emitter.pos(0, 0);
		emitter.autoKill = false;
		int speckType = (eggType == Pet.PetType.DRAGON) ? Speck.LIGHT : Speck.STAR;
		emitter.pour(Speck.factory(speckType), 0.6f);
		return emitter;
	}

	private static final String TYPE = "egg_type";
	private static final String INCUBATION = "incubation";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		if (eggType != null) bundle.put(TYPE, eggType.name());
		bundle.put(INCUBATION, incubation);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(TYPE)) {
			eggType = Pet.PetType.valueOf(bundle.getString(TYPE));
		}
		incubation = bundle.getInt(INCUBATION);
	}
}
