package org.lastwar_game.lastwargame.listeners.GUI;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.lastwar_game.lastwargame.GameWorlds;
import org.lastwar_game.lastwargame.GUI.ClassSelectionGUI;
import org.lastwar_game.lastwargame.GUI.ServerSelectionGUI;

import java.util.Arrays;
import java.util.List;

public class UniversalCompassListener implements Listener {
    private static final String LOBBY_WORLD = "world"; // ✅ Лобби всегда называется "world"

    @EventHandler
    public void onPlayerUseCompass(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Проверяем, что игрок кликнул именно основной рукой (для совместимости с 1.9+)
        if (event.getHand() != EquipmentSlot.HAND) return;

        // Проверяем, что игрок держит компас
        if (item.getType() == Material.COMPASS && item.getItemMeta() != null) {
            String worldName = player.getWorld().getName();

            if (worldName.equalsIgnoreCase(LOBBY_WORLD)) {
                // игрок в лобби → Открываем выбор сервера
                ServerSelectionGUI.open(player);
            } else if (GameWorlds.WORLD_NAMES.contains(worldName)) {
                // игрок в игровом мире → Открываем выбор класса
//                ClassSelectionGUI.open(player);
            }
        }
    }
}
