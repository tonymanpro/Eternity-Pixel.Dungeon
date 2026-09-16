/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2019-2024 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Perks;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class WndPerks extends Window {

    private final int WIDTH = Math.min(138, (int) (PixelScene.uiCamera.width * 0.9));
    private final int HEIGHT = (int) (PixelScene.uiCamera.height * 0.9);
    private static final int TTL_HEIGHT    = 18;
    private static final int BTN_HEIGHT    = 18;
    private static final int GAP        = 1;

    private ArrayList<IconButton> infos = new ArrayList<>();
    private ArrayList<ConduitBox> boxes = new ArrayList<>();
    private ScrollPane pane;

    public WndPerks(ArrayList<Perks.Perk> perks, boolean editable ) {

        super();

        resize(WIDTH, HEIGHT);

        RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get(this, "title"), 12 );
        title.hardlight( TITLE_COLOR );
        title.setPos(
                (WIDTH - title.width()) / 2,
                (TTL_HEIGHT - title.height()) / 2
        );
        PixelScene.align(title);
        add( title );

        int totalPerks = Perks.Perk.values().length;
        int unlockedCount = perks.size();

        String countStr = Messages.get(this, "unlocked", unlockedCount, totalPerks);
        RenderedTextBlock countText = PixelScene.renderTextBlock( countStr, 8 );
        countText.hardlight( Window.TITLE_COLOR );
        countText.setPos( 4, title.bottom() + 1 );
        add( countText );

        String nextStr;
        if (unlockedCount >= totalPerks) {
            nextStr = Messages.get(this, "all_unlocked");
        } else {
            int nextLvl = Perks.nextPerkLevel();
            int currentLvl = (Dungeon.hero != null) ? Dungeon.hero.lvl : 1;
            int remaining = Math.max(0, nextLvl - currentLvl);
            nextStr = Messages.get(this, "next", nextLvl, remaining);
        }
        RenderedTextBlock nextText = PixelScene.renderTextBlock( nextStr, 7 );
        nextText.setPos( WIDTH - nextText.width() - 4, title.bottom() + 1.5f );
        if (countText.right() + 4 > nextText.left()) {
            nextText.setPos( 4, countText.bottom() + 1 );
        }
        add( nextText );

        float barY = Math.max(countText.bottom(), nextText.bottom()) + 2;

        ColorBlock barBg = new ColorBlock( WIDTH - 8, 3, 0xFF222832 );
        barBg.x = 4;
        barBg.y = barY;
        add( barBg );

        int prevLvl = Perks.prevPerkLevel();
        int nextLvl = Perks.nextPerkLevel();
        int heroLvl = (Dungeon.hero != null) ? Dungeon.hero.lvl : 1;
        float progress = 1f;
        if (unlockedCount < totalPerks && nextLvl > prevLvl) {
            progress = Math.max(0f, Math.min(1f, (float)(heroLvl - prevLvl) / (nextLvl - prevLvl)));
        }
        int barFillW = Math.round((WIDTH - 8) * progress);
        if (barFillW > 0) {
            ColorBlock barFill = new ColorBlock( barFillW, 3, 0xFF44AAEE );
            barFill.x = 4;
            barFill.y = barY;
            add( barFill );
        }

        float paneTop = barY + 3 + 3;

        pane = new ScrollPane(new Component()) {
            @Override
            public void onClick(float x, float y) {
                int size = boxes.size();
                if (editable) {
                    for (int i = 0; i < size; i++) {
                        if (boxes.get(i).onClick(x, y)) break;
                    }
                }
                size = infos.size();
                for (int i = 0; i < size; i++) {
                    if (infos.get(i).inside(x, y)) {

                        String message = perks.get(i).desc();
                        String title = Messages.titleCase(perks.get(i).toString());
                        ShatteredPixelDungeon.scene().add(
                                new WndTitledMessage(
                                        Icons.get(Icons.INFO),
                                        title, message)
                        );

                        break;
                    }
                }
            }
        };
        add(pane);
        pane.setRect(0, paneTop, WIDTH, HEIGHT - paneTop);
        Component content = pane.content();

        float pos = 2;
        if (perks.isEmpty()) {
            RenderedTextBlock emptyPrompt = PixelScene.renderTextBlock(
                    Messages.get(this, "empty", nextLvl), 8 );
            emptyPrompt.maxWidth( WIDTH - 12 );
            emptyPrompt.setPos( 6, 10 );
            PixelScene.align( emptyPrompt );
            content.add( emptyPrompt );
            pos = emptyPrompt.bottom() + 10;
        } else {
            for (Perks.Perk i : perks) {

                final String challenge = i.toString();

                ConduitBox cb = new ConduitBox(challenge);
                cb.active = editable;
                cb.conduct = i;

                pos += GAP;
                cb.setRect(0, pos, WIDTH-16, BTN_HEIGHT);

                content.add(cb);
                boxes.add(cb);
                IconButton info = new IconButton(Icons.get(Icons.INFO)) {
                    @Override
                    protected void layout() {
                        super.layout();
                        hotArea.y = -5000;
                    }
                };
                info.setRect(cb.right(), pos, 16, BTN_HEIGHT);
                content.add(info);
                infos.add(info);

                pos = cb.bottom();
            }
        }

        content.setSize(WIDTH, pos);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public static class ConduitBox extends RedButton {

        public Perks.Perk conduct;

        public ConduitBox(String label) {
            super(label);
        }

        @Override
        public void onClick() {
            super.onClick();
        }

        protected boolean onClick(float x, float y) {
            if (!inside(x, y)) return false;
            Sample.INSTANCE.play(Assets.Sounds.CLICK);
            onClick();
            return true;
        }

        @Override
        protected void layout() {
            super.layout();
            hotArea.width = hotArea.height = 0;
        }
    }
}
