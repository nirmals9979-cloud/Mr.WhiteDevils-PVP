package com.mrwhitedevil.pvp.features;

import com.mrwhitedevil.pvp.api.ClientAccess;
import com.mrwhitedevil.pvp.api.ItemClassifier;
import com.mrwhitedevil.pvp.core.FeatureToggle;
import com.mrwhitedevil.pvp.core.SwapController;
import java.util.OptionalInt;

/**
 * Client-side combo coordinator. Does not apply server-side stun effects.
 * Adapter calls onAttackAttempt() from the verified attack input/event hook.
 */
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
                || !client.targetIsLivingEntity() || !client.targetHasShieldRaised()
                || !client.targetIsInReach()) return false;

        OptionalInt mace = detection.find(ItemClassifier.Kind.MACE);
        if (mace.isEmpty()) return false;
        return swaps.swapTo(client, mace.getAsInt(), tick);
    }

    /** Called by adapter after the combo's legitimate attack action has completed. */
    public boolean finishSequence(long tick) {
        return swaps.returnToOriginal(client, tick);
    }
}
