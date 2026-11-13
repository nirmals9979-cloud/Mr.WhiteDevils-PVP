package com.mrwhitedevil.pvp.core;

public abstract class FeatureToggle implements Feature {
    private boolean enabled;
    private final String id;
    private final String displayName;

    protected FeatureToggle(String id, String displayName, boolean defaultEnabled) {
        this.id = id;
        this.displayName = displayName;
        this.enabled = defaultEnabled;
    }

    @Override public final String id() { return id; }
    @Override public final String displayName() { return displayName; }
    @Override public final boolean enabled() { return enabled; }
    @Override public final void setEnabled(boolean enabled) { this.enabled = enabled; }
    @Override public void tick() {}
}
