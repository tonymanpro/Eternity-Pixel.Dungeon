/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.exit;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;
import com.watabou.utils.Point;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class ExitRoom extends StandardRoom {
	
	@Override
	public int minWidth() {
		return Math.max(super.minWidth(), 5);
	}
	
	@Override
	public int minHeight() {
		return Math.max(super.minHeight(), 5);
	}

	@Override
	public boolean isExit() {
		return true;
	}

	public void paint(Level level) {

		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY );
		
		for (Room.Door door : connected.values()) {
			door.set( Room.Door.Type.REGULAR );
		}
		
		int exit = level.pointToCell(getExitPoint(this, level));
		Painter.set( level, exit, Terrain.EXIT );
		level.transitions.add(new LevelTransition(level, exit, LevelTransition.Type.REGULAR_EXIT));
	}

	public interface PointValidator {
		boolean isValid(Point p, int cell);
	}

	public static Point getExitPoint(Room room, Level level) {
		return getExitPoint(room, level, 2, null);
	}

	public static Point getExitPoint(Room room, Level level, int margin) {
		return getExitPoint(room, level, margin, null);
	}

	public static Point getExitPoint(Room room, Level level, int margin, PointValidator validator) {
		Room.Door entrance = null;
		if (!room.connected.isEmpty()) {
			entrance = room.connected.values().iterator().next();
		}

		Point target = new Point(room.center());
		if (entrance != null) {
			if (entrance.y <= room.top || entrance.y >= room.bottom) {
				int minX = Math.min(room.left + margin, room.center().x);
				int maxX = Math.max(room.right - margin, room.center().x);
				target.x = Math.max(minX, Math.min(maxX, entrance.x));
				target.y = room.center().y;
			} else {
				target.x = room.center().x;
				int minY = Math.min(room.top + margin, room.center().y);
				int maxY = Math.max(room.bottom - margin, room.center().y);
				target.y = Math.max(minY, Math.min(maxY, entrance.y));
			}
		}

		int targetCell = level.pointToCell(target);
		if (validator == null || validator.isValid(target, targetCell)) {
			return target;
		}

		// Find the closest valid point inside the room
		Point best = null;
		int bestDist = Integer.MAX_VALUE;

		for (int y = room.top + margin; y <= room.bottom - margin; y++) {
			for (int x = room.left + margin; x <= room.right - margin; x++) {
				Point p = new Point(x, y);
				int c = level.pointToCell(p);
				if (validator.isValid(p, c)) {
					int dist = Math.abs(x - target.x) + Math.abs(y - target.y);
					if (dist < bestDist) {
						bestDist = dist;
						best = p;
					}
				}
			}
		}

		return best != null ? best : target;
	}
	
	@Override
	public boolean canPlaceCharacter(Point p, Level l) {
		return super.canPlaceCharacter(p, l) && l.pointToCell(p) != l.exit();
	}

	private static ArrayList<Class<?extends StandardRoom>> rooms = new ArrayList<>();
	static {
        rooms.add(WaterBridgeExitRoom.class);
        rooms.add(RegionDecoPatchExitRoom.class);
        rooms.add(RingExitRoom.class);
        rooms.add(CircleBasinExitRoom.class);

        rooms.add(RegionDecoLineExitRoom.class);
        rooms.add(ChasmBridgeExitRoom.class);
        rooms.add(PillarsExitRoom.class);
        rooms.add(CellBlockExitRoom.class);

        rooms.add(CaveExitRoom.class);
        rooms.add(RegionDecoBridgeExitRoom.class);
        rooms.add(CavesFissureExitRoom.class);
        rooms.add(CircleWallExitRoom.class);

        rooms.add(HallwayExitRoom.class);
        rooms.add(StatuesExitRoom.class);
        rooms.add(LibraryHallExitRoom.class);
        rooms.add(LibraryRingExitRoom.class);

        rooms.add(RegionDecoPatchExitRoom.class);
        rooms.add(RuinsExitRoom.class);
        rooms.add(ChasmExitRoom.class);
        rooms.add(RitualExitRoom.class);
	}

	private static float[][] chances = new float[27][];
	static {
        //floor 1 only uses simpler exit rooms
        chances[1] =  new float[]{4,3,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0};
        chances[2] =  new float[]{4,3,2,1, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0};
        chances[5] =  chances[4] = chances[3] = chances[2];

        chances[6] = new float[]{0,0,0,0,  4,3,2,1, 0,0,0,0, 0,0,0,0, 0,0,0,0};
        chances[10] = chances[9] = chances[8] = chances[7] = chances[6];

        chances[11] = new float[]{0,0,0,0, 0,0,0,0, 4,3,2,1, 0,0,0,0, 0,0,0,0};
        chances[15] = chances[14] = chances[13] = chances[12] = chances[11];

        chances[16] = new float[]{0,0,0,0, 0,0,0,0, 0,0,0,0, 4,3,2,1, 0,0,0,0};
        chances[20] = chances[19] = chances[18] = chances[17] = chances[16];

        chances[21] = new float[]{0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 4,3,2,1};
        chances[26] = chances[25] = chances[24] = chances[23] = chances[22] = chances[21];
	}

	public static StandardRoom createExit(){
		return Reflection.newInstance(rooms.get(Random.chances(chances[Dungeon.depth])));
	}
}
