package com.mrwhitedevil.pvp.fabric;

import com.mrwhitedevil.pvp.api.ClientAccess;
import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class FabricClientAccess implements ClientAccess {
    private final Minecraft mc = Minecraft.getInstance();
    private LocalPlayer p() { return mc.player; }

    @Override public boolean inGame() { return mc.player != null && mc.level != null && mc.gameMode != null; }
    @Override public boolean playerOnGround() { return p() != null && p().onGround(); }
    @Override public boolean playerAttacking() { return mc.options.keyAttack.isDown(); }
    @Override public boolean useKeyDown() { return mc.options.keyUse.isDown(); }
    @Override public boolean lungeChainHeld() {
        boolean ctrl = InputConstants.isKeyDown(mc.getWindow(), 341)
            || InputConstants.isKeyDown(mc.getWindow(), 345);
        return ctrl && mc.options.keyUp.isDown();
    }

    private EntityHitResult entityHit() {
        HitResult h = mc.hitResult;
        return h instanceof EntityHitResult e ? e : null;
    }
    @Override public boolean targetIsLivingEntity() { EntityHitResult e = entityHit(); return e != null && e.getEntity() instanceof LivingEntity; }
    @Override public boolean targetHasShieldRaised() { EntityHitResult e = entityHit(); return e != null && e.getEntity() instanceof LivingEntity le && le.isBlocking(); }
    @Override public boolean targetIsInReach() { return entityHit() != null; }
    @Override public int selectedHotbarSlot() { return p().getInventory().getSelectedSlot(); }
    @Override public void selectHotbarSlot(int slot) { p().getInventory().setSelectedSlot(slot); }

    @Override public List<HotbarStack> hotbar() {
        List<HotbarStack> out = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            ItemStack s = p().getInventory().getItem(i);
            String id = s.isEmpty() ? "" : BuiltInRegistries.ITEM.getKey(s.getItem()).toString();
            out.add(new HotbarStack(i, id, s.getCount(), s.isEmpty()));
        }
        return out;
    }

    @Override public void useMainHand() { if (inGame()) mc.gameMode.useItem(p(), InteractionHand.MAIN_HAND); }

    @Override public void attackTarget() {
        EntityHitResult hit = entityHit();
        if (inGame() && hit != null && hit.getEntity() instanceof LivingEntity) {
            mc.gameMode.attack(p(), hit.getEntity());
        }
    }

    @Override public void notifyAction(String message) {
        if (p() == null) return;
        ChatFormatting color = message.endsWith(": ON") ? ChatFormatting.GREEN
            : message.endsWith(": OFF") ? ChatFormatting.RED : ChatFormatting.YELLOW;
        Component prefix = Component.literal("[PvP] ").withStyle(ChatFormatting.GRAY);
        p().sendOverlayMessage(prefix.copy().append(Component.literal(message).withStyle(color)));
    }
}
