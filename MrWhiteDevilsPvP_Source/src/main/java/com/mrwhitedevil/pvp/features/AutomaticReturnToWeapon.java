package com.mrwhitedevil.pvp.features;

import com.mrwhitedevil.pvp.api.ClientAccess;
import com.mrwhitedevil.pvp.core.FeatureToggle;
import com.mrwhitedevil.pvp.core.SwapController;

public final class AutomaticReturnToWeapon extends FeatureToggle {
    private final ClientAccess client;
    private final SwapController swaps;

    public AutomaticReturnToWeapon(ClientAccess client, SwapController swaps) {
        super("automatic_return", "Automatic Return to Weapon", true);
        this.client = client; this.swaps = swaps;
    }

    public boolean afterAction(long tick) {
        return enabled() && swaps.returnToOriginal(client, tick);
    }
}
