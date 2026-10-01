/*
 * Eternity Pixel Dungeon
 * Hall of Fame Victory Build Inspection Window
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Rankings;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.VictoryBuild;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemSlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.watabou.noosa.Game;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class WndHallOfFameBuild extends Window {

	private static final int WIDTH  = 130;
	private static final int HEIGHT = 168;

	public WndHallOfFameBuild(Rankings.Record rec, VictoryBuild build) {
		super();
		resize(WIDTH, HEIGHT);

		if (build == null && Dungeon.hero != null) {
			build = VictoryBuild.captureCurrentRun(rec);
			rec.victoryBuild = build;
		}

		IconTitle title = new IconTitle();
		title.icon(HeroSprite.avatar(rec.heroClass, rec.armorTier));
		String heroClassTitle = rec.heroClass != null ? rec.heroClass.title() : "";
		String subclassTitle = "";
		if (build != null && !build.heroSubclass.isEmpty()) {
			try {
				com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass sc =
						com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass.valueOf(build.heroSubclass);
				subclassTitle = sc.title();
			} catch (Exception e) {
				subclassTitle = build.heroSubclass;
			}
		}
		String titleText = !subclassTitle.isEmpty() ?
				subclassTitle + " (" + heroClassTitle + ")" : heroClassTitle;
		title.label(titleText.toUpperCase(Locale.ENGLISH));
		title.color(TITLE_COLOR);
		title.setRect(0, 0, WIDTH, 0);
		add(title);

		float pos = title.bottom() + 2;

		String runStatus;
		if (rec.win) {
			runStatus = rec.ascending ? Messages.get(this, "ascent") : Messages.get(this, "victory");
		} else {
			runStatus = Messages.get(this, "depth") + " " + (build != null ? build.depth : rec.depth);
		}
		RenderedTextBlock date = PixelScene.renderTextBlock(runStatus + " - " + (rec.date != null ? rec.date : ""), 7);
		date.hardlight(0xFFD700); // Gold
		date.setPos(0, pos);
		add(date);
		pos += date.height() + 3;

		NumberFormat num = NumberFormat.getInstance(Locale.US);
		pos = addLine(Messages.get(this, "score"), num.format(rec.score), pos);

		if (build != null && !build.seed.isEmpty()) {
			pos = addLine(Messages.get(this, "seed"), build.seed, pos);
		}

		pos += 3;

		// Interactive Item Slots Grid
		ArrayList<Item> displayItems = new ArrayList<>();
		if (build != null) {
			if (build.weaponItem != null) displayItems.add(build.weaponItem);
			if (build.armorItem != null) displayItems.add(build.armorItem);
			if (build.ringItems != null) displayItems.addAll(build.ringItems);
			if (build.artifactItems != null) displayItems.addAll(build.artifactItems);
			if (build.quickslotItems != null) displayItems.addAll(build.quickslotItems);
		}

		if (!displayItems.isEmpty()) {
			RenderedTextBlock gearHeader = PixelScene.renderTextBlock(Messages.get(this, "equipment_header"), 6);
			gearHeader.hardlight(0xDDDDDD);
			gearHeader.setPos(0, pos);
			add(gearHeader);
			pos += gearHeader.height() + 3;

			float slotX = 0;
			float slotY = pos;
			final float slotSize = 22;
			final float slotGap = 3;

			for (Item item : displayItems) {
				if (slotX + slotSize > WIDTH) {
					slotX = 0;
					slotY += slotSize + slotGap;
				}

				ItemSlot slot = new ItemSlot(item) {
					@Override
					public void onClick() {
						if (item != null) {
							Game.scene().add(new WndInfoItem(item));
						}
					}
				};
				slot.setRect(slotX, slotY, slotSize, slotSize);
				add(slot);

				slotX += slotSize + slotGap;
			}
			pos = slotY + slotSize + 4;
		} else if (build != null && (!build.weaponName.isEmpty() || !build.armorName.isEmpty() || !build.rings.isEmpty() || !build.artifacts.isEmpty())) {
			// Fallback text rendering if items weren't bundled
			if (!build.weaponName.isEmpty()) {
				pos = addLine(Messages.get(this, "weapon_label"), build.weaponName, pos);
			}
			if (!build.armorName.isEmpty()) {
				pos = addLine(Messages.get(this, "armor_label"), build.armorName, pos);
			}
			if (!build.rings.isEmpty()) {
				pos = addLine(Messages.get(this, "rings_label"), String.join(", ", build.rings), pos);
			}
			if (!build.artifacts.isEmpty()) {
				pos = addLine(Messages.get(this, "artifacts_label"), String.join(", ", build.artifacts), pos);
			}
		} else {
			RenderedTextBlock noData = PixelScene.renderTextBlock(Messages.get(this, "no_data"), 6);
			noData.hardlight(0x888888);
			noData.maxWidth(WIDTH);
			noData.setPos(0, pos);
			add(noData);
			pos += noData.height() + 4;
		}

		// Companion / Pet section
		if (build != null && !build.petSpecies.isEmpty()) {
			String petText = Messages.get(this, "pet_details", build.petSpecies, build.petLevel);
			pos = addLine(Messages.get(this, "pet_label"), petText, pos);
		}

		// Action Buttons
		float btnY = HEIGHT - 18;
		if (build != null && !build.seed.isEmpty()) {
			final String finalSeed = build.seed;
			RedButton btnCopySeed = new RedButton(Messages.get(this, "btn_copy_seed")) {
				@Override
				public void onClick() {
					SPDSettings.customSeed(finalSeed);
					text.text(Messages.get(WndHallOfFameBuild.this, "seed_copied"));
				}
			};
			btnCopySeed.setRect(0, btnY, (WIDTH - 4) / 2f, 16);
			add(btnCopySeed);

			RedButton btnClose = new RedButton(Messages.get(this, "close")) {
				@Override
				public void onClick() {
					hide();
				}
			};
			btnClose.setRect((WIDTH - 4) / 2f + 4, btnY, (WIDTH - 4) / 2f, 16);
			add(btnClose);
		} else {
			RedButton btnClose = new RedButton(Messages.get(this, "close")) {
				@Override
				public void onClick() {
					hide();
				}
			};
			btnClose.setRect(0, btnY, WIDTH, 16);
			add(btnClose);
		}
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
