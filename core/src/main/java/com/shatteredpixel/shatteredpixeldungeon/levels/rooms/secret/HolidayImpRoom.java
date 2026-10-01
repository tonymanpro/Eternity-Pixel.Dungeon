/*
 * Eternity Pixel Dungeon
 * Holiday Imp Secret Room (Christmas Event Special Room)
 */

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.HolidayImp;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.holiday.HolidayGift;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;

public class HolidayImpRoom extends SecretRoom {

	@Override
	public int maxHeight() {
		return 8;
	}

	@Override
	public int maxWidth() {
		return 8;
	}

	@Override
	public int minHeight() {
		return 6;
	}

	@Override
	public int minWidth() {
		return 6;
	}

	@Override
	public void paint(Level level) {
		Painter.fill(level, this, Terrain.WALL);
		Painter.fill(level, this, 1, Terrain.EMPTY_SP);

		Door entrance = entrance();
		entrance.set(Door.Type.HIDDEN);
		int door = entrance.x + entrance.y * level.width();

		// Add decorative corners with festive gifts or chests
		int c1 = (top + 1) * level.width() + (left + 1);
		int c2 = (top + 1) * level.width() + (right - 1);
		int c3 = (bottom - 1) * level.width() + (left + 1);
		int c4 = (bottom - 1) * level.width() + (right - 1);

		for (int pos : new int[]{c1, c2, c3, c4}) {
			if (pos != door) {
				level.drop(new HolidayGift(), pos).type = Heap.Type.CHEST;
			}
		}

		// Place Holiday Imp in center
		HolidayImp imp = new HolidayImp();
		imp.pos = level.pointToCell(center());
		level.mobs.add(imp);
	}
}
