package com.mrwhitedevil.pvp.core;

import com.mrwhitedevil.pvp.api.ClientAccess;
import com.mrwhitedevil.pvp.config.PvpSettings;
import com.mrwhitedevil.pvp.features.*;

/** Pure-Java coordinator: wires settings, features, the swap controller and return scheduling. */
public final class PvpController {
    public final PvpSettings settings;
    public final SwapController swaps = new SwapController();
    public final SmartWeaponDetection detection;
    public final AutoLungeSwap lunge;
    public final AlwaysShield shield;
    public final AutoStunSlam stunSlam;
    public final WindChargeSwap windCharge;
    public final AutomaticReturnToWeapon autoReturn;
    public final EmergencyWeaponReturn emergency;
    public final CustomSwapDelay delay;
    public final ActionNotifications notifications;
    private final ClientAccess client;

    private long returnDueTick = -1;        // -1 = nothing scheduled
    private boolean awaitingUse = false;    // wind charge selected, waiting for the player to use it

    public PvpController(ClientAccess client, PvpSettings settings) {
        this.client = client;
        this.settings = settings;
        detection = new SmartWeaponDetection(client);
        lunge = new AutoLungeSwap(client, detection, swaps);
        shield = new AlwaysShield(client, detection, swaps);
        stunSlam = new AutoStunSlam(client, detection, swaps);
        windCharge = new WindChargeSwap(client, detection, swaps);
        autoReturn = new AutomaticReturnToWeapon(client, swaps);
        emergency = new EmergencyWeaponReturn(client, swaps);
        delay = new CustomSwapDelay(swaps);
        notifications = new ActionNotifications(client);
        applySettings();
    }

    /** Push the plain settings model into the live features. Call after any settings change. */
    public void applySettings() {
        delay.setDelayTicks(settings.swapDelayTicks);
        lunge.setEnabled(settings.autoLungeSwap);
        shield.setEnabled(settings.alwaysShield);
        stunSlam.setEnabled(settings.autoStunSlam);
        windCharge.setEnabled(settings.windChargeSwap);
        autoReturn.setEnabled(settings.automaticReturn);
        emergency.setEnabled(settings.emergencyWeaponReturn);
        notifications.setEnabled(settings.actionNotifications);
    }

    /** Hook: player's own attack input is about to hit a target. Returns true if a swap happened. */
    public boolean onAttackAttempt(long tick) {
        if (!stunSlam.onAttackAttempt(tick)) return false;
        notifications.show("Auto Stun Slam: swapped to Mace");
        scheduleReturn(tick);
        return true;
    }

    /** Hook: confirmed lunge window (adapter-detected). */
    public boolean onLungeWindow(long tick) {
        if (!lunge.onLungeWindow(tick)) return false;
        notifications.show("Auto Lunge Swap: swapped to Spear");
        scheduleReturn(tick);
        return true;
    }

    /** Hook: Wind Charge Swap keybind. Return is deferred until the player uses the charge. */
    public boolean onWindChargeKey(long tick) {
        if (!windCharge.selectWindCharge(tick)) return false;
        notifications.show("Wind Charge Swap: selected Wind Charge");
        awaitingUse = true;
        return true;
    }

    /** Hook: Always Shield keybind. No auto-return: the shield stays until the player returns. */
    public boolean onShieldKey(long tick) {
        boolean ok = shield.selectShield(tick);
        if (ok) notifications.show("Always Shield: selected Shield");
        return ok;
    }

    /** Hook: player finished using an item (right-click). Completes the Wind Charge flow. */
    public void onItemUsed(long tick) {
        if (awaitingUse) { awaitingUse = false; scheduleReturn(tick); }
    }

    /** Hook: Emergency Weapon Return keybind. Ignores delay, cancels pending state. */
    public boolean onEmergencyKey() {
        returnDueTick = -1; awaitingUse = false;
        return emergency.trigger();
    }

    private void scheduleReturn(long tick) {
        if (autoReturn.enabled()) returnDueTick = tick + swaps.delayTicks();
    }

    public void tick(long tick) {
        for (Feature f : new Feature[]{lunge, shield, stunSlam, windCharge, autoReturn, emergency, delay, notifications}) f.tick();
        if (returnDueTick >= 0 && tick >= returnDueTick) {
            if (swaps.originalSlot().isEmpty()) { returnDueTick = -1; return; }
            if (autoReturn.afterAction(tick)) {
                notifications.show("Returned to previous weapon");
                returnDueTick = -1;
            } // else: delay not yet elapsed, retry next tick
        }
    }

    public boolean returnPending() { return returnDueTick >= 0; }
}
