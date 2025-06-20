package org.lastwar_game.lastwargame;

import org.bukkit.plugin.java.JavaPlugin;
import org.lastwar_game.lastwargame.commands.EndGameCommand;
import org.lastwar_game.lastwargame.commands.StopCommand;
import org.lastwar_game.lastwargame.listeners.*;
import org.lastwar_game.lastwargame.listeners.GUI.*;
import org.lastwar_game.lastwargame.managers.GameManager;
import org.lastwar_game.lastwargame.managers.LobbyManager;

public class LastWarPlugin extends JavaPlugin {

    private static LastWarPlugin instance;
    private GameManager gameManager;
    private LobbyManager lobbyManager;

    @Override
    public void onEnable() {
        instance = this;
        this.gameManager = new GameManager();
        this.lobbyManager = new LobbyManager(this);
        getCommand("endgame").setExecutor(new EndGameCommand());
        getCommand("stop").setExecutor(new StopCommand());
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");


        getServer().getPluginManager().registerEvents(new GUIListener(), this); // Обработчик кликов в GUI
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this); // Обработчик входов игроков
//        getServer().getPluginManager().registerEvents(new CompassListener(), this); // Упрощенный UniversalCompassListener
        getServer().getPluginManager().registerEvents(new UniversalCompassListener(), this); // Обработчик кликов компасом
        getServer().getPluginManager().registerEvents(new ServerSelectionListener(this), this); // Обработчик выбора сервера
        getServer().getPluginManager().registerEvents(new GUIClickListener(), this); // Запрещаем забирать предметы из GUI
        getServer().getPluginManager().registerEvents(new PlayerWorldChangeListener(), this);
        getServer().getPluginManager().registerEvents(new TeamSelectionClickListener(), this);
        getServer().getPluginManager().registerEvents(new TeamSelectorListener(), this);
        getServer().getPluginManager().registerEvents(new ClassSelectorListener(), this);
        getServer().getPluginManager().registerEvents(new ListenerPaperRedConcrete(), this);
        getServer().getPluginManager().registerEvents(new PlayerMovementFreezeListener(), this);



        getLogger().info("[LastWar] Плагин запущен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("[LastWar] Плагин выключен!");
    }

    public static LastWarPlugin getInstance() {
        return instance;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public LobbyManager getLobbyManager() {
        return lobbyManager;
    }
}
