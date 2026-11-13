package net.minecraft.client.gui.components; import net.minecraft.network.chat.Component;
public class Button { public boolean active; public interface OnPress{ void onPress(Button b);} public static class Builder{ public Builder bounds(int a,int b,int c,int d){return this;} public Button build(){return new Button();} }
 public static Builder builder(Component c, OnPress p){return new Builder();} public void setMessage(Component c){} }
