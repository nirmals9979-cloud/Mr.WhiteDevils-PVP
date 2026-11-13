package com.mrwhitedevil.pvp.features;

import com.mrwhitedevil.pvp.api.ClientAccess;
import com.mrwhitedevil.pvp.api.ItemClassifier;
import com.mrwhitedevil.pvp.core.FeatureToggle;
import com.mrwhitedevil.pvp.core.SwapController;
import java.util.OptionalInt;

/** Selects a mace for an airborne crosshair target. The server controls actual hit/stun results. */
public final class AutoStunSlam extends FeatureToggle {
    private final ClientAccess client;
    private final SmartWeaponDetection detection;
    private final SwapController swaps;

    public AutoStunSlam(ClientAccess client, SmartWeaponDetection detection, SwapController swaps) {
        super("auto_stun_slam", "Auto Stun Slam", false);
        this.client = client; this.detection = detection; this.swaps = swaps;
    }

    public boolean onAttackAttempt(long tick) {
        if (!enabled() || !client.inGame() || client.playerOnGround()
                || !client.targetIsLivingEntity() || !client.targetIsInReach()) return false;
        OptionalInt mace = detection.find(ItemClassifier.Kind.MACE);
        return mace.isPresent() && swaps.swapTo(client, mace.getAsInt(), tick);
    }

    public boolean finishSequence(long tick) {
        return swaps.returnToOriginal(client, tick);
    }
}
