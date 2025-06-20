package org.lastwar_game.lastwargame.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.lastwar_game.lastwargame.managers.GameManager;

public class StopCommand implements CommandExecutor {

    private final GameManager gameManager = GameManager.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        World world = player.getWorld();
        String worldName = world.getName();

        if (!worldName.startsWith("lastwarGame")) {
            player.sendMessage("§cYou must be in a game world to stop the game.");
            return true;
        }

        // Удаление всех состояний игроков в этом мире
        for (Player p : world.getPlayers()) {
            gameManager.removePlayerData(p.getUniqueId());

            // Удаляем из таба и скореборда
            Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
            for (Team t : board.getTeams()) {
                t.removeEntry(p.getName());
            }

            // Очистка и телепорт в лобби
            World lobby = Bukkit.getWorld("world");
            if (lobby != null) {
                p.getInventory().clear();
                p.teleport(lobby.getSpawnLocation());
                p.sendMessage(ChatColor.YELLOW + "The game has been stopped. You were returned to the lobby.");
            }
        }

        // Остановить таймеры и очистить мир
        gameManager.resetWorldState(worldName);
        player.sendMessage(ChatColor.GREEN + "Game state in " + worldName + " has been reset.");
        return true;
    }
}
