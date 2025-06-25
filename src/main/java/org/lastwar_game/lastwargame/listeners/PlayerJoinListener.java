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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.lastwar_game.lastwargame.GameWorlds;
import org.lastwar_game.lastwargame.LastWarPlugin;
import org.lastwar_game.lastwargame.managers.GameManager;
import org.lastwar_game.lastwargame.managers.LobbyManager;

import java.util.Arrays;
import java.util.List;

import static org.lastwar_game.lastwargame.managers.LobbyItems.*;

public class PlayerJoinListener implements Listener {

    private final LobbyManager lobbyManager;
    private final LastWarPlugin plugin;

    public PlayerJoinListener(LastWarPlugin plugin) {
        this.plugin = plugin;
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

        // Очистка эффектов и добавление сопротивления урону
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60, 255, false, false, false)); // 3 сек

        // Удаляем игрока из всех команд основного scoreboard
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        for (Team team : board.getTeams()) {
            team.removeEntry(player.getName());
        }


        // Моментальная очистка и выдача предметов
        player.getInventory().clear();
        giveCompass(player, "§eSelect Game");
        //givePaper(player, "§bJoin Available Game");
        giveRedConcrete(player, "§cReturn to HUB");
        player.updateInventory();

        // Отложенная телепортация
        new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(new Location(lobby, 118.5, 68.01, -183.5, 90, 0));
                player.sendMessage("§aWelcome to the lobby! Use the compass to select a game.");

                // Удаляем все эффекты (включая сопротивление)
                player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));

                // Повторно очищаем инвентарь на случай ошибок и даём нужные предметы
                player.getInventory().clear();
                giveCompass(player, "§eSelect Game");
                givePaper(player, "§bJoin Available Game");
                giveRedConcrete(player, "§cReturn to HUB");
                player.updateInventory();

            }
        }.runTaskLater(plugin, 10L); // 2 секунды = 40 тиков
    }

}

