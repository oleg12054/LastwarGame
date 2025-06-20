package org.lastwar_game.lastwargame.listeners.GUI;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.lastwar_game.lastwargame.GUI.TeamSelectorGUI;
import org.lastwar_game.lastwargame.managers.GameManager;

import java.util.EnumSet;
import java.util.Set;

public class TeamSelectionClickListener implements Listener {
    private static final Set<Material> TEAM_WOOL = EnumSet.of(Material.WHITE_WOOL, Material.RED_WOOL, Material.BLUE_WOOL);


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // ✅ Проверяем, что игрок кликнул по шерсти "Team Selection"
        if (TEAM_WOOL.contains(item.getType()) && item.getItemMeta() != null &&
                item.getItemMeta().getDisplayName().equals("§eTeam Selection")) {

            if (GameManager.getInstance().isTeamSelectionLocked(player.getUniqueId())) {
                player.sendMessage("§cYou can no longer change teams!");
                return;
            }

            TeamSelectorGUI.open(player); // ✅ Открываем меню выбора команды
        }
    }
}
