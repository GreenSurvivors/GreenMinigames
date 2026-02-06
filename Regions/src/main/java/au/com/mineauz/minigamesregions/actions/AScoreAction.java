package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AScoreAction extends AAction {

    protected AScoreAction(@NotNull Key key) {
        super(key);
    }

    void checkScore(@Nullable MinigamePlayer player) {
        if (player == null || !player.isInMinigame()) return;
        if (player.getScore() >= player.getMinigame().getMaxScorePerPlayer() || (player.getMinigame().isTeamGame() && player.getTeam().getScore() >= player.getMinigame().getMaxScore())) {
            winMinigame(player);
        }
    }
}
