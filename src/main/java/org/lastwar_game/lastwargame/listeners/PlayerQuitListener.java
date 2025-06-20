package org.lastwar_game.lastwargame.listeners;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.lastwar_game.lastwargame.managers.GameManager;

import java.util.Arrays;
import java.util.List;

public class PlayerQuitListener implements Listener {

    private static final String LOBBY_WORLD = "world";
    private static final List<String> gameWorlds = Arrays.asList(
            "lastwarGame1", "lastwarGame2", "lastwarGame3", "lastwarGame4", "lastwarGame5", "lastwarGame6",
            "lastwarGame7", "lastwarGame8", "lastwarGame9", "lastwarGame10", "lastwarGame11", "lastwarGame12",
            "lastwarGame13", "lastwarGame14", "lastwarGame15", "lastwarGame16", "lastwarGame17", "lastwarGame18",
            "lastwarGame19", "lastwarGame20", "lastwarGame21", "lastwarGame22", "lastwarGame23", "lastwarGame24",
            "lastwarGame25", "lastwarGame26", "lastwarGame27"
    );

//    @EventHandler
//    public void onPlayerQuit(PlayerQuitEvent event) {
//        Player player = event.getPlayer();
//        World playerWorld = player.getWorld();
//
//        // ✅ Проверяем, был ли игрок в игровом мире
//        if (gameWorlds.contains(playerWorld.getName()) || !playerWorld.getName().equals(LOBBY_WORLD)) {
//            Bukkit.getLogger().info("[LastWar] " + player.getName() + " left from " + playerWorld.getName() + ". Returning to lobby on next join.");
//            GameManager.getInstance().checkGameStart(player.getWorld().getName());
//        }
//    }
}
