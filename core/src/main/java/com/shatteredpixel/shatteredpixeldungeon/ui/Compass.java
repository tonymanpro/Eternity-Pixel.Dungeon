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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.LostBackpack;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.utils.PointF;

public class Compass extends Image {

	private static final float RAD_2_G	= 180f / 3.1415926f;
	private static final float RADIUS	= 12;
	
	private int cell;
	private PointF cellCenter;
	
	private PointF lastScroll = new PointF();
	
	public Compass( int cell ) {
		
		super();
		copy( Icons.COMPASS.get() );
		origin.set( width / 2, RADIUS );
		
		this.cell = cell;
		cellCenter = DungeonTilemap.tileCenterToWorld( cell );
		visible = false;
	}
	
	@Override
	public void update() {
		super.update();
		
		int targetCell = cell;
		boolean isDeathTarget = false;
		
		if (Dungeon.hero != null && Dungeon.hero.belongings.lostInventory() && Dungeon.level != null) {
			for (Heap h : Dungeon.level.heaps.valueList()) {
				if (h.peek() instanceof LostBackpack) {
					targetCell = h.pos;
					isDeathTarget = true;
					break;
				}
			}
		}
		
		if (targetCell < 0 || targetCell >= Dungeon.level.length()){
			visible = false;
			return;
		}
		
		if (isDeathTarget) {
			visible = true;
			tint( 0xFF3333, 0.85f );
		} else {
			resetColor();
			visible = Dungeon.level.visited[targetCell] || Dungeon.level.mapped[targetCell];
		}
		
		if (visible) {
			cellCenter = DungeonTilemap.tileCenterToWorld( targetCell );
			PointF scroll = Camera.main.scroll;
			if (!scroll.equals( lastScroll )) {
				lastScroll.set( scroll );
				PointF center = Camera.main.center().offset( scroll );
				angle = (float)Math.atan2( cellCenter.x - center.x, center.y - cellCenter.y ) * RAD_2_G;
			}
		}
	}
}
