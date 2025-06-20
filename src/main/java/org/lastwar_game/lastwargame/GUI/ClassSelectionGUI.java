package org.lastwar_game.lastwargame.GUI;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.lastwar_game.lastwargame.managers.GameManager;

import java.util.Arrays;
import java.util.Map;

public class ClassSelectionGUI {
    private static final Map<String, Material> classIcons = Map.of(
            "Warrior", Material.IRON_SWORD,
            "Archer", Material.BOW,
            "Mage", Material.BLAZE_POWDER,
            "Tank", Material.SHIELD,
            "Berserker", Material.IRON_AXE,
            "Necromancer", Material.ENDER_EYE
    );

    public static void open(Player player) {
        World world = player.getWorld();
        int playerCount = world.getPlayers().size();

        Inventory gui = Bukkit.createInventory(null, 27, "Class Selection");
        GameManager gameManager = GameManager.getInstance();

        int slot = 0;
        for (Map.Entry<String, Material> entry : classIcons.entrySet()) {
            String className = entry.getKey();
            Material material = entry.getValue();

            boolean isTaken = gameManager.isClassTaken(className);
            ItemStack item = createGuiItem(material, className, isTaken ? "§cTaken" : "§7Available");

            if (isTaken) {
                item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            }

            gui.setItem(slot, item);
            slot++;
        }

        player.openInventory(gui);
    }

    private static ItemStack createGuiItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }
}

//    public static void open(Player player) {
//        World world = player.getWorld();
//        int playerCount = ((World) world).getPlayers().size(); // Получаем количество игроков в мире
//
//        Inventory gui = Bukkit.createInventory(null, 27, "Выберите класс");
//
//        gui.setItem(0, createGuiItem(Material.IRON_SWORD, "Warrior", "§7Players: §e" + playerCount + "§7/10"));
//        gui.setItem(1, createGuiItem(Material.BOW, "Archer", "§7Players: §e" + playerCount + "§7/10"));
//        gui.setItem(2, createGuiItem(Material.BLAZE_POWDER, "Mage", "§7Players: §e" + playerCount + "§7/10"));
//        gui.setItem(3, createGuiItem(Material.SHIELD, "Tank", "§7Players: §e" + playerCount + "§7/10"));
//        gui.setItem(4, createGuiItem(Material.IRON_AXE, "Berserker", "§7Players: §e" + playerCount + "§7/10"));
//        gui.setItem(5, createGuiItem(Material.ENDER_EYE, "Necromancer", "§7Players: §e" + playerCount + "§7/10"));
//
//        player.openInventory(gui);
//    }
//
//    static ItemStack createGuiItem(Material material, String name, String lore) {
//        ItemStack item = new ItemStack(material, 1);
//        ItemMeta meta = item.getItemMeta();
//        if (meta != null) {
//            meta.setDisplayName(name);
//            meta.setLore(Arrays.asList(lore));
//            item.setItemMeta(meta);
//        }
//        return item;
//    }
//}
