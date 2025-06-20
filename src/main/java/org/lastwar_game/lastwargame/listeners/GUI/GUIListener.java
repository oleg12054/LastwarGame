package org.lastwar_game.lastwargame.listeners.GUI;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.lastwar_game.lastwargame.managers.GameManager;
import org.lastwar_game.lastwargame.LastWarPlugin;

import java.util.UUID;

public class GUIListener implements Listener {

    private final GameManager gameManager;

    public GUIListener() {
        this.gameManager = LastWarPlugin.getInstance().getGameManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String inventoryTitle = event.getView().getTitle();

        if (inventoryTitle.equalsIgnoreCase("Team Selection")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null) return;

            UUID playerId = player.getUniqueId();
            if (clickedItem.getType() == Material.RED_WOOL) {
                gameManager.assignPlayerToTeam(playerId, "Red Team");
                player.sendMessage("§cYou have chosen the Red Team!");
            } else if (clickedItem.getType() == Material.BLUE_WOOL) {
                gameManager.assignPlayerToTeam(playerId, "Blue Team");
                player.sendMessage("§9You have chosen the Blue Team!");
            }
            player.closeInventory();
        }

        if (inventoryTitle.equalsIgnoreCase("Choose a Class")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null) return;

            String className = clickedItem.getItemMeta().getDisplayName();
            if (gameManager.isClassAvailable(className)) {
                gameManager.assignClassToPlayer(player, className);
                player.sendMessage("§aYou have chosen the class: " + className);
                player.closeInventory();
            } else {
                player.sendMessage("§cThis class is already taken!");
            }
        }
    }
}
