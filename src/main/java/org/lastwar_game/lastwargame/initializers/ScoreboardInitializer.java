package org.lastwar_game.lastwargame.initializers;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.lastwar_game.lastwargame.GameWorlds;

public class ScoreboardInitializer {

    public static void initializeAll() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        for (String worldName : GameWorlds.WORLD_NAMES) {
            Objective existing = scoreboard.getObjective(worldName);
            if (existing == null) {
                Objective obj = scoreboard.registerNewObjective(worldName, "dummy", worldName);
                obj.getScore("isGameStarted").setScore(0); // по умолчанию — игра не началась
            }
        }
    }
}
