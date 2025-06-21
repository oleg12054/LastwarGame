package org.lastwar_game.lastwargame.GUI;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.lastwar_game.lastwargame.GameWorlds; // 👈 импортируешь общий класс

import java.util.Arrays;

public class ServerSelectionGUI {

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, "Server Selection");

        for (int i = 0; i < GameWorlds.WORLD_NAMES.size() && i < 27; i++) { // 👈 используешь общее
            String worldName = GameWorlds.WORLD_NAMES.get(i);
            World world = Bukkit.getWorld(worldName);
            int playerCount = (world != null) ? world.getPlayers().size() : 0;

            boolean isStarted = false;
            boolean  isClassSelection = false;
            if (Bukkit.getScoreboardManager().getMainScoreboard().getObjective(worldName) != null) {
                isStarted = Bukkit.getScoreboardManager()
                        .getMainScoreboard()
                        .getObjective(worldName)
                        .getScore("isGameStarted")
                        .getScore() == 1;
                isClassSelection = Bukkit.getScoreboardManager()
                        .getMainScoreboard()
                        .getObjective(worldName)
                        .getScore("isClassSelection")
                        .getScore() == 1;
            }

            Material woolColor;
            if (isStarted || isClassSelection) {
                woolColor = Material.RED_WOOL;
            } else if (playerCount >= 4) {
                woolColor = Material.YELLOW_WOOL;
            } else {
                woolColor = Material.GREEN_WOOL;
            }

            ItemStack item = createGuiItem(woolColor, "§a" + worldName, "§7игроков: §e" + playerCount + "§7/10");
            gui.setItem(i, item);
        }

        player.openInventory(gui);
    }

    private static ItemStack createGuiItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }
}
