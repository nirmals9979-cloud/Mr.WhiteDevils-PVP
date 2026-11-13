package com.mrwhitedevil.pvp.features;

import com.mrwhitedevil.pvp.api.ClientAccess;
import com.mrwhitedevil.pvp.api.ItemClassifier;
import com.mrwhitedevil.pvp.core.FeatureToggle;
import com.mrwhitedevil.pvp.core.SwapController;
import java.util.OptionalInt;

/** Shield-selection controller; adapter must supply legitimate use/block input handling. */
public final class AlwaysShield extends FeatureToggle {
    private final ClientAccess client;
    private final SmartWeaponDetection detection;
    private final SwapController swaps;

    public AlwaysShield(ClientAccess client, SmartWeaponDetection detection, SwapController swaps) {
        super("always_shield", "Always Shield", false);
        this.client = client; this.detection = detection; this.swaps = swaps;
    }

    public boolean selectShield(long tick) {
        if (!enabled() || !client.inGame()) return false;
        OptionalInt slot = detection.find(ItemClassifier.Kind.SHIELD);
        return slot.isPresent() && swaps.swapTo(client, slot.getAsInt(), tick);
    }
}
