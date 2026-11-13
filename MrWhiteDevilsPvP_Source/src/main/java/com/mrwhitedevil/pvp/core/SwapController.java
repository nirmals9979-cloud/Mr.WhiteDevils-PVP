package com.mrwhitedevil.pvp.core;

import com.mrwhitedevil.pvp.api.ClientAccess;
import java.util.OptionalInt;

/** Centralizes slot changes so features do not overwrite each other's return slot. */
public final class SwapController {
    private int originalSlot = -1;
    private int lastSelectedSlot = -1;
    private long lastSwapTick = Long.MIN_VALUE;
    private int delayTicks = 2;

    public void setDelayTicks(int ticks) {
        if (ticks < 0 || ticks > 40) throw new IllegalArgumentException("delay must be 0..40");
        delayTicks = ticks;
    }

    public int delayTicks() { return delayTicks; }

    public boolean canSwap(long tick) {
        return lastSwapTick == Long.MIN_VALUE || tick - lastSwapTick >= delayTicks;
    }

    public boolean swapTo(ClientAccess client, int slot, long tick) {
        if (!client.inGame() || slot < 0 || slot > 8 || !canSwap(tick)) return false;
        if (originalSlot < 0) originalSlot = client.selectedHotbarSlot();
        client.selectHotbarSlot(slot);
        lastSelectedSlot = slot;
        lastSwapTick = tick;
        return true;
    }

    /** Immediate swap used only by the held Lunge chain; bypasses configured delay. */
    public boolean swapToImmediate(ClientAccess client, int slot, long tick) {
        if (!client.inGame() || slot < 0 || slot > 8) return false;
        if (originalSlot < 0) originalSlot = client.selectedHotbarSlot();
        client.selectHotbarSlot(slot);
        lastSelectedSlot = slot;
        lastSwapTick = tick;
        return true;
    }

    public boolean returnToOriginal(ClientAccess client, long tick) {
        if (!client.inGame() || originalSlot < 0 || !canSwap(tick)) return false;
        client.selectHotbarSlot(originalSlot);
        lastSelectedSlot = originalSlot;
        originalSlot = -1;
        lastSwapTick = tick;
        return true;
    }

    public boolean emergencyReturn(ClientAccess client) {
        if (!client.inGame() || originalSlot < 0) return false;
        client.selectHotbarSlot(originalSlot);
        lastSelectedSlot = originalSlot;
        originalSlot = -1;
        return true;
    }

    public OptionalInt originalSlot() {
        return originalSlot < 0 ? OptionalInt.empty() : OptionalInt.of(originalSlot);
    }

    public int lastSelectedSlot() { return lastSelectedSlot; }
}
