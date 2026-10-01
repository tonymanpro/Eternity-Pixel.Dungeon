package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Point;

public class SanctuaryRoom extends SpecialRoom {

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.EMPTY_SP);

        Point center = center();
        Painter.fill(level, center.x - 1, center.y - 1, 3, 3, Terrain.EMPTY_SP);
        Painter.set(level, center, Terrain.STATUE_SP);

        int px = 2;
        int py = 1;
        Point[] pillars = {
                new Point(center.x - px, center.y - py),
                new Point(center.x + px, center.y - py),
                new Point(center.x - px, center.y + py),
                new Point(center.x + px, center.y + py),
        };
        for (Point p : pillars) {
            if (p.x > left + 1 && p.x < right - 1 && p.y > top + 1 && p.y < bottom - 1) {
                Painter.set(level, p, Terrain.REGION_DECO_ALT);
            }
        }

        level.addItemToSpawn( new IronKey( Dungeon.depth ) );
        entrance().set(Door.Type.LOCKED);
        Painter.drawInside(level, this, entrance(), 2, Terrain.EMPTY_SP);
    }
}
