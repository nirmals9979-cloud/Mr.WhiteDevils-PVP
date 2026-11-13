package com.mrwhitedevil.pvp.core;

import com.mrwhitedevil.pvp.api.ClientAccess;
import com.mrwhitedevil.pvp.config.PvpSettings;
import com.mrwhitedevil.pvp.features.*;

/** Coordinates hotbar swaps and client-side feature actions. */
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
    private long stunAttackDueTick = -1;
    private boolean stunTargetLatched = false;
    private boolean useWasDown = false;
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

    public boolean onAttackAttempt(long tick) {
        if (!stunSlam.onAttackAttempt(tick)) return false;
        notifications.show("Auto Stun Slam: mace selected");
        stunAttackDueTick = tick + 1;
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
        if (awaitingUse) {
            awaitingUse = false;
            returnDueTick = -1;
            if (autoReturn.enabled() && swaps.emergencyReturn(client)) notifications.show("Returned to previous weapon");
        }
    }

    public boolean onLungeWindow(long tick) {
        if (!lunge.onLungeWindow(tick)) return false;
        notifications.show("Auto Lunge Swap: spear selected");
        scheduleReturn(tick);
        return true;
    }

    /** Detects a right-click edge while a sword/axe is held, then uses the hotbar wind charge. */
    private void handleWindChargeInput(long tick) {
        boolean down = client.useKeyDown();
        if (down && !useWasDown && windCharge.enabled() && client.inGame()
                && client.selectedItemIsSwordOrAxe()) {
            if (windCharge.selectWindCharge(tick)) {
                notifications.show("Wind Charge Swap: ON");
                client.useMainHand();
                scheduleReturn(tick);
            }
        }
        useWasDown = down;
    }

    public boolean onEmergencyKey() {
        returnDueTick = -1;
        stunAttackDueTick = -1;
        stunTargetLatched = false;
        awaitingUse = false;
        return emergency.trigger();
    }

    private void scheduleReturn(long tick) {
        if (autoReturn.enabled()) returnDueTick = tick + Math.max(1, swaps.delayTicks());
    }

    public void tick(long tick) {
        for (Feature f : new Feature[]{lunge, stunSlam, windCharge, autoReturn, emergency, delay, notifications}) f.tick();

        handleWindChargeInput(tick);

        if (!client.targetIsLivingEntity() || !client.targetIsInReach()) stunTargetLatched = false;
        if (stunSlam.enabled() && !stunTargetLatched && client.inGame()
                && !client.playerOnGround() && client.targetIsLivingEntity() && client.targetIsInReach()) {
            if (onAttackAttempt(tick)) stunTargetLatched = true;
        }

        if (stunAttackDueTick >= 0 && tick >= stunAttackDueTick) {
            client.attackTarget();
            stunAttackDueTick = -1;
        }

        if (returnDueTick >= 0 && tick >= returnDueTick) {
            if (swaps.originalSlot().isEmpty()) { returnDueTick = -1; return; }
            if (autoReturn.enabled() && swaps.emergencyReturn(client)) {
                notifications.show("Returned to previous weapon");
                returnDueTick = -1;
            }
        }
    }

    public boolean returnPending() { return returnDueTick >= 0; }
}
