package org.lastwar_game.lastwargame.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ClassItemManager {

    public static void giveItemsForTaggedClass(Player player) {
        Set<String> tags = player.getScoreboardTags();

        if (tags.contains("LadyNagant")) {
            assignLadyNagant(player);
        } else if (tags.contains("Archer")) {
            String name = player.getName();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givearcherkit " + name);
        } else if (tags.contains("Tank")) {
            String name = player.getName();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givestarterkit " + name);
        } else if (tags.contains("Saske")) {
            assignSaske(player);
        } else {
            player.sendMessage("§cNo class tag found, giving default item.");
            player.getInventory().addItem(new ItemStack(Material.STICK));
        }
    }

    private static void assignLadyNagant(Player player) {
        String name = player.getName();
        player.removeScoreboardTag("LadyNagant");
        player.addScoreboardTag("LadyNagan");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "setclass LadyNagant " + name);

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "giveladynagan " + name);
    }
    private static void assignSaske(Player player) {
        String name = player.getName();
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "setclass Saske " + name);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givesaske " + name);
    }

}
