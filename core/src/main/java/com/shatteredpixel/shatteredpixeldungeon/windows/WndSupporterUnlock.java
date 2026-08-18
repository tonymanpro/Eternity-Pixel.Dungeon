/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Eternity Pixel Dungeon
 * Copyright (C) 2026 Eternity Pixel Dungeon Contributors
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

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.services.platform.SupporterManager;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

public class WndSupporterUnlock extends Window {

	protected static final int WIDTH_P = 140;
	protected static final int WIDTH_L = 220;
	protected static final int GAP = 4;

	public WndSupporterUnlock() {
		super();

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		IconTitle title = new IconTitle(Icons.get(Icons.BADGES), Messages.get(this, "title"));
		title.setRect(0, 0, width, 0);
		add(title);

		boolean isSupporter = SupporterManager.isSupporter();
		SupporterManager.SupporterTier tier = SupporterManager.getActiveTier();

		String statusText = isSupporter
				? Messages.get(this, "status_active", tier.displayName())
				: Messages.get(this, "status_locked");

		String introText = Messages.get(this, "intro");

		RenderedTextBlock text = PixelScene.renderTextBlock(6);
		text.text(statusText + "\n\n" + introText, width);
		text.setPos(0, title.bottom() + GAP);
		add(text);

		float pos = text.bottom() + GAP * 2;

		if (!isSupporter) {
			RedButton btnEnterKey = new RedButton(Messages.get(this, "btn_enter_key")) {
				@Override
				public void onClick() {
					hide();
					ShatteredPixelDungeon.scene().addToFront(new WndTextInput(
							Messages.get(WndSupporterUnlock.class, "key_dialog_title"),
							Messages.get(WndSupporterUnlock.class, "key_dialog_msg"),
							"",
							32,
							false,
							Messages.get(WndSupporterUnlock.class, "btn_activate"),
							Messages.get(WndSupporterUnlock.class, "btn_cancel")
					) {
						@Override
						public void onSelect(boolean positive, String text) {
							if (positive && text != null && !text.trim().isEmpty()) {
								boolean success = SupporterManager.activateKey(text);
								if (success) {
									ShatteredPixelDungeon.scene().addToFront(new WndMessage(Messages.get(WndSupporterUnlock.class, "key_success")));
									if (ShatteredPixelDungeon.scene() instanceof com.shatteredpixel.shatteredpixeldungeon.scenes.HeroSelectScene) {
										ShatteredPixelDungeon.switchNoFade(com.shatteredpixel.shatteredpixeldungeon.scenes.HeroSelectScene.class);
									}
								} else {
									ShatteredPixelDungeon.scene().addToFront(new WndMessage(Messages.get(WndSupporterUnlock.class, "key_invalid")));
								}
							}
						}
					});
				}
			};
			btnEnterKey.setRect(0, pos, width, 18);
			add(btnEnterKey);
			pos = btnEnterKey.bottom() + GAP;

			RedButton btnWeb = new RedButton(Messages.get(this, "btn_web")) {
				@Override
				public void onClick() {
					ShatteredPixelDungeon.platform.openURI("https://eternity-pixel-dungeon.web.app/#editions");
				}
			};
			btnWeb.setRect(0, pos, width, 18);
			add(btnWeb);
			pos = btnWeb.bottom() + GAP;
		} else {
			RedButton btnDeactivate = new RedButton(Messages.get(this, "btn_deactivate")) {
				@Override
				public void onClick() {
					SupporterManager.deactivate();
					hide();
					ShatteredPixelDungeon.scene().addToFront(new WndSupporterUnlock());
					if (ShatteredPixelDungeon.scene() instanceof com.shatteredpixel.shatteredpixeldungeon.scenes.HeroSelectScene) {
						ShatteredPixelDungeon.switchNoFade(com.shatteredpixel.shatteredpixeldungeon.scenes.HeroSelectScene.class);
					}
				}
			};
			btnDeactivate.setRect(0, pos, width, 16);
			add(btnDeactivate);
			pos = btnDeactivate.bottom() + GAP;
		}

		resize(width, (int) pos);
	}
}
