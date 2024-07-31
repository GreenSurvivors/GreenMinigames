package au.com.mineauz.minigames.mechanics;

import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.MinigameModule;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class CustomMechanic extends GameMechanicBase {

    @Override
    public @NotNull String getMechanicName() {
        return "custom";
    }

    @Override
    public @NotNull EnumSet<@NotNull MinigameType> validTypes() {
        return EnumSet.of(MinigameType.MULTIPLAYER, MinigameType.SINGLEPLAYER);
    }

    @Override
    public boolean checkCanStart(@NotNull Minigame minigame, @Nullable MinigamePlayer caller) {
        return true;
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
    public void playerAutoBalance(@NotNull PlayerDeathEvent event) {
        MinigamePlayer mgPlayer = pdata.getMinigamePlayer(event.getEntity());
        if (mgPlayer.isInMinigame() && mgPlayer.getMinigame().isTeamGame()) {
            Minigame mgm = mgPlayer.getMinigame();

            if (mgm.getMechanicName().equals("custom")) {
                autoBalanceOnDeath(mgPlayer, mgm);
            }
        }
    }

    @Override
    public @Nullable MinigameModule displaySettings(@NotNull Minigame minigame) {
        return null;
    }
}
