/*
 * Eternity Pixel Dungeon
 * Steam Demo Completion / Victory Window
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

public class WndDemoVictory extends Window {

	public static final String STEAM_STORE_URL = System.getProperty(
			"eternity.steam_url",
			"https://store.steampowered.com/app/3241440/Eternity_Pixel_Dungeon/"
	);

	public WndDemoVictory() {
		super();

		int width = PixelScene.landscape() ? 190 : 130;

		IconTitle title = new IconTitle();
		if (Dungeon.hero != null) {
			title.icon(HeroSprite.avatar(Dungeon.hero.heroClass, 1));
		} else {
			title.icon(Icons.get(Icons.CHALLENGE_COLOR));
		}
		title.label(Messages.get(this, "title"));
		title.color(TITLE_COLOR);
		title.setRect(0, 0, width, 0);
		add(title);

		float pos = title.bottom() + 4;

		RenderedTextBlock txtIntro = PixelScene.renderTextBlock(Messages.get(this, "intro"), 6);
		txtIntro.maxWidth(width);
		txtIntro.setPos(0, pos);
		add(txtIntro);

		pos = txtIntro.bottom() + 6;

		RenderedTextBlock txtFeatures = PixelScene.renderTextBlock(Messages.get(this, "features"), 6);
		txtFeatures.maxWidth(width);
		txtFeatures.setPos(0, pos);
		add(txtFeatures);

		pos = txtFeatures.bottom() + 8;

		// Wishlist on Steam button (Golden highlight CTA)
		StyledButton btnWishlist = new StyledButton(Chrome.Type.RED_BUTTON, Messages.get(this, "wishlist")) {
			@Override
			public void onClick() {
				ShatteredPixelDungeon.platform.openURI(STEAM_STORE_URL);
			}
		};
		btnWishlist.icon(Icons.get(Icons.GOLD));
		btnWishlist.textColor(0xFFFF00);
		btnWishlist.setRect(0, pos, width, 22);
		add(btnWishlist);

		pos = btnWishlist.bottom() + 4;

		// Main menu button
		RedButton btnMenu = new RedButton(Messages.get(this, "menu")) {
			@Override
			public void onClick() {
				hide();
				try {
					Dungeon.saveAll();
				} catch (Exception e) {
					ShatteredPixelDungeon.reportException(e);
				}
				ShatteredPixelDungeon.switchScene(TitleScene.class);
			}
		};
		btnMenu.setRect(0, pos, width, 18);
		add(btnMenu);

		pos = btnMenu.bottom() + 4;

		// Close / explore remaining
		StyledButton btnClose = new StyledButton(Chrome.Type.GREY_BUTTON_TR, Messages.get(this, "close")) {
			@Override
			public void onClick() {
				hide();
			}
		};
		btnClose.setRect(0, pos, width, 16);
		add(btnClose);

		resize(width, (int)btnClose.bottom() + 2);
	}
}
