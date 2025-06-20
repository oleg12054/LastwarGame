package org.lastwar_game.lastwargame.listeners;

import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.entity.Player;
import org.lastwar_game.lastwargame.managers.GameManager;

import java.util.UUID;

public class PlayerMovementFreezeListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (GameManager.getInstance().isPlayerFrozen(uuid)) {
            Location fixedLoc = GameManager.getInstance().getFrozenLocation(uuid);

            if (event.getTo().distance(fixedLoc) > 0.1) {
                player.teleport(fixedLoc); // телепорт обратно при смещении
            }
        }
    }
}