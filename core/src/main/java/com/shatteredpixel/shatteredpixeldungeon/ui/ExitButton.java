/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Experienced Pixel Dungeon
 * Copyright (C) 2019-2024 Trashbox Bobylev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndKeyBindings;
import com.watabou.input.GameAction;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;

public class ExitButton extends IconButton {

	private static final float NORMAL_BRIGHTNESS = 1.45f;
	private static final float PRESSED_BRIGHTNESS = 2.2f;

	public ExitButton() {
		super(Icons.EXIT.get());

		width = 20;
		height = 20;
		if (icon != null) {
			icon.brightness(NORMAL_BRIGHTNESS);
		}
	}

	@Override
	protected void onPointerDown() {
		if (icon != null) icon.brightness(PRESSED_BRIGHTNESS);
		Sample.INSTANCE.play(Assets.Sounds.CLICK);
	}

	@Override
	protected void onPointerUp() {
		if (icon != null) icon.brightness(NORMAL_BRIGHTNESS);
	}

	@Override
	public void enable(boolean value) {
		active = value;
		if (icon != null) {
			if (value) {
				icon.brightness(NORMAL_BRIGHTNESS);
				icon.alpha(1.0f);
			} else {
				icon.resetColor();
				icon.alpha(0.3f);
			}
		}
	}

	@Override
    public void onClick() {
		if (Game.scene() instanceof TitleScene) {
			Game.instance.finish();
		} else {
			ShatteredPixelDungeon.switchNoFade( TitleScene.class );
		}
	}

	@Override
	public GameAction keyAction() {
		return GameAction.BACK;
	}

	@Override
	protected String hoverText() {
		return Messages.titleCase(Messages.get(WndKeyBindings.class, "back"));
	}
}
