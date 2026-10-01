package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindofMisc;
import com.shatteredpixel.shatteredpixeldungeon.items.TicketToWaveArena;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.EtherealChains;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.EquipmentBag;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Barricade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.food.SmallRation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WornShortsword;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Collections;

public class ArenaInventory {

    public static Bundle stashedBelongings = null;
    public static Bundle savedQuickslot = null;
    public static boolean active = false;

    public static boolean isActive(){
        return active;
    }

    public static int depth = 1;
    public static int branch = 0;
    public static int pos = 0;

    private static final String ACTIVE             = "active";
    private static final String DEPTH              = "depth";
    private static final String BRANCH             = "branch";
    private static final String POS                = "pos";
    private static final String STASHED_BELONGINGS = "stashed_belongings";
    private static final String SAVED_QUICKSLOT    = "saved_quickslot";

    public static void storeInBundle( Bundle bundle ){
        bundle.put( ACTIVE, active );
        bundle.put( DEPTH, depth );
        bundle.put( BRANCH, branch );
        bundle.put( POS, pos );
        if (stashedBelongings != null) {
            bundle.put( STASHED_BELONGINGS, stashedBelongings );
        }
        if (savedQuickslot != null) {
            bundle.put( SAVED_QUICKSLOT, savedQuickslot );
        }
    }

    public static void restoreFromBundle( Bundle bundle ){
        if (bundle == null) return;
        active = bundle.getBoolean( ACTIVE );
        depth = bundle.getInt( DEPTH );
        branch = bundle.getInt( BRANCH );
        pos = bundle.getInt( POS );
        if (bundle.contains( STASHED_BELONGINGS )) {
            stashedBelongings = bundle.getBundle( STASHED_BELONGINGS );
        } else {
            stashedBelongings = null;
        }
        if (bundle.contains( SAVED_QUICKSLOT )) {
            savedQuickslot = bundle.getBundle( SAVED_QUICKSLOT );
        } else {
            savedQuickslot = null;
        }
    }

    public static void stashAndStart( Hero hero ){
        if (hero == null) return;

        if (active) {
            stashedBelongings = null;
            savedQuickslot = null;
            active = false;
        }

        Bundle b = new Bundle();
        hero.belongings.storeInBundle( b );

        Bundle bq = new Bundle();
        Dungeon.quickslot.storePlaceholders( bq );
        stashedBelongings = b;
        savedQuickslot = bq;
        active = true;

        forceUnequipAll( hero );

        hero.belongings.backpack.clear();
        hero.belongings.weapon = null;
        hero.belongings.armor = null;
        hero.belongings.secondWep = null;
        hero.belongings.rings = new ArrayList<>();
        hero.belongings.artifacts = new ArrayList<>();
        hero.belongings.miscs = new ArrayList<>();

        Dungeon.quickslot.reset();
        QuickSlotButton.reset();
        giveStarterKit( hero );

        GLog.w( Messages.get( ArenaInventory.class, "stashed" ) );
    }

    private static void giveStarterKit( Hero hero ){
        new Dagger().identify().collect();
        new ClothArmor().identify().collect();
        new Barricade().quantity(2).collect();
        new SmallRation().collect();
        new TicketToWaveArena().collect();

        // separate chains lol, I every ethereal chains to be removed from inventory after going back to normal depth.
        EtherealChains.WaveEternalChains chains = new EtherealChains.WaveEternalChains();
        chains.collect();
        chains.identify();
        Dungeon.quickslot.setSlot(0, chains);
    }

    public static void restoreAndMerge( Hero hero ){
        if (hero == null) return;
        if (!active || stashedBelongings == null) return;

        // Gather all earned items from arena before un-equipping and restoring
        ArrayList<Item> earned = new ArrayList<>( hero.belongings.backpack.items );
        if (hero.belongings.weapon != null)    earned.add( hero.belongings.weapon );
        if (hero.belongings.armor != null)     earned.add( hero.belongings.armor );
        if (hero.belongings.secondWep != null) earned.add( hero.belongings.secondWep );
        earned.addAll( hero.belongings.rings );
        earned.addAll( hero.belongings.artifacts );
        earned.addAll( hero.belongings.miscs );

        // Remove temporary arena items from earned list
        earned.removeIf(item -> item == null || item instanceof EtherealChains.WaveEternalChains || item instanceof TicketToWaveArena);

        forceUnequipAll( hero );

        hero.belongings.backpack.clear();
        hero.belongings.weapon = null;
        hero.belongings.armor = null;
        hero.belongings.secondWep = null;
        hero.belongings.rings = new ArrayList<>();
        hero.belongings.artifacts = new ArrayList<>();
        hero.belongings.miscs = new ArrayList<>();

        // Restore original belongings
        hero.belongings.restoreFromBundle( stashedBelongings );

        Dungeon.quickslot.reset();
        if (savedQuickslot != null) {
            Dungeon.quickslot.restorePlaceholders( savedQuickslot );
        }
        QuickSlotButton.reset();

        for (Item item : earned) {
            if (item == null) continue;
            if (!item.collect( hero.belongings.backpack )) {
                if (Dungeon.level != null) {
                    Dungeon.level.drop( item, hero.pos ).sprite.drop();
                }
            }
        }

        stashedBelongings = null;
        savedQuickslot = null;
        active = false;

        GLog.p( Messages.get( ArenaInventory.class, "restored" ) );
    }

    private static void forceUnequipAll( Hero hero ){
        if (hero == null || hero.belongings == null) return;

        boolean addedImmunity = hero.buff( MagicImmune.class ) == null;
        MagicImmune immune = addedImmunity ? Buff.affect( hero, MagicImmune.class, 1f ) : null;

        if (hero.belongings.weapon != null)    hero.belongings.weapon.doUnequip( hero, false, false );
        if (hero.belongings.armor != null)     hero.belongings.armor.doUnequip( hero, false, false );
        if (hero.belongings.secondWep != null) hero.belongings.secondWep.doUnequip( hero, false, false );

        for (Ring ring : new ArrayList<>( hero.belongings.rings )) {
            if (ring != null) ring.doUnequip( hero, false, false );
        }
        for (Artifact art : new ArrayList<>( hero.belongings.artifacts )) {
            if (art != null) art.doUnequip( hero, false, false );
        }
        for (KindofMisc misc : new ArrayList<>( hero.belongings.miscs )) {
            if (misc != null) misc.doUnequip( hero, false, false );
        }

        if (addedImmunity && immune != null) immune.detach();
    }

    public static void reset(){
        stashedBelongings = null;
        savedQuickslot = null;
        active = false;
        depth = 1;
        branch = 0;
        pos = 0;
    }
}