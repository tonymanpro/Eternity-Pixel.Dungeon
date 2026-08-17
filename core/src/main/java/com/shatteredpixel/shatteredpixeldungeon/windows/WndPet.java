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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.pets.Pet;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

public class WndPet extends Window {

	private static final int WIDTH = 130;
	private static final int BTN_HEIGHT = 16;
	private static final float GAP = 2;

	private Pet pet;

	public WndPet(final Pet pet) {
		super();
		this.pet = pet;

		IconTitle titlebar = new IconTitle();
		titlebar.label(pet.name() + " (" + pet.getStageName() + ")");
		titlebar.setRect(0, 0, WIDTH, 0);
		add(titlebar);

		String stats = Messages.get(this, "stats",
				pet.petLevel,
				pet.HP, pet.HT,
				pet.exp, pet.maxExp,
				pet.hunger);

		RenderedTextBlock statsBlock = PixelScene.renderTextBlock(stats, 6);
		statsBlock.maxWidth(WIDTH);
		statsBlock.setPos(0, titlebar.bottom() + GAP + 2);
		add(statsBlock);

		RenderedTextBlock descBlock = PixelScene.renderTextBlock(pet.description(), 6);
		descBlock.maxWidth(WIDTH);
		descBlock.setPos(0, statsBlock.bottom() + GAP + 2);
		add(descBlock);

		float pos = descBlock.bottom() + GAP + 4;

		// Botón de Alimentar
		RedButton btnFeed = new RedButton(Messages.get(this, "btn_feed")) {
			@Override
			public void onClick() {
				hide();
				GameScene.selectItem(new WndBag.ItemSelector() {
					@Override
					public String textPrompt() {
						return Messages.get(WndPet.class, "feed_prompt");
					}

					@Override
					public Class<? extends Bag> preferredBag() {
						return null;
					}

					@Override
					public boolean itemSelectable(Item item) {
						return item instanceof Food || item instanceof PotionOfHealing
								|| item.name().toLowerCase().contains("meat")
								|| item.name().toLowerCase().contains("berry");
					}

					@Override
					public void onSelect(Item item) {
						if (item != null) {
							if (pet.feed(item)) {
								item.detach(Dungeon.hero.belongings.backpack);
								Dungeon.hero.spendAndNext(1f);
							}
						}
					}
				});
			}
		};
		btnFeed.setRect(0, pos, WIDTH, BTN_HEIGHT);
		add(btnFeed);
		pos += BTN_HEIGHT + GAP;

		// Botón de Orden Táctica (Cambia entre Seguir / Defender)
		String orderText = pet.currentOrder == Pet.PetOrder.FOLLOW ?
				Messages.get(this, "order_follow") : Messages.get(this, "order_defend");

		RedButton btnOrder = new RedButton(Messages.get(this, "btn_order", orderText)) {
			@Override
			public void onClick() {
				if (pet.currentOrder == Pet.PetOrder.FOLLOW) {
					pet.setOrder(Pet.PetOrder.DEFEND);
				} else {
					pet.setOrder(Pet.PetOrder.FOLLOW);
				}
				hide();
				GameScene.show(new WndPet(pet));
			}
		};
		btnOrder.setRect(0, pos, WIDTH, BTN_HEIGHT);
		add(btnOrder);
		pos += BTN_HEIGHT + GAP;

		// Botón de Renombrar
		RedButton btnRename = new RedButton(Messages.get(this, "btn_rename")) {
			@Override
			public void onClick() {
				hide();
				GameScene.show(new WndTextInput(
						Messages.get(WndPet.class, "rename_title"),
						Messages.get(WndPet.class, "rename_prompt"),
						pet.name(),
						16,
						false,
						"OK",
						"Cancel") {
					@Override
					public void onSelect(boolean positive, String text) {
						if (positive && text != null && !text.trim().isEmpty()) {
							pet.customName = text.trim();
						}
						GameScene.show(new WndPet(pet));
					}
				});
			}
		};
		btnRename.setRect(0, pos, WIDTH, BTN_HEIGHT);
		add(btnRename);
		pos += BTN_HEIGHT + GAP;

		resize(WIDTH, (int) pos);
	}
}
