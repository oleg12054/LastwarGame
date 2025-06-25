package org.lastwar_game.lastwargame.managers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.lastwar_game.lastwargame.GameWorlds;
import org.lastwar_game.lastwargame.GUI.ServerSelectionGUI;
import org.lastwar_game.lastwargame.LastWarPlugin;
import org.lastwar_game.lastwargame.managers.GameManager;

import java.util.HashMap;
import java.util.Map;

public class ServerSelectionAutoRefreshTask extends BukkitRunnable {

    private final Map<String, Integer> lastPlayerCounts = new HashMap<>();
    private final Map<String, Boolean> lastRestartStatus = new HashMap<>();
    private final Map<String, Boolean> lastGameStartStatus = new HashMap<>();

    @Override
    public void run() {
        boolean shouldRefresh = false;

        for (String worldName : GameWorlds.WORLD_NAMES) {
            World world = Bukkit.getWorld(worldName);
            int playerCount = (world != null) ? world.getPlayers().size() : 0;

            boolean isRestarting = GameManager.getInstance().isWorldRestarting(worldName);
            boolean isStarted = false;
            boolean isClass = false;

            if (Bukkit.getScoreboardManager().getMainScoreboard().getObjective(worldName) != null) {
                var obj = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(worldName);
                isStarted = obj.getScore("isGameStarted").getScore() == 1;
                isClass = obj.getScore("isClassSelection").getScore() == 1;
            }

            // Если изменился хоть один из параметров — флаг обновления
            if (
                    lastPlayerCounts.getOrDefault(worldName, -1) != playerCount ||
                            lastRestartStatus.getOrDefault(worldName, false) != isRestarting ||
                            lastGameStartStatus.getOrDefault(worldName, false) != (isStarted || isClass)
            ) {
                shouldRefresh = true;

                lastPlayerCounts.put(worldName, playerCount);
                lastRestartStatus.put(worldName, isRestarting);
                lastGameStartStatus.put(worldName, isStarted || isClass);
            }
        }

        if (shouldRefresh) {
            ServerSelectionGUI.refreshForAllViewers();
        }
    }

    public static void start(LastWarPlugin plugin) {
        new ServerSelectionAutoRefreshTask().runTaskTimer(plugin, 0L, 20L); // каждую секунду
    }
}
