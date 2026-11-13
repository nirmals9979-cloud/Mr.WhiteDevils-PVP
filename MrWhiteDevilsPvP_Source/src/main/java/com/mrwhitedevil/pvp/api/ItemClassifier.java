package com.mrwhitedevil.pvp.api;

import java.util.Locale;

public final class ItemClassifier {
    private ItemClassifier() {}

    public enum Kind { MACE, WIND_CHARGE, SPEAR, SWORD, AXE, SHIELD, LUNGE_WEAPON, ANY_WEAPON }

    public static boolean matches(String rawId, Kind kind) {
        String id = rawId == null ? "" : rawId.toLowerCase(Locale.ROOT);
        return switch (kind) {
            case MACE -> id.endsWith(":mace") || id.equals("mace");
            case WIND_CHARGE -> id.endsWith(":wind_charge") || id.equals("wind_charge");
            case SPEAR -> id.contains("spear");
            case SWORD -> id.endsWith("_sword") || id.equals("sword");
            case AXE -> id.endsWith("_axe") || id.equals("axe");
            case SHIELD -> id.endsWith(":shield") || id.equals("shield");
            case LUNGE_WEAPON -> id.contains("spear");
            case ANY_WEAPON -> id.endsWith("_sword") || id.endsWith("_axe")
                    || id.endsWith(":mace") || id.contains("spear");
        };
    }
}
