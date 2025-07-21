package org.lastwar_game.lastwargame.managers;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataType;
import org.example.gamelogic.lastwargamelogic.LastWarGameLogic;
import org.example.gamelogic.lastwargamelogic.deathsystem.DeathSpectatorListener;
import org.example.gamelogic.lastwargamelogic.flag.FlagSpawner;
import org.example.gamelogic.lastwargamelogic.privat.CoreSpawner;
import org.bukkit.util.Vector;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;




import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.lastwar_game.lastwargame.GUI.ClassSelectionGUI;
import org.lastwar_game.lastwargame.GUI.ServerSelectionGUI;
import org.lastwar_game.lastwargame.LastWarPlugin;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;


import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import java.util.*;
import java.util.stream.Collectors;

import static org.lastwar_game.lastwargame.managers.LobbyItems.*;

public class GameManager {
    private JavaPlugin plugin;

    private static GameManager instance; // статическая переменная instance
    private final Map<String, List<Player>> gameWorldPlayers = new HashMap<>();
    private final Map<UUID, String> playerTeams = new HashMap<>();
    private final Map<UUID, String> playerClasses = new HashMap<>();
    private final Map<String, UUID> takenClasses = new HashMap<>(); // Добавляем список занятых классов
    private final Map<String, BukkitRunnable> gameTimers = new HashMap<>(); // Таймеры для каждого мира
    private final List<String> classOptions = Arrays.asList("LadyNagant", "Archer", "Tank", "Saske");
    private final Set<UUID> lockedTeams = new HashSet<>(); // игроки, которые уже не могут менять команду
    private final Map<String, BossBar> bossBars = new HashMap<>();


    private final Map<UUID, Location> frozenPlayers = new HashMap<>();

    public void init(JavaPlugin plugin) {
        this.plugin = plugin;
    }


    /** ✅ Проверяет, можно ли начать игру в мире **/
    public void checkGameStart(String worldName) {
        List<Player> players = getPlayersInWorld(worldName);

        if (players.size() >= 2 && players.size() <= 10) {
            if (gameTimers.containsKey(worldName)) {
                stopGameCountdown(worldName); // ✅ Очищаем старый таймер перед запуском нового
            }
            startGameCountdown(worldName, players);
        }
    }

