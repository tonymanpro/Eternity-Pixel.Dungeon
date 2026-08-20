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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;

public class PrimalFury extends Buff {

	public static final float MAX_FURY = 100f;

	private float fury = 0f;
	private int combatTurns = 0;

	@Override
	public boolean act() {
		// Drenamos furia pasivamente fuera de combate
		if (combatTurns <= 0) {
			fury -= 2f;
			if (fury < 0) fury = 0;
			if (fury == 0) detach();
		} else {
			combatTurns--;
		}

		spend( TICK );
		return true;
	}
	
	public void hit(boolean ranged) {
		// El barbaro acumula mas furia cuerpo a cuerpo
		combatTurns = 5; // Mantener la furia por 5 turnos despues del ultimo ataque
		addFury(ranged ? 5f : 10f);
	}

	public void addFury(float amount) {
		fury += amount;
		if (fury > MAX_FURY) {
			fury = MAX_FURY;
		}
	}

	public float getFuryRaw() { return fury; }
	public float getFuryPercent() { return fury / MAX_FURY; }

	public float damageMultiplier() {
		// De 0% a +40% de daño basado en la furia acumulada
		return 1f + (getFuryPercent() * 0.40f);
	}
	
	public float reductionMultiplier() {
		// Reduces hasta un 25% de daño basado en la furia 
		return 1f - (getFuryPercent() * 0.25f);
	}

	@Override
	public int icon() {
		return BuffIndicator.FURY;
	}
	
	@Override
	public float iconFadePercent() {
		return 1f - getFuryPercent();
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString(Math.round(getFuryRaw()));
	}

	@Override
	public void tintIcon(Image icon) {
		// Rojo furia persistente
		icon.hardlight(1f, 0.4f, 0.2f);
	}

	@Override
	public String toString() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", (int)fury);
	}

	private static final String FURY = "fury";
	private static final String COMBAT = "combat";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( FURY, fury );
		bundle.put( COMBAT, combatTurns );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		fury = bundle.getFloat( FURY );
		combatTurns = bundle.getInt( COMBAT );
	}
}
