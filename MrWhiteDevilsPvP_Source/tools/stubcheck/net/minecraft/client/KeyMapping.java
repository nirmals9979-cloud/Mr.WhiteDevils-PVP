package net.minecraft.client; import com.mojang.blaze3d.platform.InputConstants; import net.minecraft.resources.Identifier;
public class KeyMapping { public static class Category { public static Category register(Identifier i){return null;} }
 public KeyMapping(String n, InputConstants.Type t, int k, Category c){} public boolean isDown(){return false;} public boolean consumeClick(){return false;} }
