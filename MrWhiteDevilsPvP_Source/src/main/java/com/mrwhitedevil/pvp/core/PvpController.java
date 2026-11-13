package com.mrwhitedevil.pvp.core;

import com.mrwhitedevil.pvp.api.ClientAccess;
import com.mrwhitedevil.pvp.config.PvpSettings;
import com.mrwhitedevil.pvp.features.*;

/** Coordinates feature logic, hotbar swaps and delayed returns. */
public final class PvpController {
    public final PvpSettings settings;
    public final SwapController swaps = new SwapController();
    public final SmartWeaponDetection detection;
    public final AutoLungeSwap lunge;
    public final AutoStunSlam stunSlam;
    public final WindChargeSwap windCharge;
    public final AutomaticReturnToWeapon autoReturn;
    public final EmergencyWeaponReturn emergency;
    public final CustomSwapDelay delay;
    public final ActionNotifications notifications;
    private final ClientAccess client;

    private long returnDueTick = -1;
    private boolean awaitingUse = false;

    public PvpController(ClientAccess client, PvpSettings settings) {
        this.client = client;
        this.settings = settings;
        detection = new SmartWeaponDetection(client);
        lunge = new AutoLungeSwap(client, detection, swaps);
        stunSlam = new AutoStunSlam(client, detection, swaps);
        windCharge = new WindChargeSwap(client, detection, swaps);
        autoReturn = new AutomaticReturnToWeapon(client, swaps);
        emergency = new EmergencyWeaponReturn(client, swaps);
        delay = new CustomSwapDelay(swaps);
        notifications = new ActionNotifications(client);
        applySettings();
    }

    public void applySettings() {
        delay.setDelayTicks(settings.swapDelayTicks);
        lunge.setEnabled(settings.autoLungeSwap);
        stunSlam.setEnabled(settings.autoStunSlam);
        windCharge.setEnabled(settings.windChargeSwap);
        autoReturn.setEnabled(settings.automaticReturn);
        emergency.setEnabled(settings.emergencyWeaponReturn);
        notifications.setEnabled(settings.actionNotifications);
    }

    /** Swaps to the mace for a qualifying airborne attack against a blocking target. */
    public boolean onAttackAttempt(long tick) {
        if (!stunSlam.onAttackAttempt(tick)) return false;
        notifications.show("Auto Stun Slam: swapped to Mace");
        scheduleReturn(tick);
        return true;
    }

    /** Called by a real adapter trigger when a Lunge action window is confirmed. */
    public boolean onLungeWindow(long tick) {
        if (!lunge.onLungeWindow(tick)) return false;
        notifications.show("Auto Lunge Swap: swapped to Spear");
        scheduleReturn(tick);
        return true;
    }

    public boolean onWindChargeKey(long tick) {
        if (!windCharge.selectWindCharge(tick)) return false;
        notifications.show("Wind Charge Swap: selected Wind Charge");
        awaitingUse = true;
        return true;
    }

    public void onItemUsed(long tick) {
        if (awaitingUse) { awaitingUse = false; scheduleReturn(tick); }
    }

    public boolean onEmergencyKey() {
        returnDueTick = -1;
        awaitingUse = false;
        return emergency.trigger();
    }

    private void scheduleReturn(long tick) {
        if (autoReturn.enabled()) returnDueTick = tick + swaps.delayTicks();
    }

    public void tick(long tick) {
        for (Feature f : new Feature[]{lunge, stunSlam, windCharge, autoReturn, emergency, delay, notifications}) f.tick();
        if (returnDueTick >= 0 && tick >= returnDueTick) {
            if (swaps.originalSlot().isEmpty()) { returnDueTick = -1; return; }
            if (autoReturn.afterAction(tick)) {
                notifications.show("Returned to previous weapon");
                returnDueTick = -1;
            }
        }
    }

    public boolean returnPending() { return returnDueTick >= 0; }
}
