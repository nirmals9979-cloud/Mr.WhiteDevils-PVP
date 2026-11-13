package net.fabricmc.fabric.api.client.event.lifecycle.v1; import net.fabricmc.fabric.api.event.Event; import net.minecraft.client.Minecraft;
public class ClientTickEvents { public interface EndTick { void onEndTick(Minecraft c); } public static final Event<EndTick> END_CLIENT_TICK = new Event<>(); }
