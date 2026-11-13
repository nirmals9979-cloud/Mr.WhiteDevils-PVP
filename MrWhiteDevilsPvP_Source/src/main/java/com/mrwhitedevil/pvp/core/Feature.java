package com.mrwhitedevil.pvp.core;

public interface Feature {
    String id();
    String displayName();
    boolean enabled();
    void setEnabled(boolean enabled);
    void tick();
}
