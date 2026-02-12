package au.com.mineauz.minigames.mechanics;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.events.StartMinigameEvent;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
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

public class LivesMechanic extends AGameMechanic {

    public LivesMechanic(final @NotNull Minigames plugin, final @NotNull Key key, final @NotNull Minigame minigame) {
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
        return EnumSet.of(MinigameType.MULTIPLAYER);
    }

    @Override
    public boolean checkCanStart(@Nullable MinigamePlayer caller) {
        if (minigame.getLives() > 0) {
            return true;
        }

        if (caller == null) {
            plugin.getComponentLogger().warn("The Minigame \"" + minigame.getName() + "\" must have more than 0 lives to use this type");
        } else {
            MessageManager.sendMessage(caller, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_LIVES_ERROR_NOLIVES);
        }
        return false;
    }

    @Override
    public @Nullable MenuItemPage displayMechanicSettings(@NotNull Menu previous) {
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
    private void minigameStart(final @NotNull StartMinigameEvent event) {
        if (minigame.equals(event.getMinigame())) {
            final @NotNull List<@NotNull MinigamePlayer> players = event.getPlayers();
            for (MinigamePlayer player : players) {
                if (Math.abs(minigame.getLives()) < Integer.MAX_VALUE) {
                    final int lives = minigame.getLives();
                    player.setScore(lives);
                    minigame.setScore(player, lives);
                } else {
                    player.setScore(Integer.MAX_VALUE);
                    minigame.setScore(player, Integer.MAX_VALUE);
                }
            }
        }
    }

    @EventHandler
    private void playerDeath(final @NotNull PlayerDeathEvent event) {
        final @NotNull MinigamePlayer mgPlayer = plugin.getPlayerManager().getMinigamePlayer(event.getEntity());
        if (minigame.equals(mgPlayer.getMinigame())) {
            mgPlayer.addScore(-1);
            mgPlayer.getMinigame().setScore(mgPlayer, mgPlayer.getScore());
        }
    }
}
