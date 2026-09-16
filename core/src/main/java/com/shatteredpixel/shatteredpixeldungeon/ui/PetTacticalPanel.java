/*
 * Eternity Pixel Dungeon
 * Pet Companion Tag / Tactical Widget
 */

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.pets.Pet;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndPet;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.RectF;
import com.watabou.utils.Reflection;

public class PetTacticalPanel extends Button {

	public static final int SIZE = 24;

	private NinePatch bg;
	private CharSprite sprite;
	private Class<? extends CharSprite> lastSpriteClass = null;

	private ColorBlock hpBg;
	private ColorBlock hpBar;
	private RenderedTextBlock statusBadge;

	private Pet currentPet = null;
	private boolean lastStored = false;

	public PetTacticalPanel() {
		super();

		bg = Chrome.get(Chrome.Type.TAG);
		bg.hardlight(0.2f, 0.6f, 0.3f);
		addToBack(bg);

		hpBg = new ColorBlock(18, 2, 0xAA000000);
		add(hpBg);

		hpBar = new ColorBlock(18, 2, 0xFF44CC44);
		add(hpBar);

		statusBadge = PixelScene.renderTextBlock(5);
		add(statusBadge);

		setSize(SIZE, SIZE);

		visible = false;
	}

	private Pet getActivePet() {
		Hero hero = Dungeon.hero;
		if (hero != null && hero.pet != null && hero.pet.isAlive()) {
			return hero.pet;
		}
		if (Dungeon.level != null) {
			for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob : Dungeon.level.mobs) {
				if (mob instanceof Pet && mob.isAlive()) {
					if (hero != null && hero.pet == null) {
						hero.pet = (Pet) mob;
					}
					return (Pet) mob;
				}
			}
		}
		return null;
	}

	@Override
	public void onClick() {
		super.onClick();
		Hero hero = Dungeon.hero;
		Pet pet = getActivePet();
		if (pet != null) {
			Sample.INSTANCE.play(Assets.Sounds.CLICK);
			GameScene.show(new WndPet(pet, false));
		} else if (hero != null && hero.storedPet != null) {
			Pet stored = Pet.createFromBundle(hero.storedPet);
			if (stored != null) {
				Sample.INSTANCE.play(Assets.Sounds.CLICK);
				GameScene.show(new WndPet(stored, true));
			}
		}
	}

	@Override
	public void update() {
		super.update();

		if (!com.shatteredpixel.shatteredpixeldungeon.SPDSettings.showPetPanel()) {
			if (visible) {
				visible = false;
				currentPet = null;
			}
			return;
		}

		Hero hero = Dungeon.hero;
		Pet pet = getActivePet();

		if (pet != null) {
			if (!visible || currentPet != pet || lastStored) {
				visible = true;
				currentPet = pet;
				lastStored = false;
				updateVisuals(pet, false);
			}
			updateHp(pet.HP, pet.HT);
		} else if (hero != null && hero.storedPet != null) {
			if (!visible || !lastStored) {
				visible = true;
				currentPet = null;
				lastStored = true;
				Pet stored = Pet.createFromBundle(hero.storedPet);
				if (stored != null) {
					updateVisuals(stored, true);
					updateHp(stored.HP, stored.HT);
				}
			}
		} else {
			if (visible) {
				visible = false;
				currentPet = null;
				lastStored = false;
			}
		}
	}

	private void updateVisuals(Pet pet, boolean stored) {
		if (pet.spriteClass != lastSpriteClass || sprite == null) {
			if (sprite != null) {
				sprite.killAndErase();
				sprite = null;
			}
			try {
				sprite = Reflection.newInstance(pet.spriteClass);
				sprite.linkVisuals(pet);
				sprite.idle();
				sprite.paused = false;
				if (sprite.width() > 18 || sprite.height() > 18) {
					sprite.scale.set(PixelScene.align(18f / Math.max(sprite.width(), sprite.height())));
				}
				add(sprite);
			} catch (Exception ignored) {}
			lastSpriteClass = pet.spriteClass;
		}

		if (sprite != null) {
			sprite.alpha(stored ? 0.5f : 1.0f);
		}

		if (stored) {
			bg.hardlight(0.3f, 0.3f, 0.5f);
			statusBadge.text("Z");
			statusBadge.hardlight(0xFFCCDDEE);
		} else if (pet.currentOrder == Pet.PetOrder.DEFEND) {
			bg.hardlight(0.6f, 0.4f, 0.1f);
			statusBadge.text("D");
			statusBadge.hardlight(0xFFFFE533);
		} else {
			bg.hardlight(0.2f, 0.6f, 0.3f);
			statusBadge.text("");
		}

		bringToFront(hpBg);
		bringToFront(hpBar);
		bringToFront(statusBadge);

		layout();
	}

	private void updateHp(long curHP, long maxHP) {
		float ratio = Math.max(0f, Math.min(1f, (float) curHP / (float) maxHP));
		float barW = 18f * ratio;
		hpBar.size(Math.max(1, barW), 2);

		if (ratio > 0.5f) {
			hpBar.color(0xFF44CC44);
		} else if (ratio > 0.25f) {
			hpBar.color(0xFFCCCC44);
		} else {
			hpBar.color(0xFFCC4444);
		}
	}

	@Override
	public void layout() {
		if (camera != null) {
			RectF insets = DeviceCompat.getSafeInsets();
			insets = insets.scale(1f / camera.zoom);

			boolean portrait = (com.shatteredpixel.shatteredpixeldungeon.SPDSettings.interfaceSize() == 0);
			float posX = insets.left + 4;
			float posY = portrait ? 38 : 4;

			this.x = posX;
			this.y = posY;
			this.width = SIZE;
			this.height = SIZE;
		}

		super.layout();

		if (bg != null) {
			bg.x = x;
			bg.y = y;
			bg.size(width, height);
		}

		if (sprite != null) {
			sprite.x = x + (width - sprite.width()) / 2f;
			sprite.y = y + (height - sprite.height()) / 2f - 1;
			PixelScene.align(sprite);
		}

		if (hpBg != null) {
			hpBg.x = x + 3;
			hpBg.y = y + height - 4;
			hpBg.size(18, 2);
		}

		if (hpBar != null) {
			hpBar.x = x + 3;
			hpBar.y = y + height - 4;
		}

		if (statusBadge != null) {
			statusBadge.setPos(x + width - 7, y + 2);
		}
	}
}
