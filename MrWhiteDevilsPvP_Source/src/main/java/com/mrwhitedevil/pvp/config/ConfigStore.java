package com.mrwhitedevil.pvp.config;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/** Plain .properties persistence for PvpSettings. */
public final class ConfigStore {
    private ConfigStore() {}

    public static void save(Path file, PvpSettings s) throws IOException {
        Properties p = new Properties();
        p.setProperty("swapDelayTicks", Integer.toString(s.swapDelayTicks));
        p.setProperty("autoLungeSwap", Boolean.toString(s.autoLungeSwap));
        p.setProperty("alwaysShield", Boolean.toString(s.alwaysShield));
        p.setProperty("autoStunSlam", Boolean.toString(s.autoStunSlam));
        p.setProperty("windChargeSwap", Boolean.toString(s.windChargeSwap));
        p.setProperty("automaticReturn", Boolean.toString(s.automaticReturn));
        p.setProperty("emergencyWeaponReturn", Boolean.toString(s.emergencyWeaponReturn));
        p.setProperty("actionNotifications", Boolean.toString(s.actionNotifications));
        if (file.getParent() != null) Files.createDirectories(file.getParent());
        try (OutputStream o = Files.newOutputStream(file)) { p.store(o, "Mr.WhiteDevil's PvP"); }
    }

    public static PvpSettings load(Path file) {
        PvpSettings s = new PvpSettings();
        if (!Files.exists(file)) return s;
        Properties p = new Properties();
        try (InputStream i = Files.newInputStream(file)) { p.load(i); } catch (IOException e) { return s; }
        try { s.swapDelayTicks = Math.max(0, Math.min(40, Integer.parseInt(p.getProperty("swapDelayTicks", "2")))); }
        catch (NumberFormatException ignored) {}
        s.autoLungeSwap = b(p, "autoLungeSwap", s.autoLungeSwap);
        s.alwaysShield = b(p, "alwaysShield", s.alwaysShield);
        s.autoStunSlam = b(p, "autoStunSlam", s.autoStunSlam);
        s.windChargeSwap = b(p, "windChargeSwap", s.windChargeSwap);
        s.automaticReturn = b(p, "automaticReturn", s.automaticReturn);
        s.emergencyWeaponReturn = b(p, "emergencyWeaponReturn", s.emergencyWeaponReturn);
        s.actionNotifications = b(p, "actionNotifications", s.actionNotifications);
        return s;
    }

    private static boolean b(Properties p, String k, boolean d) {
        String v = p.getProperty(k); return v == null ? d : Boolean.parseBoolean(v);
    }
}
