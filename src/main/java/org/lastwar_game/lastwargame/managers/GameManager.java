package org.lastwar_game.lastwargame.managers;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.lastwar_game.lastwargame.GUI.ClassSelectionGUI;
import org.lastwar_game.lastwargame.LastWarPlugin;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import java.util.*;

import static org.lastwar_game.lastwargame.managers.LobbyItems.*;

public class GameManager {
    private static GameManager instance; // статическая переменная instance
    private final Map<String, List<Player>> gameWorldPlayers = new HashMap<>();
    private final Map<UUID, String> playerTeams = new HashMap<>();
    private final Map<UUID, String> playerClasses = new HashMap<>();
    private final Map<String, UUID> takenClasses = new HashMap<>(); // Добавляем список занятых классов
    private final Map<String, BukkitRunnable> gameTimers = new HashMap<>(); // Таймеры для каждого мира
    private final List<String> classOptions = Arrays.asList("Warrior", "Archer", "Mage", "Tank", "Assassin", "Shieldbearer", "Berserker", "Paladin", "Alchemist", "Necromancer");
    private final Set<UUID> lockedTeams = new HashSet<>(); // игроки, которые уже не могут менять команду

    private final Map<UUID, Location> frozenPlayers = new HashMap<>();


    /** ✅ Проверяет, можно ли начать игру в мире **/
    public void checkGameStart(String worldName) {
        List<Player> players = getPlayersInWorld(worldName);
        Bukkit.broadcastMessage("DEBUG: Checking game start in " + worldName + " with " + players.size() + " players.");

        if (players.size() >= 2 && players.size() <= 10) {
            if (gameTimers.containsKey(worldName)) {
                stopGameCountdown(worldName); // ✅ Очищаем старый таймер перед запуском нового
            }
            startGameCountdown(worldName, players);
        }
    }

    /** ✅ Запускает таймер начала игры **/
    private void startGameCountdown(String worldName, List<Player> players) {
        Bukkit.broadcastMessage("§aThe game in " + worldName + " will start in 15 seconds...");

        for (Player player : players) {
            giveTeamSelectionItem(player);
        }

        BukkitRunnable timer = new BukkitRunnable() {
            int countdown = 15;

            @Override
            public void run() {
                List<Player> updatedPlayers = getPlayersInWorld(worldName);

                if (updatedPlayers.size() < 2) {
                    Bukkit.broadcastMessage("§cGame start canceled in " + worldName + ", not enough players!");
                    gameTimers.remove(worldName);
                    this.cancel();
                    return;
                }

                if (countdown <= 0) {
                    // ✅ Добавляем проверку и балансировку команд перед стартом выбора классов
                    finalizeTeams(worldName);

                    lockTeamSelection(updatedPlayers);
                    assignTeams(updatedPlayers);
                    startClassSelection(updatedPlayers, worldName);
                    gameTimers.remove(worldName);
                    this.cancel();
                } else {
                    Bukkit.broadcastMessage("§eGame in " + worldName + " starts in " + countdown + " seconds...");
                    countdown--;
                }
            }
        };

        timer.runTaskTimer(LastWarPlugin.getInstance(), 0L, 20L);
        gameTimers.put(worldName, timer);
    }

