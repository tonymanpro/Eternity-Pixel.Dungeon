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

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Skeleton;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.noosa.tweeners.ScaleTweener;
import com.watabou.utils.AssetPackResolver;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class MobSprite extends CharSprite {

	private static final float FADE_TIME	= 3f;
	private static final float FALL_TIME	= 1f;
	
	@Override
	public void link( Char ch ) {
		super.link( ch );
		if (com.shatteredpixel.shatteredpixeldungeon.HolidayEventConfig.get().isWinterEventActive()
				&& com.shatteredpixel.shatteredpixeldungeon.HolidayEventConfig.get().santaHatEnabled) {
			if (ch instanceof Rat || ch instanceof Skeleton || ch instanceof Gnoll || ch instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief) {
				this.hardlight(0xff7777);
			}
		}
	}

	@Override
	public void update() {
		sleeping = ch != null && ch.isAlive() && ((Mob)ch).state == ((Mob)ch).SLEEPING;
		super.update();
	}
	
	@Override
	public void onComplete( Animation anim ) {
		
		super.onComplete( anim );
		
		if (anim == die && parent != null) {
			parent.add( new AlphaTweener( this, 0, FADE_TIME ) {
				@Override
				protected void onComplete() {
					MobSprite.this.killAndErase();
				}
			} );
		}
	}
	
	public void fall() {
		
		origin.set( width / 2, height - DungeonTilemap.SIZE / 2 );
		angularSpeed = Random.Int( 2 ) == 0 ? -720 : 720;
		am = 1;

		hideEmo();

		if (health != null){
			health.killAndErase();
		}
		
		parent.add( new ScaleTweener( this, new PointF( 0, 0 ), FALL_TIME ) {
			@Override
			protected void onComplete() {
				MobSprite.this.killAndErase();
				parent.erase( this );
			}
			@Override
			protected void updateValues( float progress ) {
				super.updateValues( progress );
				y += 12 * Game.elapsed;
				am = 1 - progress;
			}
		} );
	}

	public TextureFilm createFilm( int baseWidth, int baseHeight ) {
		try {
			int scaleFactor = 1;
			if (texture != null && texture.path != null) {
				String resolved = AssetPackResolver.resolvePath( texture.path );
				if (resolved.contains("/hd/") || resolved.contains("\\hd\\")) {
					scaleFactor = 4;
				}
			}
			if (scaleFactor == 1 && texture != null) {
				if (texture.width >= 1000) {
					scaleFactor = 4;
				} else if (texture.width >= 500) {
					scaleFactor = 2;
				}
			}

			if (scaleFactor == 4) {
				scale.set( 0.25f, 0.25f );
				TextureFilm film = new TextureFilm( texture, baseWidth * 4, baseHeight * 4 );
				film.densityScale( 4f );
				return film;
			} else if (scaleFactor == 2) {
				scale.set( 0.5f, 0.5f );
				TextureFilm film = new TextureFilm( texture, baseWidth * 2, baseHeight * 2 );
				film.densityScale( 2f );
				return film;
			}
		} catch (RuntimeException e) {
			Game.reportException(e);
		}

		scale.set( 1f, 1f );
		return new TextureFilm( texture, baseWidth, baseHeight );
	}
}
