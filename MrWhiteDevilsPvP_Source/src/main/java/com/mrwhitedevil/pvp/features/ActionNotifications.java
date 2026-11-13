package com.mrwhitedevil.pvp.features;

import com.mrwhitedevil.pvp.api.ClientAccess;
import com.mrwhitedevil.pvp.core.FeatureToggle;

public final class ActionNotifications extends FeatureToggle {
    private final ClientAccess client;

    public ActionNotifications(ClientAccess client) {
        super("action_notifications", "Action Notifications", true);
        this.client = client;
    }

    public void show(String message) {
        if (enabled() && client.inGame()) client.notifyAction(message);
    }
}
