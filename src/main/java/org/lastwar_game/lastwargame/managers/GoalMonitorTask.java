package org.lastwar_game.lastwargame.managers;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.lastwar_game.lastwargame.LastWarPlugin;

public class GoalMonitorTask extends BukkitRunnable {
    private final String worldName;

    public GoalMonitorTask(String worldName) {
        this.worldName = worldName;
    }

    @Override
    public void run() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective(worldName);
        if (objective == null) return;

        int isGameStarted = objective.getScore("isGameStarted").getScore();
        int isGoalScored = objective.getScore("isGoalScored").getScore();

        if (isGameStarted == 1 && isGoalScored == 1) {
            objective.getScore("isGoalScored").setScore(0);

            Bukkit.broadcastMessage("§6Goal detected in " + worldName + "! Preparing next round...");

            new BukkitRunnable() {
                @Override
                public void run() {
                    GameManager.getInstance().freezeTime(worldName);
                }
            }.runTaskLater(LastWarPlugin.getInstance(), 200L); // 10 сек

            // Можно остановить проверку, если нужно однократно
            // this.cancel(); ❌ Убери, если хочешь постоянный мониторинг
        }
    }
}
