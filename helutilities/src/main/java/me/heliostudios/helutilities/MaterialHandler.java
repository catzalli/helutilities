package me.heliostudios.helutilities;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MaterialHandler {
    public static ItemStack createItemStack(String input, int amount) {
        try {
            Material material;
            if (input.contains(":")) {
                // minecraft:stone formatını stone'a çevir
                String[] parts = input.split(":");
                material = Material.valueOf(parts[1].toUpperCase());
            } else {
                material = Material.valueOf(input.toUpperCase());
            }
            return new ItemStack(material, amount);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String getMaterialName(Material material) {
        return material.name().toLowerCase();
    }
}