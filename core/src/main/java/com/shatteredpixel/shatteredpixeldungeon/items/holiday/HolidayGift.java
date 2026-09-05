/*
 * Eternity Pixel Dungeon
 * Holiday Gift Item (Winter Event Special Consumable)
 */

package com.shatteredpixel.shatteredpixeldungeon.items.holiday;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.CandyCane;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.WinterJoyPotion;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class HolidayGift extends Item {

	private static final String AC_OPEN = "OPEN";

	{
		image = ItemSpriteSheet.CHEST;
		stackable = true;
		defaultAction = AC_OPEN;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_OPEN);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (action.equals(AC_OPEN)) {
			detach(hero.belongings.backpack);
			hero.spend(TIME_TO_PICK_UP);

			CellEmitter.get(hero.pos).burst(Speck.factory(Speck.STAR), 10);
			GLog.p(Messages.get(this, "opened"));

			// Gift contents: Gold, CandyCane, WinterJoyPotion or random Potion/Scroll
			int rewardType = Random.Int(4);
			Item reward = null;
			switch (rewardType) {
				case 0:
					reward = new CandyCane();
					break;
				case 1:
					reward = new WinterJoyPotion();
					break;
				case 2:
					reward = Generator.random(Generator.Category.POTION);
					break;
				default:
					reward = new Gold(Random.IntRange(40, 120));
					break;
			}

			if (reward != null) {
				reward.identify();
				if (!reward.collect(hero.belongings.backpack)) {
					Dungeon.level.drop(reward, hero.pos);
				}
			}
		}
	}
}
