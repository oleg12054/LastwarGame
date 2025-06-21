package org.lastwar_game.lastwargame.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.lastwar_game.lastwargame.GameWorlds;
import org.lastwar_game.lastwargame.LastWarPlugin;


import java.util.Arrays;
import java.util.List;


public class LobbyManager {
    public static void open(Player player) {
        // Проверяем, что игрок находится в World
        if (!player.getWorld().getName().equalsIgnoreCase("World")) {
            player.sendMessage("§cВыбор сервера доступен только в лобби!");
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 54, "Выбор игры");

        // Заполняем только первые 3 строки (27 слотов)
        for (int i = 0; i < GameWorlds.WORLD_NAMES.size() && i < 27; i++) {
            String worldName = GameWorlds.WORLD_NAMES.get(i);
            World world = Bukkit.getWorld(worldName);
            int playerCount = (world != null) ? world.getPlayers().size() : 0;

            // Получаем значение scoreboard objective `isGameStarted`
            boolean isStarted = false;
            if (Bukkit.getScoreboardManager().getMainScoreboard().getObjective(worldName) != null) {
                isStarted = Bukkit.getScoreboardManager()
                        .getMainScoreboard()
                        .getObjective(worldName) != null &&
                        Bukkit.getScoreboardManager()
                                .getMainScoreboard()
                                .getObjective(worldName)
                                .getScore("isGameStarted")
                                .getScore() == 1;
            }

            Material woolColor;
            if (isStarted) {
                woolColor = Material.RED_WOOL; // Мир заполнен
            } else if (playerCount >= 4) {
                woolColor = Material.YELLOW_WOOL; // игра скоро начнется
            } else {
                woolColor = Material.GREEN_WOOL; // Свободный мир
            }

            ItemStack item = createGuiItem(woolColor, "§a" + worldName, "§7игроков: §e" + playerCount + "§7/10");
            gui.setItem(i, item);
        }

        player.openInventory(gui);
    }

    private final LastWarPlugin plugin;

    public LobbyManager(LastWarPlugin plugin) {
        this.plugin = plugin;
    }

    public void sendPlayerToGame(Player player, String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            player.teleport(world.getSpawnLocation());
        }
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
