package com.mrwhitedevil.pvp;

import com.mrwhitedevil.pvp.api.*;
import com.mrwhitedevil.pvp.config.*;
import com.mrwhitedevil.pvp.core.PvpController;
import java.nio.file.*;
import java.util.*;

/** Dependency-free tests against a fake ClientAccess. Run: java -cp out com.mrwhitedevil.pvp.CoreTests */
public final class CoreTests {
    static int fails = 0, passes = 0;
    static void check(boolean c, String n) { if (c) passes++; else { fails++; System.out.println("FAIL: " + n); } }

    static final class Fake implements ClientAccess {
        boolean ground = false, living = true, shield = true, reach = true;
        int sel = 0; List<String> msgs = new ArrayList<>();
        String[] items = {"minecraft:diamond_sword","minecraft:mace","minecraft:wind_charge",
            "minecraft:shield","minecraft:iron_spear","","","",""};
        public boolean inGame() { return true; }
        public boolean playerOnGround() { return ground; }
        public boolean playerAttacking() { return false; }
        public boolean targetIsLivingEntity() { return living; }
        public boolean targetHasShieldRaised() { return shield; }
        public boolean targetIsInReach() { return reach; }
        public int selectedHotbarSlot() { return sel; }
        public void selectHotbarSlot(int s) { sel = s; }
        public List<HotbarStack> hotbar() {
            List<HotbarStack> l = new ArrayList<>();
            for (int i = 0; i < 9; i++) l.add(new HotbarStack(i, items[i], items[i].isEmpty()?0:1, items[i].isEmpty()));
            return l;
        }
        public void useMainHand() {}
        public void notifyAction(String m) { msgs.add(m); }
    }

    static PvpController make(Fake f, PvpSettings s) { return new PvpController(f, s); }

