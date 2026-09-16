package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class CrystalMazeRoom extends SpecialRoom {

    @Override
    public boolean canConnect(Point p) {
        return super.canConnect(p) && ((p.x > left + 1 && p.x < right - 1) || (p.y > top + 1 && p.y < bottom - 1));
    }

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.EMPTY_SP);

        int inset = 2;
        boolean gapOnLeft = Random.Int(2) == 0;
        while (left + inset + 1 < right - inset - 1 && top + inset + 1 < bottom - inset - 1) {

            int ringLeft = left + inset;
            int ringTop = top + inset;
            int ringRight = right - inset;
            int ringBottom = bottom - inset;
            int ringWidth = ringRight - ringLeft + 1;
            int ringHeight = ringBottom - ringTop + 1;

            Painter.fill(level, ringLeft, ringTop, ringWidth, 1, Terrain.REGION_DECO);
            Painter.fill(level, ringLeft, ringTop, 1, ringHeight, Terrain.REGION_DECO);
            Painter.fill(level, ringLeft, ringBottom, ringWidth, 1, Terrain.REGION_DECO);
            Painter.fill(level, ringRight, ringTop, 1, ringHeight, Terrain.REGION_DECO);

            int gapX = gapOnLeft ? ringLeft : ringRight;
            int gapY = ringTop + 1 + Random.Int(Math.max(1, ringHeight - 2));
            Painter.set(level, new Point(gapX, gapY), Terrain.EMPTY_SP);
            gapOnLeft = !gapOnLeft;

            inset += 2;
        }

        Door entrance = entrance();
        if (entrance != null) {
            entrance.set(Door.Type.UNLOCKED);
            Painter.drawInside(level, this, entrance, Math.max(width(), height()) / 2 + 1, Terrain.EMPTY_SP);
        }

        for (Door door : connected.values()) {
            door.set(Door.Type.UNLOCKED);
            Painter.drawInside(level, this, door, Math.max(width(), height()) / 2 + 1, Terrain.EMPTY_SP);
        }
    }
}
