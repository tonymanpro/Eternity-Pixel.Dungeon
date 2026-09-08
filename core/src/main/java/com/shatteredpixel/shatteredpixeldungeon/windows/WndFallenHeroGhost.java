/*
 * Eternity Pixel Dungeon
 * Nemesis System - Interactive Fallen Hero Dialogue Window
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.FallenHeroRecord;
import com.shatteredpixel.shatteredpixeldungeon.NemesisConfig;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barkskin;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.FallenHeroMob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.GhostHeroNPC;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

import java.util.Locale;

public class WndFallenHeroGhost extends Window {

	private static final int WIDTH = 135;

	public WndFallenHeroGhost(final GhostHeroNPC npc, final FallenHeroRecord record) {
		super();

		final NemesisConfig cfg = NemesisConfig.get();

		HeroClass heroClassEnum = HeroClass.WARRIOR;
		try {
			heroClassEnum = HeroClass.valueOf(record.heroClass.toUpperCase(Locale.ENGLISH));
		} catch (Exception ignored) {}

		IconTitle title = new IconTitle();
		title.icon(HeroSprite.avatar(heroClassEnum, 1));
		title.label(Messages.get(WndFallenHeroGhost.class, "title", record.heroClass.toUpperCase(Locale.ENGLISH)));
		title.color(TITLE_COLOR);
		title.setRect(0, 0, WIDTH, 0);
		add(title);

		float pos = title.bottom() + 4;

		String story = Messages.get(WndFallenHeroGhost.class, "story",
				record.heroClass,
				record.level,
				record.depth,
				record.causeOfDeath);

		RenderedTextBlock txtStory = PixelScene.renderTextBlock(story, 6);
		txtStory.maxWidth(WIDTH);
		txtStory.setPos(0, pos);
		add(txtStory);

		pos = txtStory.bottom() + 8;

		StyledButton btnPray = new StyledButton(Chrome.Type.GREY_BUTTON_TR, Messages.get(WndFallenHeroGhost.class, "pray")) {
			@Override
			public void onClick() {
				hide();
				if (Dungeon.hero != null && Dungeon.hero.isAlive()) {
					Dungeon.hero.HP = Math.min(Dungeon.hero.HT, Dungeon.hero.HP + (int)(Dungeon.hero.HT * cfg.healRatio));
					Dungeon.hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), 6);
					Buff.prolong(Dungeon.hero, Bless.class, Bless.DURATION * 2);
					Buff.affect(Dungeon.hero, Barkskin.class).set(5 + Dungeon.hero.lvl, 30);
					new Flare(6, 32).color(0xFFFF00, true).show(Dungeon.hero.sprite, 2f);
					Sample.INSTANCE.play(Assets.Sounds.MELD);
					GLog.p(Messages.get(WndFallenHeroGhost.class, "pray_done"));
					CellEmitter.get(npc.pos).burst(Speck.factory(Speck.DISCOVER), 8);
				}
				if (record != null) {
					record.released = true;
					FallenHeroRecord.save(record);
				}
				npc.destroy();
				npc.sprite.die();
			}
		};
		btnPray.setRect(0, pos, WIDTH, 16);
		add(btnPray);

		pos = btnPray.bottom() + 4;

		RedButton btnDuel = new RedButton(Messages.get(WndFallenHeroGhost.class, "duel")) {
			@Override
			public void onClick() {
				hide();
				GLog.w(Messages.get(WndFallenHeroGhost.class, "duel_start"));
				
				int npcPos = npc.pos;
				npc.destroy();
				npc.sprite.die();

				FallenHeroMob mob = FallenHeroMob.spawnAt(record, npcPos);
				if (mob != null && Dungeon.level != null) {
					com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.add(mob);
					mob.notice();
				}
			}
		};
		btnDuel.setRect(0, pos, WIDTH, 16);
		add(btnDuel);

		resize(WIDTH, (int)(btnDuel.bottom() + 2));
	}
}
