//package org.lastwar_game.lastwargame.listeners;
//
//import org.bukkit.Material;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.Listener;
//import org.bukkit.event.player.PlayerInteractEvent;
//import org.bukkit.inventory.EquipmentSlot;
//import org.bukkit.inventory.ItemStack;
//import org.lastwar_game.lastwargame.GUI.ServerSelectionGUI;
//
//public class CompassListener implements Listener {
//
//    @EventHandler
//    public void onPlayerUseCompass(PlayerInteractEvent event) {
//        Player player = event.getPlayer();
//        ItemStack item = player.getInventory().getItemInMainHand();
//
//        // Проверяем, что игрок кликнул именно основной рукой (для совместимости с 1.9+)
//        if (event.getHand() != EquipmentSlot.HAND) return;
//
//        if (item.getType() == Material.COMPASS && item.getItemMeta() != null
//                && item.getItemMeta().getDisplayName().equals("§eВыбор игры")) {
//            ServerSelectionGUI.open(player);
//        }
//    }
//}