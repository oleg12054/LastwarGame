package org.lastwar_game.lastwargame.managers;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LobbyItems {

    public static void giveCompass(Player player, String displayName) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            compass.setItemMeta(meta);
        }
        player.getInventory().setItem(0, compass);
    }

    public static void givePaper(Player player, String displayName) {
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            paper.setItemMeta(meta);
        }
        player.getInventory().setItem(4, paper);
    }

    public static void giveRedConcrete(Player player, String displayName) {
        ItemStack concrete = new ItemStack(Material.RED_CONCRETE);
        ItemMeta meta = concrete.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            concrete.setItemMeta(meta);
        }
        player.getInventory().setItem(8, concrete);
    }
}