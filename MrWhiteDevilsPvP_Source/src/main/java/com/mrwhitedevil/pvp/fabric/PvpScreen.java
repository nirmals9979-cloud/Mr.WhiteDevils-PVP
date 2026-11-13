package com.mrwhitedevil.pvp.fabric;

import com.mrwhitedevil.pvp.config.PvpSettings;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class PvpScreen extends Screen {
    private final Screen parent;
    private final PvpSettings s;
    private final Runnable onChange;

    public PvpScreen(Screen parent, PvpSettings s, Runnable onChange) {
        super(Component.literal("Mr.WhiteDevil's PvP"));
        this.parent = parent; this.s = s; this.onChange = onChange;
    }

    private void toggle(int row, String name, Supplier<Boolean> get, Consumer<Boolean> set) {
        int x = this.width / 2 - 100, y = 30 + row * 24;
        addRenderableWidget(Button.builder(label(name, get.get()), b -> {
            set.accept(!get.get()); b.setMessage(label(name, get.get())); onChange.run();
        }).bounds(x, y, 200, 20).build());
    }
    private static Component label(String n, boolean v) {
        return Component.literal(n + ": ").append(Component.literal(v ? "ON" : "OFF")
            .withStyle(v ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    @Override protected void init() {
        toggle(0, "Auto Lunge Swap (V)", () -> s.autoLungeSwap, v -> s.autoLungeSwap = v);
        toggle(1, "Auto Stun Slam (J)", () -> s.autoStunSlam, v -> s.autoStunSlam = v);
        toggle(2, "Wind Charge Swap", () -> s.windChargeSwap, v -> s.windChargeSwap = v);
        toggle(3, "Auto Return to Weapon", () -> s.automaticReturn, v -> s.automaticReturn = v);
        toggle(4, "Emergency Weapon Return", () -> s.emergencyWeaponReturn, v -> s.emergencyWeaponReturn = v);
        toggle(5, "Action Notifications", () -> s.actionNotifications, v -> s.actionNotifications = v);

        int y = 30 + 6 * 24, cx = this.width / 2;
        Button delay = Button.builder(delayText(), b -> {}).bounds(cx - 70, y, 140, 20).build();
        delay.active = false;
        addRenderableWidget(Button.builder(Component.literal("-"), b -> { s.swapDelayTicks = Math.max(0, s.swapDelayTicks - 1); delay.setMessage(delayText()); onChange.run(); })
            .bounds(cx - 100, y, 28, 20).build());
        addRenderableWidget(delay);
        addRenderableWidget(Button.builder(Component.literal("+"), b -> { s.swapDelayTicks = Math.min(40, s.swapDelayTicks + 1); delay.setMessage(delayText()); onChange.run(); })
            .bounds(cx + 72, y, 28, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose()).bounds(cx - 100, y + 30, 200, 20).build());
    }
    private Component delayText() { return Component.literal("Swap Delay: " + s.swapDelayTicks + " ticks"); }
    @Override public void onClose() { onChange.run(); this.minecraft.gui.setScreen(parent); }
}
