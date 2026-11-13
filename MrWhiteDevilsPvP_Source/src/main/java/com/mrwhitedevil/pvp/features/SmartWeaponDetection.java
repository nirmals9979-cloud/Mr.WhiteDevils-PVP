package com.mrwhitedevil.pvp.features;

import com.mrwhitedevil.pvp.api.ClientAccess;
import com.mrwhitedevil.pvp.api.ItemClassifier;
import java.util.OptionalInt;

public final class SmartWeaponDetection {
    private final ClientAccess client;

    public SmartWeaponDetection(ClientAccess client) { this.client = client; }

    public OptionalInt find(ItemClassifier.Kind kind) {
        return client.firstItem(kind).map(s -> OptionalInt.of(s.slot())).orElseGet(OptionalInt::empty);
    }

    public OptionalInt findBestGeneralWeapon() {
        for (ItemClassifier.Kind kind : new ItemClassifier.Kind[] {
                ItemClassifier.Kind.MACE, ItemClassifier.Kind.SPEAR,
                ItemClassifier.Kind.SWORD, ItemClassifier.Kind.AXE }) {
            OptionalInt slot = find(kind);
            if (slot.isPresent()) return slot;
        }
        return OptionalInt.empty();
    }
}
