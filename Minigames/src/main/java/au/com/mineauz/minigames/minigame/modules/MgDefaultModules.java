package au.com.mineauz.minigames.minigame.modules;

import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.loadout.LoadoutModule;
import au.com.mineauz.minigames.minigame.modules.team.TeamsModule;
import au.com.mineauz.minigames.objects.MinigamesKey;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public enum MgDefaultModules implements ModuleFactory {
    GAME_OVER("GameOver", GameOverModule::new),
    LOADOUT("Loadouts", LoadoutModule::new),
    LOBBY_SETTINGS("LobbySettings", LobbySettingsModule::new),
    RESOURCEPACK("ResourcePack", ResourcePackModule::new),
    REWARDS("Rewards", RewardsModule::new),
    TEAMS("Teams", TeamsModule::new),
    WEATHER_TIME("WeatherTime", WeatherTimeModule::new);

    private final @NotNull BiFunction<@NotNull Minigame, @NotNull Key, @NotNull AMinigameModule> minigameModuleInit;
    private final @NotNull Key key;

    MgDefaultModules(final @NotNull String key, final @NotNull BiFunction<@NotNull Minigame, @NotNull Key, @NotNull AMinigameModule> minigameModuleInit) {
        this.minigameModuleInit = minigameModuleInit;
        this.key = MinigamesKey.minigames(key);
    }

    public @NotNull AMinigameModule makeNewModule(final @NotNull Minigame minigame) {
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
