/*
 * Eternity Pixel Dungeon
 * Cinematic Boss Splash Banner
 */

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.NinePatch;

public class BossSplashBanner extends Group {

	private static final float TOTAL_TIME = 3.6f;
	private static final float FADE_IN = 0.4f;
	private static final float FADE_OUT = 0.5f;

	private ColorBlock bgDim;
	private NinePatch frame;
	private RenderedTextBlock txtTitle;
	private RenderedTextBlock txtSubtitle;

	private float timePassed = 0f;
	private int bossPos;

	public BossSplashBanner(String title, String subtitle, int titleColor, int bossCell) {
		super();
		this.bossPos = bossCell;
		camera = PixelScene.uiCamera;

		// 1. Fullscreen dark backdrop letterbox with subtle tint
		bgDim = new ColorBlock(camera.width, camera.height, 0x000000);
		bgDim.alpha(0f);
		bgDim.camera = camera;
		add(bgDim);

		// 2. Center frame panel
		frame = Chrome.get(Chrome.Type.TOAST_TR_HEAVY);
		frame.hardlight(0.12f, 0.05f, 0.15f);
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
		txtSubtitle.hardlight(0xCCCCCC);
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
		float bannerY = (camH - bannerH) / 2f - 15f;

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

		bgDim.alpha(curAlpha * 0.65f);
		frame.alpha(curAlpha * 0.95f);
		txtTitle.alpha(curAlpha);
		txtSubtitle.alpha(curAlpha * 0.9f);

		// Burst dramatic dark particles around the boss position during the intro
		if (timePassed < 1.0f && bossPos >= 0) {
			CellEmitter.get(bossPos).burst(ShadowParticle.CURSE, 1);
		}
	}

	public static void show(String title, String subtitle, int titleColor, int bossCell) {
		Game.runOnRenderThread(new com.watabou.utils.Callback() {
			@Override
			public void call() {
				GameScene scene = (GameScene) Game.scene();
				if (scene != null) {
					BossSplashBanner banner = new BossSplashBanner(title, subtitle, titleColor, bossCell);
					scene.add(banner);
				}
			}
		});
	}
}
