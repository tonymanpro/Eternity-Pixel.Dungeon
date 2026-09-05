/*
 * Eternity Pixel Dungeon
 * Hall of Fame Victory Build Inspection Window
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Rankings;
import com.shatteredpixel.shatteredpixeldungeon.VictoryBuild;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

import java.text.NumberFormat;
import java.util.Locale;

public class WndHallOfFameBuild extends Window {

	private static final int WIDTH  = 125;
	private static final int HEIGHT = 145;

	public WndHallOfFameBuild(Rankings.Record rec, VictoryBuild build) {
		super();
		resize(WIDTH, HEIGHT);

		IconTitle title = new IconTitle();
		title.icon(HeroSprite.avatar(rec.heroClass, rec.armorTier));
		String titleText = build != null && !build.heroSubclass.isEmpty() ?
				build.heroSubclass + " (" + rec.heroClass.title() + ")" : rec.heroClass.title();
		title.label(titleText.toUpperCase(Locale.ENGLISH));
		title.color(TITLE_COLOR);
		title.setRect(0, 0, WIDTH, 0);
		add(title);

		float pos = title.bottom() + 4;

		String runStatus = rec.win ? Messages.get(this, "victory") : (Messages.get(WndRanking.class, "depth") + " " + (build != null ? build.depth : rec.depth));
		RenderedTextBlock date = PixelScene.renderTextBlock(
				runStatus + " - " + rec.date, 7);
		date.hardlight(0xFFD700); // Gold title
		date.setPos(0, pos);
		add(date);
		pos += date.height() + 4;

		NumberFormat num = NumberFormat.getInstance(Locale.US);

		pos = addLine(Messages.get(WndRanking.class, "score"), num.format(rec.score), pos);
		if (build != null && !build.seed.isEmpty()) {
			pos = addLine(Messages.get(WndRanking.class, "seed"), build.seed, pos);
		}

		pos += 3;

		if (build != null) {
			if (!build.weaponName.isEmpty()) {
				pos = addLine(Messages.get(this, "weapon_label"), build.weaponName, pos);
			}
			if (!build.armorName.isEmpty()) {
				pos = addLine(Messages.get(this, "armor_label"), build.armorName, pos);
			}
			if (!build.petSpecies.isEmpty()) {
				pos = addLine(Messages.get(this, "pet_label"), build.petSpecies + " Niv " + build.petLevel, pos);
			}
			if (!build.rings.isEmpty()) {
				pos = addLine(Messages.get(this, "rings_label"), String.join(", ", build.rings), pos);
			}
			if (!build.artifacts.isEmpty()) {
				pos = addLine(Messages.get(this, "artifacts_label"), String.join(", ", build.artifacts), pos);
			}
		}

		RedButton btnClose = new RedButton(Messages.get(this, "close")) {
			@Override
			public void onClick() {
				hide();
			}
		};
		btnClose.setRect(0, HEIGHT - 18, WIDTH, 16);
		add(btnClose);
	}

	private float addLine(String label, String val, float pos) {
		RenderedTextBlock txtL = PixelScene.renderTextBlock(label, 6);
		txtL.setPos(0, pos);
		txtL.hardlight(0xAAAAAA);
		add(txtL);

		RenderedTextBlock txtV = PixelScene.renderTextBlock(val, 6);
		txtV.maxWidth(WIDTH - (int)txtL.width() - 4);
		txtV.setPos(WIDTH - txtV.width(), pos);
		txtV.hardlight(0xFFFFFF);
		add(txtV);

		return pos + Math.max(txtL.height(), txtV.height()) + 2;
	}
}
