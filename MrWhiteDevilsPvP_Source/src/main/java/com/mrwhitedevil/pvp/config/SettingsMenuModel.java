package com.mrwhitedevil.pvp.config;

/** UI-independent settings model for the future in-game screen. */
public final class SettingsMenuModel {
    private final PvpSettings settings;

    public SettingsMenuModel(PvpSettings settings) { this.settings = settings; }

    public PvpSettings settings() { return settings; }

    public void setSwapDelayTicks(int ticks) {
        if (ticks < 0 || ticks > 40) throw new IllegalArgumentException("delay must be 0..40");
        settings.swapDelayTicks = ticks;
    }
}
