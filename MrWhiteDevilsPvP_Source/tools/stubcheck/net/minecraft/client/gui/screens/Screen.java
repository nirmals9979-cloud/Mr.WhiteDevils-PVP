package net.minecraft.client.gui.screens; import net.minecraft.client.Minecraft; import net.minecraft.network.chat.Component; import net.minecraft.client.gui.components.Button;
public class Screen { protected Minecraft minecraft; protected int width; protected Screen(Component c){} protected void init(){} public void onClose(){} protected <T extends Button> T addRenderableWidget(T t){return t;} }
