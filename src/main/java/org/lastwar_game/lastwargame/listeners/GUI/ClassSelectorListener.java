package org.lastwar_game.lastwargame.listeners.GUI;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.lastwar_game.lastwargame.managers.GameManager;

public class ClassSelectorListener implements Listener {

    @EventHandler
    public void onClassSelect(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Class Selection")) return;

        event.setCancelled(true); // ❌ Нельзя брать предметы из GUI

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String className = clicked.getItemMeta().getDisplayName();

        // ❌ Проверка на занятие
        if (clicked.getEnchantments().containsKey(Enchantment.DURABILITY)) {
            player.sendMessage("§cThis class is already taken!");
            return;
        }

        boolean assigned = GameManager.getInstance().assignClassToPlayer(player, className);

        if (assigned) {
            player.closeInventory();
            player.sendMessage("§aYou selected the class: §e" + className);
            System.out.println("[Class Selection] " + player.getName() + " selected class: " + className);
        } else {
            player.sendMessage("§cFailed to select class. It may already be taken.");
        }
    }
}
