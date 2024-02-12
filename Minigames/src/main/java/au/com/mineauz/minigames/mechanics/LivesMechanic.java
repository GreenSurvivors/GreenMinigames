package au.com.mineauz.minigames.mechanics;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.events.StartMinigameEvent;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.managers.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
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

    @Override
    public String getMechanic() {
        return "lives";
    }

    @Override
    public EnumSet<MinigameType> validTypes() {
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
            MinigameMessageManager.sendMgMessage(caller, MinigameMessageType.ERROR, MinigameLangKey.MINIGAME_LIVES_ERROR_NOLIVES);
        }
        return false;
    }

    @Override
    public MinigameModule displaySettings(Minigame minigame) {
        return null;
    }

    @Override
    public void startMinigame(Minigame minigame, MinigamePlayer caller) {
    }

    @Override
    public void stopMinigame(Minigame minigame) {
    }

    @Override
    public void onJoinMinigame(Minigame minigame, MinigamePlayer player) {
    }

    @Override
    public void quitMinigame(Minigame minigame, MinigamePlayer player,
                             boolean forced) {
    }

    @Override
    public void endMinigame(Minigame minigame, List<MinigamePlayer> winners,
                            List<MinigamePlayer> losers) {
    }

    @EventHandler
    private void minigameStart(StartMinigameEvent event) {
        if (event.getMinigame().getMechanicName().equals(getMechanic())) {
            final List<MinigamePlayer> players = event.getPlayers();
            final Minigame minigame = event.getMinigame();
            for (MinigamePlayer player : players) {
                if (!Float.isFinite(minigame.getLives())) {
                    player.setScore(Integer.MAX_VALUE);
                    minigame.setScore(player, Integer.MAX_VALUE);
                } else {
                    int lives = Float.floatToIntBits(minigame.getLives());
                    player.setScore(lives);
                    minigame.setScore(player, lives);
                }
            }
        }
    }

    @EventHandler
    private void playerDeath(PlayerDeathEvent event) {
        MinigamePlayer mgPlayer = Minigames.getPlugin().getPlayerManager().getMinigamePlayer(event.getEntity());
        if (mgPlayer.isInMinigame() && mgPlayer.getMinigame().getMechanicName().equals(getMechanic())) {
            mgPlayer.addScore(-1);
            mgPlayer.getMinigame().setScore(mgPlayer, mgPlayer.getScore());
        }
    }

}