    public static void main(String[] a) throws Exception {
        // Stun slam: airborne vs raised shield -> mace, then auto-return after delay
        Fake f = new Fake(); PvpSettings s = new PvpSettings(); s.autoStunSlam = true; s.swapDelayTicks = 3;
        PvpController c = make(f, s);
        check(c.onAttackAttempt(100), "stun slam swaps");
        check(f.sel == 1, "selected mace");
        c.tick(101); c.tick(102); check(f.sel == 1, "no early return");
        c.tick(103); check(f.sel == 0, "returned to original after delay");
        check(!c.returnPending(), "return cleared");

        // Conditions: on ground / no shield / out of reach / not living => no swap
        for (int i = 0; i < 4; i++) {
            f = new Fake(); s = new PvpSettings(); s.autoStunSlam = true; c = make(f, s);
            if (i==0) f.ground = true; if (i==1) f.shield = false; if (i==2) f.reach = false; if (i==3) f.living = false;
            check(!c.onAttackAttempt(0) && f.sel == 0, "stun slam gated case " + i);
        }
        // Disabled feature does nothing
        f = new Fake(); c = make(f, new PvpSettings()); check(!c.onAttackAttempt(0), "disabled stun slam");

        // Wind charge: return only after item use
        f = new Fake(); s = new PvpSettings(); s.windChargeSwap = true; s.swapDelayTicks = 1; c = make(f, s);
        check(c.onWindChargeKey(10) && f.sel == 2, "wind charge selected");
        c.tick(50); check(f.sel == 2, "stays until used");
        c.onItemUsed(60); c.tick(61); check(f.sel == 0, "returns after use");

        // Auto-return disabled => stays
        f = new Fake(); s = new PvpSettings(); s.windChargeSwap = true; s.automaticReturn = false; c = make(f, s);
        c.onWindChargeKey(0); c.onItemUsed(1); c.tick(10); check(f.sel == 2, "no return when disabled");

        // Emergency return ignores delay and cancels schedule
        f = new Fake(); s = new PvpSettings(); s.autoStunSlam = true; s.emergencyWeaponReturn = true; s.swapDelayTicks = 40; c = make(f, s);
        c.onAttackAttempt(0); check(f.sel == 1, "slam swap");
        check(c.onEmergencyKey() && f.sel == 0 && !c.returnPending(), "emergency return");
        check(!c.onEmergencyKey(), "emergency with nothing to return");

        // Swap delay blocks rapid second swap while preserving original slot
        f = new Fake(); s = new PvpSettings(); s.windChargeSwap = true; s.emergencyWeaponReturn = true; s.swapDelayTicks = 5; c = make(f, s);
        check(c.onWindChargeKey(0) && f.sel == 2, "wind charge first swap");
        check(!c.onWindChargeKey(2), "blocked by delay");
        c.onItemUsed(4);
        c.onEmergencyKey(); check(f.sel == 0, "original slot preserved across chained swaps");

        // Lunge
        f = new Fake(); s = new PvpSettings(); s.autoLungeSwap = true; c = make(f, s);
        check(c.onLungeWindow(0) && f.sel == 4, "lunge -> spear");

        // Missing items
        f = new Fake(); f.items[1] = ""; s = new PvpSettings(); s.autoStunSlam = true; c = make(f, s);
        check(!c.onAttackAttempt(0), "no mace in hotbar");

        // Notifications toggle
        f = new Fake(); s = new PvpSettings(); s.autoStunSlam = true; s.actionNotifications = false; c = make(f, s);
        c.onAttackAttempt(0); check(f.msgs.isEmpty(), "notifications off");

        // Delay validation + config round trip
        try { new SettingsMenuModel(new PvpSettings()).setSwapDelayTicks(41); check(false, "delay range"); }
        catch (IllegalArgumentException e) { check(true, "delay range"); }
        Path p = Files.createTempFile("pvp", ".properties");
        PvpSettings x = new PvpSettings(); x.swapDelayTicks = 7; x.autoStunSlam = true; x.automaticReturn = false;
        ConfigStore.save(p, x); PvpSettings y = ConfigStore.load(p);
        check(y.swapDelayTicks == 7 && y.autoStunSlam && !y.automaticReturn, "config round trip");

        // Classifier
        check(ItemClassifier.matches("minecraft:netherite_sword", ItemClassifier.Kind.SWORD), "sword id");
        check(ItemClassifier.matches("minecraft:mace", ItemClassifier.Kind.MACE), "mace id");


        // Smart Weapon Detection: real 26.3 item ids (spear tiers, mace, shield, wind charge, copper/other tools)
        for (String sp : new String[]{"wooden_spear","copper_spear","iron_spear","golden_spear","diamond_spear","netherite_spear"})
            check(ItemClassifier.matches("minecraft:" + sp, ItemClassifier.Kind.SPEAR), "spear id " + sp);
        for (String sw : new String[]{"wooden_sword","stone_sword","copper_sword","iron_sword","golden_sword","diamond_sword","netherite_sword"})
            check(ItemClassifier.matches("minecraft:" + sw, ItemClassifier.Kind.SWORD), "sword id " + sw);
        check(ItemClassifier.matches("minecraft:netherite_axe", ItemClassifier.Kind.AXE), "axe id");
        check(ItemClassifier.matches("minecraft:shield", ItemClassifier.Kind.SHIELD), "shield id");
        check(ItemClassifier.matches("minecraft:wind_charge", ItemClassifier.Kind.WIND_CHARGE), "wind charge id");
        check(!ItemClassifier.matches("minecraft:diamond_pickaxe", ItemClassifier.Kind.ANY_WEAPON), "pickaxe is not a weapon");
        check(!ItemClassifier.matches("minecraft:trident", ItemClassifier.Kind.MACE), "trident is not mace");
        check(!ItemClassifier.matches("minecraft:shield", ItemClassifier.Kind.SWORD), "shield is not sword");

        System.out.println(passes + " passed, " + fails + " failed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
