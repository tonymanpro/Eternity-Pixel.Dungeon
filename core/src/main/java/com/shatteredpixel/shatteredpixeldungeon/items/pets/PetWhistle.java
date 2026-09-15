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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.pets.Pet;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class PetWhistle extends Item {

	public static final String AC_USE     = "USE";
	public static final String AC_RECALL  = "RECALL";
	public static final String AC_SUMMON  = "SUMMON";

	{
		image = ItemSpriteSheet.ARTIFACT_HORN1;
		stackable = false;
		defaultAction = AC_USE;
		unique = true;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		Pet activePet = getActivePet(hero);
		if (activePet != null) {
			actions.add(AC_RECALL);
		} else if (hero.storedPet != null) {
			actions.add(AC_SUMMON);
		} else {
			actions.add(AC_USE);
		}
		return actions;
	}

	private Pet getActivePet(Hero hero) {
		if (hero.pet != null && hero.pet.isAlive()) {
			return hero.pet;
		}
		if (Dungeon.level != null) {
			for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob : Dungeon.level.mobs) {
				if (mob instanceof Pet && mob.isAlive()) {
					hero.pet = (Pet) mob;
					return (Pet) mob;
				}
			}
		}
		return null;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		Pet activePet = getActivePet(hero);

		if (action.equals(AC_RECALL)) {
			if (activePet != null) {
				Sample.INSTANCE.play(Assets.Sounds.CHALLENGE);
				activePet.recall();
				hero.spend(1f);
				hero.busy();
			}
		} else if (action.equals(AC_SUMMON)) {
			if (hero.storedPet != null) {
				Sample.INSTANCE.play(Assets.Sounds.CHALLENGE);
				Pet.summon(hero);
				hero.spend(1f);
				hero.busy();
			}
		} else if (action.equals(AC_USE)) {
			if (activePet != null) {
				Sample.INSTANCE.play(Assets.Sounds.CHALLENGE);
				activePet.recall();
				hero.spend(1f);
				hero.busy();
			} else if (hero.storedPet != null) {
				Sample.INSTANCE.play(Assets.Sounds.CHALLENGE);
				Pet.summon(hero);
				hero.spend(1f);
				hero.busy();
			} else {
				GLog.w(Messages.get(PetWhistle.class, "no_pet"));
			}
		}
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}
}
