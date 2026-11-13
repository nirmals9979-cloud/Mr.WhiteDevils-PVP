package com.mrwhitedevil.pvp.api;

import java.util.List;
import java.util.Optional;

public interface ClientAccess {
    boolean inGame();
    boolean playerOnGround();
    boolean playerAttacking();
    boolean targetIsLivingEntity();
    boolean targetHasShieldRaised();
    boolean targetIsInReach();
    int selectedHotbarSlot();
    void selectHotbarSlot(int slot);
    List<HotbarStack> hotbar();
    void useMainHand();
    void attackTarget();
    boolean useKeyDown();
    void notifyAction(String message);

    default boolean selectedItemIsSwordOrAxe() {
        return hotbar().stream()
            .filter(s -> s.slot() == selectedHotbarSlot() && !s.empty())
            .anyMatch(s -> ItemClassifier.matches(s.itemId(), ItemClassifier.Kind.SWORD)
                || ItemClassifier.matches(s.itemId(), ItemClassifier.Kind.AXE));
    }

    record HotbarStack(int slot, String itemId, int count, boolean empty) {
        public HotbarStack {
            if (slot < 0 || slot > 8) throw new IllegalArgumentException("slot must be 0..8");
            itemId = itemId == null ? "" : itemId;
        }
    }

    default Optional<HotbarStack> firstItem(ItemClassifier.Kind kind) {
        return hotbar().stream().filter(s -> !s.empty() && ItemClassifier.matches(s.itemId(), kind)).findFirst();
    }
}