    /** ✅ Выдаёт предмет для выбора команды **/
    public void giveTeamSelectionItem(Player player) {
        ItemStack teamSelector = new ItemStack(Material.WHITE_WOOL);
        ItemMeta meta = teamSelector.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§eTeam Selection");
            teamSelector.setItemMeta(meta);
        }
        player.getInventory().setItem(4, teamSelector);
    }

    /** ✅ Проверяет, заблокирован ли выбор команды **/
    public boolean isTeamSelectionLocked(UUID playerId) {
        return lockedTeams.contains(playerId); // Проверяем, зафиксирована ли команда
    }
    public void updatePlayerTeam(Player player, String team) {
        playerTeams.put(player.getUniqueId(), team); // ✅ Записываем команду игрока

        // ✅ Устанавливаем цвет ника и даём соответствующую шерсть
        if (team.equals("RED")) {
            player.setDisplayName("§c" + player.getName());
            player.getInventory().setItem(4, createTeamItem(Material.RED_WOOL, "§cTeam Selection"));
        } else {
            player.setDisplayName("§9" + player.getName());
            player.getInventory().setItem(4, createTeamItem(Material.BLUE_WOOL, "§9Team Selection"));
        }

        player.sendMessage("§aYou are now in the " + (team.equals("RED") ? "§cRed" : "§9Blue") + " Team!");
    }

    /** ✅ Метод для создания предмета (шерсти) с названием */
    private ItemStack createTeamItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }


    /** ✅ Блокирует выбор команды и зачаровывает предмет **/
    private void lockTeamSelection(List<Player> players) {
        for (Player player : players) {
            lockedTeams.add(player.getUniqueId());
            String team = playerTeams.get(player.getUniqueId());

            ItemStack teamItem = new ItemStack(team.equals("RED") ? Material.RED_WOOL : Material.BLUE_WOOL);
            ItemMeta meta = teamItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§eYour Team: " + (team.equals("RED") ? "§cRed" : "§9Blue"));
                meta.addEnchant(org.bukkit.enchantments.Enchantment.LUCK, 1, true);
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
                teamItem.setItemMeta(meta);
            }
            player.getInventory().setItem(4, teamItem);
        }
    }


    /** ✅ Обрабатывает выбор команды игроком **/
    public void selectTeam(Player player, String team) {
        if (lockedTeams.contains(player.getUniqueId())) {
            player.sendMessage("§cYou can no longer change teams!");
            return;
        }

        long redCount = playerTeams.values().stream().filter(t -> t.equals("RED")).count();
        long blueCount = playerTeams.values().stream().filter(t -> t.equals("BLUE")).count();
        long totalPlayers = redCount + blueCount;

        if (team.equals("RED") && redCount < totalPlayers / 2) {
            playerTeams.put(player.getUniqueId(), "RED");
            setPlayerTeam(player, "RED");
        } else if (team.equals("BLUE") && blueCount < totalPlayers / 2) {
            playerTeams.put(player.getUniqueId(), "BLUE");
            setPlayerTeam(player, "BLUE");
        } else {
            playerTeams.put(player.getUniqueId(), redCount <= blueCount ? "RED" : "BLUE");
            setPlayerTeam(player, playerTeams.get(player.getUniqueId()));
        }

        player.sendMessage("§aYou joined " + (team.equals("RED") ? "§cRED" : "§9BLUE") + " §ateam!");
    }



    /** ✅ Останавливает таймер, если игроков стало меньше 2 **/
    private void stopGameCountdown(String worldName) {
        if (gameTimers.containsKey(worldName)) {
            gameTimers.get(worldName).cancel();
            gameTimers.remove(worldName);
            Bukkit.broadcastMessage("§cGame start canceled in " + worldName + ", not enough players!");
        }
    }

    /** ✅ Выдаёт команды (RED / BLUE) **/
    private void assignTeams(List<Player> players) {
        List<Player> redTeam = new ArrayList<>();
        List<Player> blueTeam = new ArrayList<>();

        for (Player player : players) {
            String team = playerTeams.get(player.getUniqueId());
            if (team != null) {
                if (team.equals("RED")) redTeam.add(player);
                else blueTeam.add(player);
            }
        }

        for (Player player : players) {
            if (!playerTeams.containsKey(player.getUniqueId())) {
                if (redTeam.size() < blueTeam.size()) {
                    playerTeams.put(player.getUniqueId(), "RED");
                    redTeam.add(player);
                } else if (blueTeam.size() < redTeam.size()) {
                    playerTeams.put(player.getUniqueId(), "BLUE");
                    blueTeam.add(player);
                } else {
                    playerTeams.put(player.getUniqueId(), Math.random() > 0.5 ? "RED" : "BLUE");
                }
            }
            setPlayerTeam(player, playerTeams.get(player.getUniqueId()));
        }
    }

    /** ✅ Устанавливает цвет ника в зависимости от команды **/
    private void setPlayerTeam(Player player, String team) {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team red = board.getTeam("RED");
        Team blue = board.getTeam("BLUE");

        if (red == null) {
            red = board.registerNewTeam("RED");
            red.setPrefix(ChatColor.RED.toString());
            red.setColor(ChatColor.RED);
        }
        if (blue == null) {
            blue = board.registerNewTeam("BLUE");
            blue.setPrefix(ChatColor.BLUE.toString());
            blue.setColor(ChatColor.BLUE);
        }

        red.removeEntry(player.getName());
        blue.removeEntry(player.getName());

//        if (team.equals("RED")) {
//            red.addEntry(player.getName());
//            player.getInventory().setItem(5, new ItemStack(Material.RED_WOOL));
//        } else {
//            blue.addEntry(player.getName());
//            player.getInventory().setItem(5, new ItemStack(Material.BLUE_WOOL));
//        }

        player.setDisplayName((team.equals("RED") ? ChatColor.RED : ChatColor.BLUE) + player.getName() + ChatColor.RESET);
        player.setPlayerListName(player.getDisplayName());
    }

    /** ✅ Получает команду игрока **/
    public String getPlayerTeam(Player player) {
        return playerTeams.get(player.getUniqueId());
    }



    /** ✅ Запускает процесс выбора классов **/
    private void startClassSelection(List<Player> players, String worldName) {
        List<Player> queue = new ArrayList<>(players);
        Collections.shuffle(queue);
        processClassSelection(queue, worldName);
    }

    private void processClassSelection(List<Player> queue, String worldName) {
        if (queue.isEmpty()) {
            Bukkit.broadcastMessage("§aAll players have selected their classes!");
            startGame();
            return;
        }

        Player player = queue.remove(0);
        Bukkit.broadcastMessage("§e" + player.getName() + " is selecting a class...");
        openClassSelectionGUI(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!playerClasses.containsKey(player.getUniqueId())) {
                    assignRandomClass(player);
                }
                // ✅ Закрываем GUI после выбора или рандомного назначения
                if (player.isOnline() && player.getOpenInventory().getTitle().equals("Class Selection")) {
                    player.closeInventory();
                }
                processClassSelection(queue, worldName);
            }
        }.runTaskLater(LastWarPlugin.getInstance(), 200L); // ✅ 10 секунд на выбор
    }


    /** ✅ Открывает GUI выбора класса **/
    private void openClassSelectionGUI(Player player) {
        ClassSelectionGUI.open(player);
    }

    /** ✅ Назначает случайный класс **/
    private void assignRandomClass(Player player) {
        for (String className : classOptions) {
            if (!playerClasses.containsValue(className)) {
                playerClasses.put(player.getUniqueId(), className);
                Bukkit.broadcastMessage("§e" + player.getName() + " was assigned a random class: " + className);
                break;
            }
        }
    }

    /** ✅ Проверяет, свободен ли класс **/
    public boolean isClassAvailable(String className) {
        return !playerClasses.containsValue(className);
    }

    /** ✅ Проверяет, занят ли класс **/
    public boolean isClassTaken(String className) {
        return takenClasses.containsKey(className);
    }

    /** ✅ Назначает класс игроку **/
    public boolean assignClassToPlayer(Player player, String className) {
        if (isClassTaken(className)) {
            return false;
        }

        playerClasses.put(player.getUniqueId(), className);
        takenClasses.put(className, player.getUniqueId());
        return true;
    }

    /** ✅ Получает класс игрока **/
    public String getPlayerClass(Player player) {
        return playerClasses.get(player.getUniqueId());
    }

    private void startClassSelection(List<Player> players) {
        List<Player> queue = new ArrayList<>(players);
        Collections.shuffle(queue);
        processClassSelection(queue);
    }

    private void processClassSelection(List<Player> queue) {
        if (queue.isEmpty()) {
            Bukkit.broadcastMessage("§aAll players have selected their classes!");
            startGame();
            return;
        }

        Player player = queue.remove(0);
        openClassSelectionGUI(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!playerClasses.containsKey(player.getUniqueId())) {
                    assignRandomClass(player);
                }
                processClassSelection(queue);
            }
        }.runTaskLater(LastWarPlugin.getInstance(), 200L); // 10 секунд
    }

    public void assignPlayerToTeam(UUID playerId, String team) {
        if (playerTeams.containsKey(playerId)) return;

        long redCount = playerTeams.values().stream().filter(t -> t.equals("Red Team")).count();
        long blueCount = playerTeams.values().stream().filter(t -> t.equals("Blue Team")).count();

        if (team.equals("Red Team") && redCount < gameWorldPlayers.get(playerId).size() / 2) {
            playerTeams.put(playerId, "Red Team");
        } else if (team.equals("Blue Team") && blueCount < gameWorldPlayers.get(playerId).size() / 2) {
            playerTeams.put(playerId, "Blue Team");
        } else {
            playerTeams.put(playerId, redCount <= blueCount ? "Red Team" : "Blue Team");
        }
    }


    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }
    /** ✅ Финальная балансировка команд после 15 секунд ожидания */
    public void finalizeTeams(String worldName) {
        List<Player> players = getPlayersInWorld(worldName);
        List<Player> unassignedPlayers = new ArrayList<>();

        int redCount = 0;
        int blueCount = 0;

        // Подсчет игроков в командах
        for (Player player : players) {
            String team = playerTeams.get(player.getUniqueId());
            if (team == null) {
                unassignedPlayers.add(player); // iгрок без команды
            } else if (team.equals("RED")) {
                redCount++;
            } else if (team.equals("BLUE")) {
                blueCount++;
            }
        }

        // ✅ Добавляем игроков без команды в команду с меньшим количеством игроков
        for (Player player : unassignedPlayers) {
            if (redCount < blueCount) {
                assignPlayerToTeam(player, "RED");
                redCount++;
            } else if (blueCount < redCount) {
                assignPlayerToTeam(player, "BLUE");
                blueCount++;
            } else {
                String randomTeam = Math.random() > 0.5 ? "RED" : "BLUE";
                assignPlayerToTeam(player, randomTeam);
                if (randomTeam.equals("RED")) redCount++;
                else blueCount++;
            }
        }

        // ✅ Проверяем разницу между командами
        int diff = Math.abs(redCount - blueCount);
        if (diff >= 2) {
            int minPlayersPerTeam = (int) Math.floor((redCount + blueCount) / 2.0); // Округляем вниз
            balanceTeams(redCount, blueCount, minPlayersPerTeam);
        }

        // ✅ Сообщаем всем игрокам итоговое распределение команд
        for (Player player : players) {
            player.sendMessage(ChatColor.YELLOW + "Final teams:");
            player.sendMessage(ChatColor.RED + "Red Team: " + redCount);
            player.sendMessage(ChatColor.BLUE + "Blue Team: " + blueCount);
        }
    }

    /** ✅ Балансировка команд, если разница больше 1 */
    private void balanceTeams(int redCount, int blueCount, int minPlayersPerTeam) {
        while (redCount > minPlayersPerTeam + 1 && blueCount < minPlayersPerTeam) {
            Player playerToMove = getPlayerFromTeam("RED");
            if (playerToMove != null) {
                assignPlayerToTeam(playerToMove, "BLUE");
                redCount--;
                blueCount++;
            }
        }

        while (blueCount > minPlayersPerTeam + 1 && redCount < minPlayersPerTeam) {
            Player playerToMove = getPlayerFromTeam("BLUE");
            if (playerToMove != null) {
                assignPlayerToTeam(playerToMove, "RED");
                blueCount--;
                redCount++;
            }
        }
    }

    /** ✅ Назначает игрока в команду */
    private void assignPlayerToTeam(Player player, String team) {
        playerTeams.put(player.getUniqueId(), team);
        player.sendMessage(ChatColor.DARK_GRAY + "You have been assigned to " + (team.equals("RED") ? ChatColor.RED + "Red Team!" : ChatColor.BLUE + "Blue Team!"));
    }

    /** ✅ Получает случайного игрока из команды */
    private Player getPlayerFromTeam(String team) {
        for (UUID playerId : playerTeams.keySet()) {
            if (playerTeams.get(playerId).equals(team)) {
                return Bukkit.getPlayer(playerId);
            }
        }
        return null;
    }

    /** ✅ Получает список игроков в мире */
    private List<Player> getPlayersInWorld(String worldName) {
        return new ArrayList<>(Bukkit.getWorld(worldName).getPlayers());
    }

    /** ✅ !!ОЧиСТКА ВСЕГО СОСТОЯНиЯ иГРЫ!! */
    public void resetWorldState(String worldName) {
        // Остановка таймера
        if (gameTimers.containsKey(worldName)) {
            gameTimers.get(worldName).cancel();
            gameTimers.remove(worldName);
        }

        // Удаление игроков, связанных с этим миром
        List<Player> players = getPlayersInWorld(worldName);
        for (Player player : players) {
            UUID id = player.getUniqueId();
            playerTeams.remove(id);
            playerClasses.remove(id);
            lockedTeams.remove(id);
            takenClasses.values().remove(id); // удаляем из занятого класса
        }
    }
    public void removePlayerData(UUID uuid) {
        playerTeams.remove(uuid);
        playerClasses.remove(uuid);
        lockedTeams.remove(uuid);
        takenClasses.values().remove(uuid);
    }

    public static Location getRandomLocationAround(Location center, int radius) {
        double angle = Math.random() * 2 * Math.PI; // Формула нахождения радиуса
        double distance = Math.random() * radius;

        double x = center.getX() + distance * Math.cos(angle); // Формула нахождения блоков по радиусу
        double z = center.getZ() + distance * Math.sin(angle); // Формула нахождения блоков по радиусу
        double y = center.getWorld().getHighestBlockYAt((int) x, (int) z) + 1; // +1 чтобы над землёй

        return new Location(center.getWorld(), x, y, z);
    }

    public boolean isPlayerFrozen(UUID id) {
        return frozenPlayers.containsKey(id);
    }

    public Location getFrozenLocation(UUID id) {
        return frozenPlayers.get(id);
    }

    public void freezePlayer(Player player, Location loc) {
        frozenPlayers.put(player.getUniqueId(), loc);
    }

    public void unfreezeAllPlayers() {
        frozenPlayers.clear();
    }

    /** ✅ Начинает игру **/
    private void startGame() {
        Bukkit.broadcastMessage("§aThe game starts now!");

        for (World world : Bukkit.getWorlds()) {
            List<Player> players = world.getPlayers();
            if (players.isEmpty()) continue;

            for (Player player : players) {
                String team = playerTeams.get(player.getUniqueId());
                if (team == null) continue;

                Location spawn;
                if (team.equals("RED")) {
                    spawn = getRandomLocationAround(new Location(world, -141.5, 35, 473.5), 3);
                } else if (team.equals("BLUE")) {
                    spawn = getRandomLocationAround(new Location(world, -141.5, 35, 115.5), 3);
                } else {
                    continue;
                }

                player.teleport(spawn);
                frozenPlayers.put(player.getUniqueId(), spawn); // Замораживаем игроков на 5 сек
                Bukkit.broadcastMessage("§aAll players are now freeze to move!");
            }
        }

        // ✅ Через 5 секунд снимаем заморозку
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage("§aAll players are now free to move!");
                unfreezeAllPlayers();
            }
        }.runTaskLater(LastWarPlugin.getInstance(), 100L); // 5 секунд = 100 тико
    }

    public void endGame(World world) {
        // ✅ Выполняем команду /endgame от имени сервера
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "endgame");

        // ✅ Выполняем команду /stop через 10.5 сек (210 тиков)
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
            }
        }.runTaskLater(LastWarPlugin.getInstance(), 210L);
    }
}
