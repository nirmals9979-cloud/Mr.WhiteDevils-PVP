package com.mrwhitedevil.pvp.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import com.mrwhitedevil.pvp.config.ConfigStore;
import com.mrwhitedevil.pvp.config.PvpSettings;
import com.mrwhitedevil.pvp.core.PvpController;
import java.nio.file.Path;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;

/** Client entrypoint (26.x: KeyMappingHelper, gui.setScreen). Not yet compiled against the 26.3 jar. */
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
        KeyMapping menu   = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.mrwhitedevilspvp.menu", InputConstants.Type.KEYSYM, InputConstants.KEY_RSHIFT, cat));
        KeyMapping wind   = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.mrwhitedevilspvp.windcharge", InputConstants.Type.KEYSYM, InputConstants.KEY_G, cat));
        KeyMapping shield = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.mrwhitedevilspvp.shield", InputConstants.Type.KEYSYM, InputConstants.KEY_H, cat));
        KeyMapping emerg  = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.mrwhitedevilspvp.emergency", InputConstants.Type.KEYSYM, InputConstants.KEY_R, cat));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            tick++;
            while (menu.consumeClick()) mc.gui.setScreen(new PvpScreen(mc.gui.screen(), settings, MrWhiteDevilsPvPClient::changed));
            if (mc.player == null) return;
            while (wind.consumeClick()) controller.onWindChargeKey(tick);
            while (shield.consumeClick()) controller.onShieldKey(tick);
            while (emerg.consumeClick()) controller.onEmergencyKey();
            controller.tick(tick);
        });

        // Player's own attack: swap happens before the attack is sent (vanilla syncs the held slot in attack()).
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (level.isClientSide()) controller.onAttackAttempt(tick);
            return InteractionResult.PASS;
        });
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (level.isClientSide()) controller.onItemUsed(tick);
            return InteractionResult.PASS;
        });
    }

    private static void changed() {
        controller.applySettings();
        try { ConfigStore.save(configPath, settings); } catch (Exception ignored) {}
    }
}
