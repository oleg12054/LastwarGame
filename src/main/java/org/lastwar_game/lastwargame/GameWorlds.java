package org.lastwar_game.lastwargame;

import java.util.Arrays;
import java.util.List;

public class GameWorlds {

    /**
     * Список всех игровых миров, используемых в плагине.
     *
     * ✅ Пример использования:
     *
     * import org.lastwar_game.lastwargame.GameWorlds;
     *
     * for (String worldName : GameWorlds.WORLD_NAMES) {
     *     // Твоя логика, например:
     *     World world = Bukkit.getWorld(worldName);
     * }
     *
     * Этот общий список позволяет избежать дублирования одних и тех же имён миров
     * в разных классах и упрощает поддержку проекта.
     */
    public static final List<String> WORLD_NAMES = Arrays.asList(
            "lastwarGame1", "lastwarGame2", "lastwarGame3", "lastwarGame4", "lastwarGame5", "lastwarGame6",
            "lastwarGame7", "lastwarGame8", "lastwarGame9", "lastwarGame10", "lastwarGame11", "lastwarGame12",
            "lastwarGame13", "lastwarGame14", "lastwarGame15", "lastwarGame16", "lastwarGame17", "lastwarGame18",
            "lastwarGame19", "lastwarGame20", "lastwarGame21", "lastwarGame22", "lastwarGame23", "lastwarGame24",
            "lastwarGame25", "lastwarGame26", "lastwarGame27"
    );
}