    /** ✅ Запускает таймер начала игры **/
    private void startGameCountdown(String worldName, List<Player> players) {

        for (Player player : players) {
            giveTeamSelectionItem(player);
        }

        BukkitRunnable timer = new BukkitRunnable() {
            int countdown = 15;

            @Override
            public void run() {
                List<Player> updatedPlayers = getPlayersInWorld(worldName);

                if (updatedPlayers.size() < 2) {
                    gameTimers.remove(worldName);
                    this.cancel();
                    return;
                }

                if (countdown <= 0) {
                    //Что происходит когда игра начинается
                    finishQueue(worldName,updatedPlayers);

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
    public void finishQueue(String worldName, List<Player> updatedPlayers) {
        finalizeTeams(worldName); // ✅ балансировка команд

        // ✅ Обновляем значение scoreboard для isClassSelectionStarted = 1
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective(worldName);
        if (objective != null) {
            //показать скорборду что сейчас идет выбор классов
            objective.getScore("isClassSelectionStarted").setScore(1);
        } else {
            Bukkit.getLogger().warning("[LastWar] Objective for world " + worldName + " not found when starting game.");
        }

        lockTeamSelection(updatedPlayers);
        assignTeams(updatedPlayers);
        startClassSelection(updatedPlayers, worldName);
        gameTimers.remove(worldName);
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
        for (Player player : players) {
            for (String tag : player.getScoreboardTags()) {
                player.removeScoreboardTag(tag);
            }
        }
        List<Player> queue = new ArrayList<>(players);
        Collections.shuffle(queue);
        processClassSelection(queue, worldName);
    }

    private void processClassSelection(List<Player> queue, String worldName) {
        if (queue.isEmpty()) {
            Bukkit.broadcastMessage("§aAll players have selected their classes!");
            startGame(worldName);
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
                player.addScoreboardTag(className);
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
        player.addScoreboardTag(className);
        Bukkit.broadcastMessage("§e" + player.getName() + " took " + className);
        takenClasses.put(className, player.getUniqueId());
        return true;
    }

    /** ✅ Получает класс игрока **/
    public String getPlayerClass(Player player) {
        return playerClasses.get(player.getUniqueId());
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

        player.sendMessage(ChatColor.GREEN + "You have been assigned to " + (team.equals("RED") ? ChatColor.RED + "Red Team!" : ChatColor.BLUE + "Blue Team!"));
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
    private void startGame(String worldName) {


        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Bukkit.getLogger().warning("World not found: " + worldName);
            return;
        }
        replaceWoolOnTeams(world);

        LastWarGameLogic.addActiveGameWorld(world);

        // Выдача предметов по классам
        for (Player player : Bukkit.getWorld(worldName).getPlayers()) {
            String playerClass = getPlayerClass(player);
            if (playerClass != null) {
                ClassItemManager.giveItemsForTaggedClass(player);
            }
        }



        Bukkit.broadcastMessage("§aThe game starts now!");
        // ✅ Обновляем значение scoreboard для isClassSelectionStarted = 0 && isGameStarted = 1
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective(worldName);
        // ✅ Запускаем проверку окончания игры
        new GameEndCheckerTask(worldName, LastWarPlugin.getInstance())
                .runTaskTimer(LastWarPlugin.getInstance(), 0L, 100L); // каждые 5 секунд (100 тиков)

        // ✅ Сбрасываем DeathCount у всех игроков в этом мире
        Objective deathObj = scoreboard.getObjective("DeathCount");
        if (deathObj != null) {
            for (Player player : Bukkit.getWorld(worldName).getPlayers()) {
                deathObj.getScore(player.getName()).setScore(0);
            }
        } else {
            Bukkit.getLogger().warning("[LastWar] Objective DeathCount not found when starting game.");
        }

        createAndStartBossBarr(worldName);

        //проверка гола если гол то фриз и тд
        new GoalMonitorTask(worldName).runTaskTimer(LastWarPlugin.getInstance(), 0L, 20L); // каждые 20 тиков (1 сек)

        if (objective != null) {
            objective.getScore("isClassSelectionStarted").setScore(0);
            objective.getScore("isGameStarted").setScore(1);
        } else {
            Bukkit.getLogger().warning("[LastWar] Objective for world " + worldName + " not found when starting game.");
        }





        freezeTime(worldName);
    }
    public static void scheduleTimeout(World world) {
        JavaPlugin plugin = LastWarPlugin.getInstance(); // или передай как параметр

        new BukkitRunnable() {
            @Override
            public void run() {
                Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                Objective obj = scoreboard.getObjective(world.getName());
                if (obj == null) return;

                int redScore = obj.getScore("RED").getScore();
                int blueScore = obj.getScore("BLUE").getScore();

                // если ничья, ничего не делаем — пусть GameEndCheckerTask активирует овертайм
                if (redScore == blueScore) {
                    plugin.getLogger().info("[LastWar] Timeout reached, but it's a tie — waiting for overtime logic.");
                    return;
                }

                GameManager.handleGameEndAfter600Seconds(world, plugin);
            }
        }.runTaskLater(plugin, 1200 * 20L); // 600 секунд = 600 * 20 тиков
    }

    public void replaceWoolOnTeams(World world){
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team red = scoreboard.getTeam("RED");
        Team blue = scoreboard.getTeam("BLUE");

        if (red == null || blue == null) {
            Bukkit.getLogger().warning("[LastWar] RED or BLUE team not found in scoreboard.");
            return;
        }

        for (Player player : world.getPlayers()) {
            boolean assigned = false;

            // Ищем RED или BLUE шерсть в инвентаре
            for (ItemStack item : player.getInventory()) {
                if (item == null) continue;

                if (item.getType() == Material.RED_WOOL) {
                    red.addEntry(player.getName());
                    assigned = true;
                    break;
                } else if (item.getType() == Material.BLUE_WOOL) {
                    blue.addEntry(player.getName());
                    assigned = true;
                    break;
                }
            }

            if (!assigned) {
                player.sendMessage("§eNo valid wool found in inventory. You were not added to any team.");
            }

            // Удаляем всю RED и BLUE шерсть из инвентаря
            player.getInventory().remove(Material.RED_WOOL);
            player.getInventory().remove(Material.BLUE_WOOL);
        }


    }
    public void freezeTime(String worldName) {
        //take world and players
        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        List<Player> players = world.getPlayers();
        if (players.isEmpty()) return;

        DeathSpectatorListener.respawnAllDeadPlayers(worldName, LastWarPlugin.getInstance());

        // ✅ Устанавливаем isFrozen = 1
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective(worldName);
        if (objective != null) {
            objective.getScore("isFrozen").setScore(1);
        }

        //reset cores and spawn flag
        Plugin plugin = Bukkit.getPluginManager().getPlugin("LastWarGameLogic");

        if (plugin instanceof LastWarGameLogic logicMain && plugin.isEnabled()) {


            CoreSpawner coreSpawner = logicMain.getCoreSpawner();

            if (world != null) {
                coreSpawner.spawnCoresForce(world);

                FlagSpawner spawner = new FlagSpawner(logicMain);
                spawner.spawnAtFixedLocation(world);
            }
        }

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
            frozenPlayers.put(player.getUniqueId(), spawn); // ❄ Замораживаем игрока
        }

        Bukkit.broadcastMessage("§aAll players in " + world.getName() + " are now frozen!");

        // ⏳ Обратный отсчёт
        new BukkitRunnable() {
            int countdown = 15;

            @Override
            public void run() {
                if (countdown <= 0) {
                    Bukkit.broadcastMessage("§aAll players in " + world.getName() + " can move again!");
                    unfreezeAllPlayers();

                    // ✅ Устанавливаем isFrozen = 0
                    if (objective != null) {
                        objective.getScore("isFrozen").setScore(0);
                    }

                    this.cancel();
                    return;
                }

                Bukkit.broadcastMessage("§7Unfreeze in §e" + countdown + "§7 seconds...");
                countdown--;
            }
        }.runTaskTimer(LastWarPlugin.getInstance(), 0L, 20L);
    }

    public void afterGoal(String worldName) {
        new BukkitRunnable() {
            @Override
            public void run() {
                freezeTime(worldName);
            }
        }.runTaskLater(LastWarPlugin.getInstance(), 200L);
    }
    public void playGoalEffect(Location center, Color color) {
        World world = center.getWorld();
        if (world == null) return;

        // Громкий звук
        world.playSound(center, Sound.ENTITY_ENDER_DRAGON_DEATH, 10f, 1f);

        // Фейерверки по кругу
        for (int i = 0; i < 8; i++) {
            Location fireworkLoc = center.clone().add(Math.cos(i * Math.PI / 4) * 1.2, 0.1, Math.sin(i * Math.PI / 4) * 1.2);
            Firework fw = world.spawn(fireworkLoc, Firework.class);
            FireworkMeta meta = fw.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder()
                    .withColor(color)
                    .withFade(Color.WHITE)
                    .with(FireworkEffect.Type.BURST)
                    .trail(true)
                    .flicker(true)
                    .build());
            meta.setPower(0); // моментальный взрыв
            fw.setFireworkMeta(meta);
            fw.setVelocity(new Vector(0, 0.1, 0));
            fw.detonate();
        }
    }






    public void endGame(World world) {

        LastWarGameLogic.removeActiveGameWorld(world);



        //get players
        List<Player> players = Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getWorld().equals(world))
                .collect(Collectors.toList());

        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team red = board.getTeam("RED");
        Team blue = board.getTeam("BLUE");

        for (Player player : world.getPlayers()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "setclass Clear " + player);
            if (red != null) red.removeEntry(player.getName());
            if (blue != null) blue.removeEntry(player.getName());
        }

        for (Player player : players) {
            for (String tag : player.getScoreboardTags()) {
                player.removeScoreboardTag(tag);
            }
        }


        BossBar bar = bossBars.remove(world.getName());
        if (bar != null) {
            bar.removeAll();
        }

        //tp player
        for (Player player : players) {
            removePlayerData(player.getUniqueId());
            player.teleport(Bukkit.getWorld("world").getSpawnLocation()); // Лобби
            LobbyItems.giveTo(player);
        }






        // ✅ Выполняем команду /endgame от имени сервера
        //Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "endgame");

        // ✅ Выполняем команду /stop через 10.5 сек (210 тиков)
       /*
       new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
            }
        }.runTaskLater(LastWarPlugin.getInstance(), 210L);
        */

        GameManager.getInstance().restartWorld(world.getName());
    }
    public void restartWorld(String worldName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Multiverse-Core");




        if (plugin == null || !plugin.isEnabled()) {
            Bukkit.getLogger().warning("[LastWar] Multiverse-Core is not available!");
            return;
        }

        GameManager.getInstance().markWorldRestarting(worldName);


        // Обнуляем значения RED и BLUE в objective этого мира
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective(worldName);
        if (objective != null) {
            objective.getScore("RED").setScore(0);
            objective.getScore("BLUE").setScore(0);
            objective.getScore("Timer").setScore(0);
            objective.getScore("isGameStarted").setScore(0);
            objective.getScore("isFrozen").setScore(0);
            objective.getScore("isGoalScored").setScore(0);
            objective.getScore("isClassSelectionStarted").setScore(0);
        }

        // 1. Выгружаем и удаляем текущий мир
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv delete -f " + worldName);

        // 2. Клонируем из шаблона
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv clone lastwarGame0 " + worldName);

        // 3. Загружаем мир
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv import " + worldName + " normal");


        // 4. Сброс данных игроков
        List<Player> players = Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getWorld().getName().equals(worldName))
                .map(p -> (Player) p) // это явно Player
                .toList();



        for (Player player : players) {
            removePlayerData(player.getUniqueId());
            player.teleport(Bukkit.getWorld("world").getSpawnLocation()); // в лобби
            LobbyItems.giveTo(player); // или твой метод выдачи предметов
        }

        // 5. Проверка на старт
        new BukkitRunnable() {
            @Override
            public void run() {
                checkGameStart(worldName);
            }
        }.runTaskLater(LastWarPlugin.getInstance(), 100L); // ждём 5 секунд на загрузку мира


        new BukkitRunnable() {
            @Override
            public void run() {
                // все нужные действия после рестарта
                GameManager.getInstance().unmarkWorldRestarting(worldName);

                // ⏺ Перерисовываем GUI у всех, кто смотрит на сервера
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getOpenInventory().getTitle().equals("Server Selection")) {
                        ServerSelectionGUI.open(player);
                    }
                }
            }
        }.runTaskLater(plugin, 60L); // например, через 3 секунды после окончания

    }
    // GameManager.java

    public static void handleGameEndAfter600Seconds(World world, JavaPlugin plugin) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective(world.getName());
        if (objective == null) return;


        // 3. Получение очков
        int redScore = objective.getScore("RED").getScore();
        int blueScore = objective.getScore("BLUE").getScore();

        if (objective == null || objective.getScore("isGameStarted").getScore() == 0) {
            Bukkit.getLogger().info("[LastWar] Overtime task cancelled because game has ended.");
            return;
        }

        if (redScore > blueScore) {
            Bukkit.broadcastMessage("§c§lRED wins the match!");
            scheduleFinalEnd(world, plugin, 15);
        } else if (blueScore > redScore) {
            Bukkit.broadcastMessage("§b§lBLUE wins the match!");
            scheduleFinalEnd(world, plugin, 15);
        } else {
            Bukkit.broadcastMessage("§6§lIt's a draw! Entering §eOVERTIME §6for 2 more minutes!");

            // ⏱ Overtime на 2 минуты (120 сек)
            new BukkitRunnable() {
                int seconds = 180;

                @Override
                public void run() {
                    Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                    Objective objective = scoreboard.getObjective(world.getName());


                    if (seconds <= 0) {
                        if (objective == null || objective.getScore("isGameStarted").getScore() == 0) {
                            this.cancel();
                            return;
                        }else{
                            Bukkit.broadcastMessage("§c§lOvertime is over!");
                            GameManager.handleGameEndAfter600Seconds(world, plugin);
                            this.cancel();
                            return;
                        }

                    }

                    seconds--;
                }
            }.runTaskTimer(plugin, 0L, 20L);

        }
    }

    public static void scheduleFinalEnd(World world, JavaPlugin plugin, int delaySeconds) {

        NamespacedKey flagKey = new NamespacedKey(plugin, "flag");

        // 1. Удаление всех флагов с карты
        Plugin logicMain = Bukkit.getPluginManager().getPlugin("LastWarGameLogic");
        CoreSpawner spawner = new CoreSpawner((JavaPlugin) logicMain);
        spawner.clearAllArmorStands(world);

        // 3. Звук смерти дракона для всех игроков в этом мире
        for (Player player : world.getPlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 10f, 1f);
        }



        // 2. Удаление флага из инвентарей игроков (слот 4)
        for (Player player : world.getPlayers()) {
            player.getInventory().clear(4); // Слот 4 = центр хотбара
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                GameManager.getInstance().endGame(world);
            }
        }.runTaskLater(plugin, delaySeconds * 20L); // 15 сек * 20 тиков
    }




    private final Set<String> restartingWorlds = new HashSet<>();

    public void markWorldRestarting(String worldName) {
        restartingWorlds.add(worldName);
    }

    public void unmarkWorldRestarting(String worldName) {
        restartingWorlds.remove(worldName);
    }

    public boolean isWorldRestarting(String worldName) {
        return restartingWorlds.contains(worldName);
    }
    public void createAndStartBossBarr(String worldName){
        BossBar bar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);
        bossBars.put(worldName, bar);

        for (Player player : Bukkit.getWorld(worldName).getPlayers()) {
            bar.addPlayer(player);
        }

        int[] time = {1200}; // 10 минут = 600 секунд

        new BukkitRunnable() {
            @Override
            public void run() {
                Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                Objective objective = scoreboard.getObjective(worldName);
                if (objective == null) {
                    bar.setTitle("§cError: Objective not found");
                    this.cancel();
                    return;
                }

                int redScore = objective.getScore("RED").getScore();
                int blueScore = objective.getScore("BLUE").getScore();

                // 🟥 Цвет бара в зависимости от лидера
                if (redScore > blueScore) {
                    bar.setColor(BarColor.RED);
                } else if (blueScore > redScore) {
                    bar.setColor(BarColor.BLUE);
                } else {
                    bar.setColor(BarColor.PURPLE);
                }

                // 🕒 Время и счёт
                String title = "§cRED: " + redScore + "  §7|  §9BLUE: " + blueScore + "    §eTime Left: " + time[0] + "s";
                bar.setTitle(title);

                // 📊 Прогресс (обратный отсчёт)
                double progress = 1.0 - (time[0] / 1200.0);
                bar.setProgress(progress);

                time[0]--;
                if (time[0] < 0) {
                    bar.setTitle("§eTime's up!");
                    World world = Bukkit.getWorld(worldName);
                    handleGameEndAfter600Seconds(world,plugin);
                    this.cancel();
                }
            }
        }.runTaskTimer(LastWarPlugin.getInstance(), 0L, 20L); // Каждую секунду
    }



}
