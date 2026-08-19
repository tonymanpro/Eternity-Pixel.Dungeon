/*
 * Eternity Pixel Dungeon
 * Copyright (C) 2026 Eternity PD Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

public class ManticoreSprite extends MobSprite {

	public ManticoreSprite() {
		super();

		texture( Assets.Sprites.MANTICORE );

		TextureFilm frames = new TextureFilm( texture, 16, 16 );

		idle = new Animation( 10, true );
		idle.frames( frames, 0, 1, 2, 1 );

		run = new Animation( 14, true );
		run.frames( frames, 0, 1, 2, 3 );

		attack = new Animation( 14, false );
		attack.frames( frames, 4, 5, 6, 0 );

		die = new Animation( 12, false );
		die.frames( frames, 7, 8, 9 );

		play( idle );
	}
}
