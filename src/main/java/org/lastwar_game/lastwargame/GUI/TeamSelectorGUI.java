package org.lastwar_game.lastwargame.GUI;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.lastwar_game.lastwargame.managers.GameManager;

public class TeamSelectorGUI {

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§eSelect Your Team");

        // Заполнение фона стеклянными панелями
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, glass);
        }

        // Красная команда (центр слева)
        ItemStack redWool = new ItemStack(Material.RED_WOOL);
        ItemMeta redMeta = redWool.getItemMeta();
        if (redMeta != null) {
            redMeta.setDisplayName("§cJoin RED Team");
            redWool.setItemMeta(redMeta);
        }

        // Синяя команда (центр справа)
        ItemStack blueWool = new ItemStack(Material.BLUE_WOOL);
        ItemMeta blueMeta = blueWool.getItemMeta();
        if (blueMeta != null) {
            blueMeta.setDisplayName("§9Join BLUE Team");
            blueWool.setItemMeta(blueMeta);
        }

        // Устанавливаем красную и синюю шерсть в центр
        gui.setItem(11, redWool);
        gui.setItem(15, blueWool);

        player.openInventory(gui);
    }
}
