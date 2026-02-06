package au.com.mineauz.minigames.mechanics;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemPage;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.key.Key;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.EnumSet;
import java.util.List;

public class CustomMechanic extends AGameMechanic {

    public CustomMechanic(@NotNull Minigames plugin, @NotNull Key key, @NotNull Minigame minigame) {
        super(plugin, key, minigame);
    }

    @Override
    public void save(@NotNull CommentedConfigurationNode config) throws SerializationException {

    }

    @Override
    public void load(@NotNull CommentedConfigurationNode config) throws ConfigurateException {

    }

    @Override
    public boolean useSeparateConfig() {
        return false;
    }

    @Override
    public @NotNull EnumSet<@NotNull MinigameType> validTypes() {
        return EnumSet.of(MinigameType.MULTIPLAYER, MinigameType.SINGLEPLAYER);
    }

    @Override
    public boolean checkCanStart(@Nullable MinigamePlayer caller) {
        return true;
    }

    @Override
    public @Nullable MenuItemPage displayMechanicSettings(final @NotNull Menu previous) {
        return null;
    }

    @Override
    public void startMinigame(@Nullable MinigamePlayer caller) {
    }

    @Override
    public void stopMinigame() {
    }

    @Override
    public void onJoinMinigame(@NotNull MinigamePlayer player) {
    }

    @Override
    public void quitMinigame(@NotNull MinigamePlayer player,
                             boolean forced) {
    }

    @Override
    public void endMinigame(@NotNull List<@NotNull MinigamePlayer> winners,
                            @NotNull List<@NotNull MinigamePlayer> losers) {
    }

    @EventHandler
    public void playerAutoBalance(@NotNull PlayerDeathEvent event) {
        final @NotNull MinigamePlayer mgPlayer = plugin.getPlayerManager().getMinigamePlayer(event.getEntity());
        if (minigame.equals(mgPlayer.getMinigame()) && minigame.isTeamGame()) {
            autoBalanceOnDeath(mgPlayer, minigame);
        }
    }
}
