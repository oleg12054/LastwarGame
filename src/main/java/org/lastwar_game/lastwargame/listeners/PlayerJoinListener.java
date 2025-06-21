package org.lastwar_game.lastwargame.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.lastwar_game.lastwargame.GameWorlds;
import org.lastwar_game.lastwargame.LastWarPlugin;
import org.lastwar_game.lastwargame.managers.GameManager;
import org.lastwar_game.lastwargame.managers.LobbyManager;

import java.util.Arrays;
import java.util.List;

import static org.lastwar_game.lastwargame.managers.LobbyItems.*;

public class PlayerJoinListener implements Listener {

    private final LobbyManager lobbyManager;
    private static final String LOBBY_WORLD = "world"; // ✅ Лобби всегда называется "world"
    private static final int MIN_PLAYERS_TO_START = 2;
    private static final int MAX_PLAYERS = 10;
    public PlayerJoinListener(LastWarPlugin plugin) {
        this.lobbyManager = plugin.getLobbyManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        World lobby = Bukkit.getWorld("world");

        if (lobby == null) {
            player.kickPlayer("§cLobby world not found!");
            return;
        }

        // ✅ Always clear inventory and re-give fixed items when joining the lobby
        player.getInventory().clear();
        giveCompass(player, "§eSelect Game");
        givePaper(player, "§bJoin Available Game");
        giveRedConcrete(player, "§cReturn to HUB");

        // Prevent movement of these items elsewhere
        player.updateInventory();
        // ✅ Всегда телепортируем в лобби при входе
        if (!player.getWorld().getName().equals("world")) {
            player.getInventory().clear();
            giveCompass(player, "§eSelect Game");
            player.teleport(new Location(lobby, 118.5, 68.01, -183.5, 90, 0));
            player.sendMessage("§aWelcome to the lobby! Use the compass to select a game.");
        }
    }
}

