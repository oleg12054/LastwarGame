package org.lastwar_game.lastwargame.listeners.GUI;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.lastwar_game.lastwargame.LastWarPlugin;
import org.lastwar_game.lastwargame.managers.GameManager;
import org.lastwar_game.lastwargame.managers.LobbyManager;

public class ServerSelectionListener implements Listener {

    private final LobbyManager lobbyManager;

    public ServerSelectionListener(LastWarPlugin plugin) {
        this.lobbyManager = plugin.getLobbyManager();
    }

    @EventHandler
    public void onServerSelect(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (!event.getView().getTitle().equalsIgnoreCase("Server Selection")) return;
        event.setCancelled(true); // ✅ Запрещаем перемещение предметов

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        String worldName = clickedItem.getItemMeta().getDisplayName().replace("§a", "").trim();

        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            player.teleport(world.getSpawnLocation());
            player.getInventory().clear();
            GameManager.getInstance().giveTeamSelectionItem(player); // ✅ Выдаём шерсть выбора команды
            player.closeInventory();
        } else {
            player.sendMessage("§cThis server is unavailable!");
        }
    }

    private ItemStack createCompass(String displayName) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            compass.setItemMeta(meta);
        }
        return compass;
    }
}

