package com.mrwhitedevil.pvp.features;

import com.mrwhitedevil.pvp.api.ClientAccess;
import com.mrwhitedevil.pvp.api.ItemClassifier;
import com.mrwhitedevil.pvp.core.FeatureToggle;
import com.mrwhitedevil.pvp.core.SwapController;
import java.util.OptionalInt;

public final class WindChargeSwap extends FeatureToggle {
    private final ClientAccess client;
    private final SmartWeaponDetection detection;
    private final SwapController swaps;

    public WindChargeSwap(ClientAccess client, SmartWeaponDetection detection, SwapController swaps) {
        super("wind_charge_swap", "Wind Charge Swap", false);
        this.client = client; this.detection = detection; this.swaps = swaps;
    }

    public boolean selectWindCharge(long tick) {
        if (!enabled() || !client.inGame()) return false;
        OptionalInt slot = detection.find(ItemClassifier.Kind.WIND_CHARGE);
        return slot.isPresent() && swaps.swapTo(client, slot.getAsInt(), tick);
    }
}
