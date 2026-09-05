/*
 * Eternity Pixel Dungeon
 * Santa Hat Visual Overlay for Mobs (Winter Event Feature)
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.Image;

public class SantaHatOverlay extends Image {

	protected CharSprite owner;

	public SantaHatOverlay(CharSprite owner) {
		super(Assets.Sprites.SANTA_HAT);
		this.owner = owner;
		scale.set(0.65f, 0.65f);
		GameScene.add(this);
	}

	@Override
	public void update() {
		super.update();

		if (owner != null && owner.visible && texture != null) {
			visible = true;
			flipHorizontal = owner.flipHorizontal;
			// Position snugly directly on top of the mob's head
			x = owner.x + (owner.width() - width()) / 2f + (owner.flipHorizontal ? 1f : -1f);
			y = owner.y - 5f;
		} else {
			visible = false;
		}

		if (owner != null && owner.ch != null && !owner.ch.isAlive()) {
			killAndErase();
		}
	}
}
