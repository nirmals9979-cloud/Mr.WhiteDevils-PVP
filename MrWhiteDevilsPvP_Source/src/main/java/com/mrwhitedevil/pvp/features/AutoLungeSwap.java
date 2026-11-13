package com.mrwhitedevil.pvp.features;

import com.mrwhitedevil.pvp.api.ClientAccess;
import com.mrwhitedevil.pvp.api.ItemClassifier;
import com.mrwhitedevil.pvp.core.FeatureToggle;
import com.mrwhitedevil.pvp.core.SwapController;
import java.util.OptionalInt;

/** Generic feature controller. Exact Lunge trigger must be connected in the Fabric adapter. */
public final class AutoLungeSwap extends FeatureToggle {
    private final ClientAccess client;
    private final SmartWeaponDetection detection;
    private final SwapController swaps;

    public AutoLungeSwap(ClientAccess client, SmartWeaponDetection detection, SwapController swaps) {
        super("auto_lunge_swap", "Auto Lunge Swap", true);
        this.client = client; this.detection = detection; this.swaps = swaps;
    }

    /** Call only when the adapter detects the confirmed Lunge action window. */
    public boolean onLungeWindow(long tick) {
        if (!enabled() || !client.inGame()) return false;
        OptionalInt slot = detection.find(ItemClassifier.Kind.SPEAR);
        return slot.isPresent() && swaps.swapTo(client, slot.getAsInt(), tick);
    }
}
