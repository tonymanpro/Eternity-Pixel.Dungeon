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
 * Eternity Pixel Dungeon
 * Copyright (C) 2026 Eternity PD Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.SeasonalTasks;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.Archs;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class BattlePassSeasonalTasksScene extends PixelScene {

	private static final int ROW_GAP    = 3;
	private static final int MARGIN     = 8;
	private static final int FOOTER_H   = 28;

	private final ArrayList<TaskRow> rows = new ArrayList<>();

	@Override
	public void create() {
		super.create();

		int w = Camera.main.width;
		int h = Camera.main.height;

		Archs archs = new Archs();
		archs.setSize( w, h );
		add( archs );

		RenderedTextBlock title = renderTextBlock( Messages.get( this, "title" ), 12 );
		title.maxWidth( (int)(w - MARGIN * 2) );
		title.hardlight( 0xFFD700 );
		title.setPos( (w - title.width()) / 2f, 6 );
		align( title );
		add( title );

		RenderedTextBlock notice = renderTextBlock( Messages.get( this, "notice" ), 9 );
		notice.maxWidth( (int)(w - MARGIN * 2) );
		notice.hardlight( 0xCACFC2 );
		notice.setPos( (w - notice.width()) / 2f, title.bottom() + 4 );
		align( notice );
		add( notice );

		Component content = new Component();

		rows.clear();
		int y = 0;
		if (SeasonalTasks.tasks.isEmpty()) {
			RenderedTextBlock empty = renderTextBlock( Messages.get( this, "empty" ), 9 );
			empty.maxWidth( (int)(w - MARGIN * 2) );
			empty.setPos( 0, 0 );
			content.add( empty );
			y = (int) empty.height();
		} else {
			for (SeasonalTasks.Task task : SeasonalTasks.tasks) {
				TaskRow row = new TaskRow( task );
				row.setSize( w - MARGIN * 2, 0 );
				row.layout();
				row.setRect( 0, y, w - MARGIN * 2, row.height() );
				content.add( row );
				rows.add( row );
				y += row.height() + ROW_GAP;
			}
		}
		content.setSize( w - MARGIN*2, Math.max( 0, y - ROW_GAP ) );

		int listTop = (int) notice.bottom() + 6;
		int listHeight = h - listTop - FOOTER_H;
		ScrollPane list = new ScrollPane( content ) {
			@Override
			public void onClick( float x, float y ) {
				for (TaskRow row : rows) {
					if (row.tryClaimClick( x, y )) {
						break;
					}
				}
			}
		};
		add( list );
		list.setRect( MARGIN, listTop, w - MARGIN*2, listHeight );

		StyledButton btnBack = new StyledButton( Chrome.Type.GREY_BUTTON_TR, Messages.get( this, "back" ) ){
			@Override
			public void onClick(){
				onBackPressed();
			}
		};
		btnBack.setRect( (w - 100) / 2f, h - FOOTER_H + 4, 100, FOOTER_H - 8 );
		add( btnBack );

		fadeIn();
	}

	@Override
	protected void onBackPressed(){
		ShatteredPixelDungeon.switchNoFade( BattlePassScene.class );
	}

	private void refresh(){
		for (TaskRow row : rows) {
			row.layout();
		}
	}

	private class TaskRow extends Component {

		private final SeasonalTasks.Task task;
		private RenderedTextBlock label;
		private StyledButton btnClaim;

		TaskRow( SeasonalTasks.Task task ){
			this.task = task;
		}

		@Override
		protected void createChildren(){
			label = PixelScene.renderTextBlock( 9 );
			add( label );

			btnClaim = new StyledButton( Chrome.Type.RED_BUTTON, Messages.get( BattlePassSeasonalTasksScene.class, "claim" ), 8 ){
				@Override
				public void onClick(){
					claim();
				}
			};
			add( btnClaim );
		}

		private void claim(){
			if (SeasonalTasks.claim( task )) {
				GLog.p( Messages.get( BattlePassSeasonalTasksScene.class, "task_claimed" ) );
				refresh();
			}
		}

		boolean tryClaimClick( float x, float y ){
			if (btnClaim.visible && btnClaim.active
					&& x >= btnClaim.left() && x <= btnClaim.right()
					&& y >= btnClaim.top() && y <= btnClaim.bottom()) {
				claim();
				return true;
			}
			return false;
		}

		@Override
		protected void layout(){
			String status = task.claimed
					? Messages.get( BattlePassSeasonalTasksScene.class, "claimed" )
					: task.completed
					? Messages.get( BattlePassSeasonalTasksScene.class, "ready" )
					: task.progress + "/" + task.target;

			btnClaim.visible = btnClaim.active = task.completed && !task.claimed;

			label.text( task.description() + "\n" + status );
			float maxTextW = Math.max(10, width() - (btnClaim.visible ? 48 : 8));
			label.maxWidth( (int)maxTextW );

			float minH = 26;
			float reqH = Math.max(minH, label.height() + 6);
			height = reqH;

			if (btnClaim.visible) {
				btnClaim.setRect( x + width() - 44, y + (reqH - 18) / 2f, 40, 18 );
			}
			label.setPos( x + 4, y + (reqH - label.height()) / 2f );
		}
	}
}