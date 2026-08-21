/*
 * Eternity Pixel Dungeon
 * Pet Quick Tactical HUD Panel
 */

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.pets.Pet;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndPet;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.ui.Component;

public class PetTacticalPanel extends Component {

	public static final int PANEL_HEIGHT = 20;
	private static final int BTN_WIDTH = 34;

	private NinePatch bg;
	private ColorBlock hpBar;
	private ColorBlock hpBg;
	private RenderedTextBlock nameText;
	private StyledButton btnOrder;
	private StyledButton btnFeed;
	private StyledButton btnInfo;

	private Pet currentPet = null;

	public PetTacticalPanel() {
		super();

		bg = Chrome.get(Chrome.Type.TOAST_TR);
		addToBack(bg);

		hpBg = new ColorBlock(1, 2, 0x88000000);
		add(hpBg);

		hpBar = new ColorBlock(1, 2, 0xFF44CC44);
		add(hpBar);

		nameText = PixelScene.renderTextBlock(5);
		add(nameText);

		btnOrder = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "", 5) {
			@Override
			public void onClick() {
				Pet pet = getActivePet();
				if (pet != null) {
					if (pet.currentOrder == Pet.PetOrder.FOLLOW) {
						pet.setOrder(Pet.PetOrder.DEFEND);
					} else {
						pet.setOrder(Pet.PetOrder.FOLLOW);
					}
					updatePetData(pet);
				}
			}
		};
		add(btnOrder);

		btnFeed = new StyledButton(Chrome.Type.GREY_BUTTON_TR, Messages.get(PetTacticalPanel.class, "btn_feed"), 5) {
			@Override
			public void onClick() {
				final Pet pet = getActivePet();
				if (pet != null) {
					GameScene.selectItem(new WndBag.ItemSelector() {
						@Override
						public String textPrompt() {
							return Messages.get(WndPet.class, "feed_prompt");
						}

						@Override
						public Class<? extends Bag> preferredBag() {
							return null;
						}

						@Override
						public boolean itemSelectable(Item item) {
							return item instanceof Food || item instanceof PotionOfHealing
									|| item.name().toLowerCase().contains("meat")
									|| item.name().toLowerCase().contains("berry");
						}

						@Override
						public void onSelect(Item item) {
							if (item != null) {
								if (pet.feed(item)) {
									item.detach(Dungeon.hero.belongings.backpack);
									Dungeon.hero.spendAndNext(1f);
									updatePetData(pet);
								}
							}
						}
					});
				}
			}
		};
		add(btnFeed);

		btnInfo = new StyledButton(Chrome.Type.GREY_BUTTON_TR, Messages.get(PetTacticalPanel.class, "btn_info"), 5) {
			@Override
			public void onClick() {
				Pet pet = getActivePet();
				if (pet != null) {
					GameScene.show(new WndPet(pet));
				}
			}
		};
		add(btnInfo);

		visible = false;
	}

	private Pet getActivePet() {
		Hero hero = Dungeon.hero;
		if (hero != null && hero.pet != null && hero.pet.isAlive()) {
			return hero.pet;
		}
		if (Dungeon.level != null) {
			for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob : Dungeon.level.mobs) {
				if (mob instanceof Pet && mob.isAlive()) {
					if (hero != null && hero.pet == null) {
						hero.pet = (Pet) mob;
					}
					return (Pet) mob;
				}
			}
		}
		return null;
	}

	@Override
	public void update() {
		super.update();

		if (!com.shatteredpixel.shatteredpixeldungeon.SPDSettings.showPetPanel()) {
			if (visible) {
				visible = false;
				currentPet = null;
			}
			return;
		}

		Pet pet = getActivePet();
		if (pet != null) {
			if (!visible || currentPet != pet) {
				visible = true;
				currentPet = pet;
			}
			updatePetData(pet);
		} else {
			if (visible) {
				visible = false;
				currentPet = null;
			}
		}
	}

	public void updatePetData(Pet pet) {
		if (pet == null) return;

		nameText.text(pet.name() + " L" + pet.petLevel);

		String orderStr = (pet.currentOrder == Pet.PetOrder.FOLLOW) ?
				Messages.get(PetTacticalPanel.class, "order_follow") :
				Messages.get(PetTacticalPanel.class, "order_defend");
		btnOrder.text.text(orderStr);

		float leftRegionW = Math.max(20, width - (BTN_WIDTH * 3 + 10));
		float hpRatio = Math.max(0f, Math.min(1f, (float) pet.HP / (float) pet.HT));
		hpBar.size(Math.max(1, leftRegionW * hpRatio), 2);
		hpBg.size(leftRegionW, 2);

		layout();
	}

	@Override
	protected void layout() {
		super.layout();

		bg.x = x;
		bg.y = y;
		bg.size(width, height);

		float padding = 3;
		float currentX = x + padding;
		float leftRegionW = Math.max(20, width - (BTN_WIDTH * 3 + 10));

		nameText.maxWidth((int) leftRegionW);
		nameText.setPos(currentX, y + 2);

		hpBg.x = currentX;
		hpBg.y = y + height - 5;

		hpBar.x = currentX;
		hpBar.y = y + height - 5;

		float btnY = y + 2;
		float btnH = height - 4;

		float infoX = x + width - BTN_WIDTH - padding;
		float feedX = infoX - BTN_WIDTH - 2;
		float orderX = feedX - BTN_WIDTH - 2;

		btnInfo.setRect(infoX, btnY, BTN_WIDTH, btnH);
		btnFeed.setRect(feedX, btnY, BTN_WIDTH, btnH);
		btnOrder.setRect(orderX, btnY, BTN_WIDTH, btnH);
	}
}
