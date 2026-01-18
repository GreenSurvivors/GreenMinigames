package au.com.mineauz.minigames.minigame.modules;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.minigame.Minigame;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public enum MgModules implements ModuleFactory {
    CAPTURE_THE_FLAG("CTF", CTFModule::new),
    GAME_OVER("GameOver", GameOverModule::new),
    INFECTION("Infection", InfectionModule::new),
    JUGGERNAUT("Juggernaut", JuggernautModule::new),
    LOADOUT("Loadouts", LoadoutModule::new),
    LOBBY_SETTINGS("LobbySettings", LobbySettingsModule::new),
    RESOURCEPACK("ResourcePack", ResourcePackModule::new),
    REWARDS("Rewards", RewardsModule::new),
    TEAMS("Teams", TeamsModule::new),
    TREASURE_HUNT("TreasureHunt", TreasureHuntModule::new),
    WEATHER_TIME("WeatherTime", WeatherTimeModule::new);

    private final @NotNull BiFunction<@NotNull Minigame, @NotNull Key, @NotNull MinigameModule> minigameModuleInit;
    private final @NotNull Key key;

    MgModules(final @NotNull String key, final @NotNull BiFunction<@NotNull Minigame, @NotNull Key, @NotNull MinigameModule> minigameModuleInit) {
        this.minigameModuleInit = minigameModuleInit;
        this.key = new NamespacedKey(Minigames.getPlugin(), key);
    }

    public @NotNull MinigameModule makeNewModule(final @NotNull Minigame minigame) {
        return minigameModuleInit.apply(minigame, key);
    }

    @Override
    public @NotNull String toString() {
        return key.value();
    }

    public @NotNull Key getKey() {
        return key;
    }
}
