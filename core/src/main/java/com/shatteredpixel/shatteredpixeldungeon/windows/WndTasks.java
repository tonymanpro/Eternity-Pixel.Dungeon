package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Tasks;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;

public class WndTasks extends Window {

	private static final int WIDTH      = 120;
	private static final int MIN_ROW_H  = 16;
	private static final int GAP        = 2;
	private static final int MARGIN     = 4;

	public WndTasks(){
		super();

		RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get( this, "title" ), 9 );
		title.hardlight( TITLE_COLOR );
		title.maxWidth( WIDTH - MARGIN * 2 );
		title.setPos( MARGIN, MARGIN );
		PixelScene.align( title );
		add( title );

		float y = title.bottom() + 4;

		for (Tasks.Task task : Tasks.tasks){
			String label = task.description();
			if (task.target > 1) {
				label += "  (" + task.progress + "/" + task.target + ")";
			}
			RenderedTextBlock text = PixelScene.renderTextBlock( label, 7 );
			text.hardlight( task.completed ? 0x88FF88 : 0xCACFC2 );
			text.maxWidth( WIDTH - MARGIN * 2 - 8 );

			float rowH = Math.max(MIN_ROW_H, text.height() + 4);

			ColorBlock bg = new ColorBlock( WIDTH - MARGIN*2, rowH, task.completed ? 0x2000FF00 : 0x20FFFFFF );
			bg.x = MARGIN;
			bg.y = y;
			add( bg );

			text.setPos( MARGIN + 4, y + (rowH - text.height()) / 2f );
			PixelScene.align( text );
			add( text );

			y += rowH + GAP;
		}

		if (Tasks.tasks.isEmpty()) {
			RenderedTextBlock empty = PixelScene.renderTextBlock( Messages.get( this, "none" ), 8 );
			empty.hardlight( 0x888888 );
			empty.maxWidth( WIDTH - MARGIN * 2 );
			empty.setPos( MARGIN, y );
			PixelScene.align( empty );
			add( empty );
			y = empty.bottom() + 4;
		}

		resize( WIDTH, (int)(y + MARGIN) );
	}
}