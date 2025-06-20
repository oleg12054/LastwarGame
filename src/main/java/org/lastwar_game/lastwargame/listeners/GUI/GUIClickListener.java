package org.lastwar_game.lastwargame.listeners.GUI;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class GUIClickListener implements Listener {
    private static final List<Material> lockedItems = Arrays.asList(
            Material.COMPASS,
            Material.WHITE_WOOL,
            Material.RED_WOOL,
            Material.BLUE_WOOL,
            Material.PAPER,
            Material.RED_CONCRETE
    );
    private static final List<String> guiTitles = Arrays.asList(
            "Server Selection",
            "Class Selection",
            "Team Selection",
            "Game Selection"
    );

    /** ✅ Запрещаем перемещение компаса и шерсти */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        Player player = (Player) event.getWhoClicked();

        // ✅ Запрещаем забирать и выбрасывать важные предметы
        if (clickedItem.getType() == Material.COMPASS || clickedItem.getType() == Material.WHITE_WOOL) {
            event.setCancelled(true);
        }
    }

    /** ✅ Запрещаем перетаскивание компаса и шерсти */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        for (ItemStack item : event.getNewItems().values()) {
            if (lockedItems.contains(item.getType())) {
                event.setCancelled(true);
            }
        }
    }

    /** ✅ Запрещаем выбрасывание компаса и шерсти */
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        if (droppedItem.getType() == Material.COMPASS || droppedItem.getType() == Material.WHITE_WOOL) {
            event.setCancelled(true);
        }
    }

    /** ✅ Запрещаем установку блоков из защищённых предметов */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item != null && lockedItems.contains(item.getType())) {
            event.setCancelled(true);
        }
    }
    /** ✅ Запрещаем выпадение предметов при смерти */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.getDrops().removeIf(this::isLockedItem);
    }

    /** ✅ Проверка — предмет под защитой или нет */
    private boolean isLockedItem(ItemStack item) {
        return item != null && lockedItems.contains(item.getType());
    }
}

