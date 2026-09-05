/*
 * Eternity Pixel Dungeon
 * Ghost Hero NPC (Spectral Fallen Hero Encounter)
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.FallenHeroRecord;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GhostSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndFallenHeroGhost;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

public class GhostHeroNPC extends NPC {

	public FallenHeroRecord record;

	{
		spriteClass = GhostSprite.class;
		flying = true;
		state = PASSIVE;
		properties.add(Property.UNDEAD);
	}

	public GhostHeroNPC() {
		super();
	}

	public GhostHeroNPC(FallenHeroRecord record) {
		this();
		this.record = record;
	}

	@Override
	public boolean interact(Char chr) {
		sprite.turnTo(pos, chr.pos);
		if (record == null) {
			record = FallenHeroRecord.activeRecord != null ? FallenHeroRecord.activeRecord : FallenHeroRecord.load();
		}
		if (record != null) {
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					GameScene.show(new WndFallenHeroGhost(GhostHeroNPC.this, record));
				}
			});
		}
		return true;
	}

	private static final String RECORD = "record";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		if (record != null) {
			Bundle recBundle = new Bundle();
			recBundle.put("heroClass", record.heroClass);
			recBundle.put("heroSubclass", record.heroSubclass);
			recBundle.put("level", record.level);
			recBundle.put("depth", record.depth);
			recBundle.put("causeOfDeath", record.causeOfDeath);
			recBundle.put("weaponName", record.weaponName);
			recBundle.put("armorName", record.armorName);
			recBundle.put("seed", record.seed);
			recBundle.put("date", record.date);
			bundle.put(RECORD, recBundle);
		}
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(RECORD)) {
			Bundle recBundle = bundle.getBundle(RECORD);
			record = new FallenHeroRecord();
			record.heroClass = recBundle.getString("heroClass");
			record.heroSubclass = recBundle.getString("heroSubclass");
			record.level = recBundle.getInt("level");
			record.depth = recBundle.getInt("depth");
			record.causeOfDeath = recBundle.getString("causeOfDeath");
			record.weaponName = recBundle.getString("weaponName");
			record.armorName = recBundle.getString("armorName");
			record.seed = recBundle.getString("seed");
			record.date = recBundle.getString("date");
		}
	}
}
