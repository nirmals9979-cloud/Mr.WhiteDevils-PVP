package com.mrwhitedevil.pvp.features;

import com.mrwhitedevil.pvp.core.FeatureToggle;
import com.mrwhitedevil.pvp.core.SwapController;

public final class CustomSwapDelay extends FeatureToggle {
    private final SwapController swaps;

    public CustomSwapDelay(SwapController swaps) {
        super("custom_swap_delay", "Custom Swap Delay", true);
        this.swaps = swaps;
    }

    public void setDelayTicks(int ticks) { swaps.setDelayTicks(ticks); }
    public int getDelayTicks() { return swaps.delayTicks(); }
}
