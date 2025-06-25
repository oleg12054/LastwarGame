package org.lastwar_game.lastwargame.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.lastwar_game.lastwargame.LastWarPlugin;
import org.lastwar_game.lastwargame.managers.GameManager;
import org.lastwar_game.lastwargame.managers.LobbyItems;

public class EndGameCommand implements CommandExecutor {

    private final GameManager gameManager = GameManager.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // ✅ Только игрок может вызвать
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can run this command.");
            return true;
        }

        Player player = (Player) sender;

        // ✅ Проверка прав: OP или permission
        if (!player.isOp() && !player.hasPermission("lastwar.endgame")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        World world = player.getWorld();

        // ✅ Проверка что это игровой мир
        if (!world.getName().startsWith("lastwarGame")) {
            player.sendMessage(ChatColor.RED + "You must be in a game world to end the game.");
            return true;
        }

        Bukkit.broadcastMessage("§cThe game has been stopped manually!");

        GameManager gm = GameManager.getInstance();
        gm.endGame(world);


        return true;
    }
}

