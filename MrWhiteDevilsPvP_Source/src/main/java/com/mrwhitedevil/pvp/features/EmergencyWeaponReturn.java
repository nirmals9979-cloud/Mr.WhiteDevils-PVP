package com.mrwhitedevil.pvp.features;

import com.mrwhitedevil.pvp.api.ClientAccess;
import com.mrwhitedevil.pvp.core.FeatureToggle;
import com.mrwhitedevil.pvp.core.SwapController;

public final class EmergencyWeaponReturn extends FeatureToggle {
    private final ClientAccess client;
    private final SwapController swaps;

    public EmergencyWeaponReturn(ClientAccess client, SwapController swaps) {
        super("emergency_weapon_return", "Emergency Weapon Return", false);
        this.client = client; this.swaps = swaps;
    }

    public boolean trigger() {
        if (!enabled()) return false;
        boolean returned = swaps.emergencyReturn(client);
        if (returned) client.notifyAction("Returned to previous hotbar slot");
        return returned;
    }
}
