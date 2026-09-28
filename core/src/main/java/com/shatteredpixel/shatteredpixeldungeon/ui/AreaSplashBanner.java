/*
 * Eternity Pixel Dungeon
 * Cinematic Area Splash Banner
 */

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.NinePatch;

public class AreaSplashBanner extends Group {

	private static final float TOTAL_TIME = 3.2f;
	private static final float FADE_IN = 0.45f;
	private static final float FADE_OUT = 0.6f;

	private ColorBlock bgDim;
	private NinePatch frame;
	private RenderedTextBlock txtTitle;
	private RenderedTextBlock txtSubtitle;

	private float timePassed = 0f;

	public AreaSplashBanner(String title, String subtitle, int titleColor) {
		super();
		camera = PixelScene.uiCamera;

		// 1. Fullscreen dark backdrop letterbox with subtle tint
		bgDim = new ColorBlock(camera.width, camera.height, 0x000000);
		bgDim.alpha(0f);
		bgDim.camera = camera;
		add(bgDim);

		// 2. Center frame panel
		frame = Chrome.get(Chrome.Type.TOAST_TR_HEAVY);
		frame.hardlight(0.08f, 0.12f, 0.18f);
		frame.alpha(0f);
		frame.camera = camera;
		add(frame);

		// 3. Main Title
		txtTitle = PixelScene.renderTextBlock(title, 12);
		txtTitle.hardlight(titleColor);
		txtTitle.alpha(0f);
		txtTitle.camera = camera;
		add(txtTitle);

		// 4. Subtitle
		txtSubtitle = PixelScene.renderTextBlock(subtitle, 8);
		txtSubtitle.hardlight(0xDDDDDD);
		txtSubtitle.alpha(0f);
		txtSubtitle.camera = camera;
		add(txtSubtitle);

		layout();
	}

	private void layout() {
		float camW = camera.width;
		float camH = camera.height;

		bgDim.size(camW, camH);

		float textW = Math.max(txtTitle.width(), txtSubtitle.width());
		float bannerW = Math.min(camW - 20, textW + 36);
		float bannerH = txtTitle.height() + txtSubtitle.height() + 18;

		float bannerX = (camW - bannerW) / 2f;
		float bannerY = (camH - bannerH) / 2f - 20f;

		frame.x = bannerX;
		frame.y = bannerY;
		frame.size(bannerW, bannerH);

		txtTitle.setPos(
				bannerX + (bannerW - txtTitle.width()) / 2f,
				bannerY + 7
		);
		PixelScene.align(txtTitle);

		txtSubtitle.setPos(
				bannerX + (bannerW - txtSubtitle.width()) / 2f,
				txtTitle.bottom() + 4
		);
		PixelScene.align(txtSubtitle);
	}

	@Override
	public void update() {
		super.update();
		timePassed += Game.elapsed;

		float curAlpha;
		if (timePassed < FADE_IN) {
			curAlpha = timePassed / FADE_IN;
		} else if (timePassed < TOTAL_TIME - FADE_OUT) {
			curAlpha = 1.0f;
		} else if (timePassed < TOTAL_TIME) {
			curAlpha = (TOTAL_TIME - timePassed) / FADE_OUT;
		} else {
			killAndErase();
			return;
		}

		bgDim.alpha(curAlpha * 0.55f);
		frame.alpha(curAlpha * 0.95f);
		txtTitle.alpha(curAlpha);
		txtSubtitle.alpha(curAlpha * 0.9f);
	}

	public static void show(String title, String subtitle, int titleColor) {
		Game.runOnRenderThread(new com.watabou.utils.Callback() {
			@Override
			public void call() {
				GameScene scene = (GameScene) Game.scene();
				if (scene != null) {
					AreaSplashBanner banner = new AreaSplashBanner(title, subtitle, titleColor);
					scene.add(banner);
				}
			}
		});
	}
}
