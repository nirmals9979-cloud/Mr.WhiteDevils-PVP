package com.mrwhitedevil.pvp.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import com.mrwhitedevil.pvp.config.ConfigStore;
import com.mrwhitedevil.pvp.config.PvpSettings;
import com.mrwhitedevil.pvp.core.PvpController;
import java.nio.file.Path;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public final class MrWhiteDevilsPvPClient implements ClientModInitializer {
    private static long tick = 0;
    private static PvpSettings settings;
    private static PvpController controller;
    private static Path configPath;

    @Override public void onInitializeClient() {
        configPath = FabricLoader.getInstance().getConfigDir().resolve("mrwhitedevilspvp.properties");
        settings = ConfigStore.load(configPath);
        controller = new PvpController(new FabricClientAccess(), settings);

        KeyMapping.Category cat = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("mrwhitedevilspvp", "main"));
        KeyMapping menu = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.mrwhitedevilspvp.menu", InputConstants.Type.KEYBOARD, InputConstants.KEY_RSHIFT, cat));
        KeyMapping lunge = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.mrwhitedevilspvp.lunge", InputConstants.Type.KEYBOARD, InputConstants.KEY_V, cat));
        KeyMapping stun = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.mrwhitedevilspvp.stunslam", InputConstants.Type.KEYBOARD, InputConstants.KEY_J, cat));
        KeyMapping emerg = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.mrwhitedevilspvp.emergency", InputConstants.Type.KEYBOARD, InputConstants.KEY_R, cat));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            tick++;
            while (menu.consumeClick()) mc.gui.setScreen(new PvpScreen(mc.gui.screen(), settings, MrWhiteDevilsPvPClient::changed));
            if (mc.player == null) return;
            while (lunge.consumeClick()) controller.onLungeWindow(tick);
            while (stun.consumeClick()) {
                settings.autoStunSlam = !settings.autoStunSlam;
                changed();
                controller.notifications.show("Auto Stun Slam: " + (settings.autoStunSlam ? "ON" : "OFF"));
            }
            while (emerg.consumeClick()) controller.onEmergencyKey();
            controller.tick(tick);
        });
    }

    private static void changed() {
        controller.applySettings();
        try { ConfigStore.save(configPath, settings); } catch (Exception ignored) {}
    }
}
