package au.com.mineauz.minigames.mechanics;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.events.StartMinigameEvent;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.MinigameModule;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class LivesMechanic extends GameMechanicBase {

    protected LivesMechanic() {
    }

    @Override
    public @NotNull String getMechanicName() {
        return "lives";
    }

    @Override
    public @NotNull EnumSet<@NotNull MinigameType> validTypes() {
        return EnumSet.of(MinigameType.MULTIPLAYER);
    }

    @Override
    public boolean checkCanStart(@NotNull Minigame minigame, @Nullable MinigamePlayer caller) {
        if (minigame.getLives() > 0) {
            return true;
        }

        if (caller == null) {
            Minigames.getCmpnntLogger().warn("The Minigame \"" + minigame.getName() + "\" must have more than 0 lives to use this type");
        } else {
            MinigameMessageManager.sendMgMessage(caller, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_LIVES_ERROR_NOLIVES);
        }
        return false;
    }

    @Override
    public @Nullable MinigameModule displaySettings(@NotNull Minigame minigame) {
        return null;
    }

    @Override
    public void startMinigame(@NotNull Minigame minigame, @Nullable MinigamePlayer caller) {
    }

    @Override
    public void stopMinigame(@NotNull Minigame minigame) {
    }

    @Override
    public void onJoinMinigame(@NotNull Minigame minigame, @NotNull MinigamePlayer player) {
    }

    @Override
    public void quitMinigame(@NotNull Minigame minigame, @NotNull MinigamePlayer player,
                             boolean forced) {
    }

    @Override
    public void endMinigame(@NotNull Minigame minigame, @NotNull List<@NotNull MinigamePlayer> winners,
                            @NotNull List<@NotNull MinigamePlayer> losers) {
    }

    @EventHandler
    private void minigameStart(@NotNull StartMinigameEvent event) {
        if (event.getMinigame().getMechanicName().equals(getMechanicName())) {
            final List<MinigamePlayer> players = event.getPlayers();
            final Minigame minigame = event.getMinigame();
            for (MinigamePlayer player : players) {
                if (Math.abs(minigame.getLives()) < Integer.MAX_VALUE) {
                    int lives = minigame.getLives();
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
    private void playerDeath(@NotNull PlayerDeathEvent event) {
        MinigamePlayer mgPlayer = Minigames.getPlugin().getPlayerManager().getMinigamePlayer(event.getEntity());
        if (mgPlayer.isInMinigame() && mgPlayer.getMinigame().getMechanicName().equals(getMechanicName())) {
            mgPlayer.addScore(-1);
            mgPlayer.getMinigame().setScore(mgPlayer, mgPlayer.getScore());
        }
    }
}
