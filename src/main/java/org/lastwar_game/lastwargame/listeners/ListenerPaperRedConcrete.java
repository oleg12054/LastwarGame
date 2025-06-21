package org.lastwar_game.lastwargame.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.lastwar_game.lastwargame.GameWorlds;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.List;
public class ListenerPaperRedConcrete implements Listener {


    @EventHandler
    public void onItemUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if (item == null || item.getType() == Material.AIR || item.getItemMeta() == null) return;
        ItemMeta meta = item.getItemMeta();
        String name = meta.getDisplayName();

        // ✅ Бумага — выбор первого доступного игрового мира
        if (item.getType() == Material.PAPER && name.equals("§bJoin Available Game")) {
            for (String worldName : GameWorlds.WORLD_NAMES) {
                World world = Bukkit.getWorld(worldName);
                if (world == null) continue;

                int players = world.getPlayers().size();
                boolean isEmpty = players == 0;
                boolean notStarted = players < 10; // или по дополнительным условиям, если они есть

                if (isEmpty || notStarted) {
                    player.teleport(world.getSpawnLocation());
                    player.sendMessage("§aYou joined: " + worldName);
                    return;
                }
            }
            player.sendMessage("§cNo available games at the moment!");
        }
        // 🟥 Красный бетон — переход на сервер "lobby" через BungeeCord
        if (item.getType() == Material.RED_CONCRETE && name.equals("§cReturn to HUB")) {
            sendToServer(player, "lobby");
        }
    }

    // ✅ Отправляет игрока на другой сервер через BungeeCord (BungeeGuard)
    private void sendToServer(Player player, String serverName) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteStream);

        try {
            out.writeUTF("Connect");
            out.writeUTF(serverName);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        player.sendPluginMessage(
                org.lastwar_game.lastwargame.LastWarPlugin.getInstance(),
                "BungeeCord",
                byteStream.toByteArray()
        );
    }
}