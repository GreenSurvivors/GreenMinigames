package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.minigame.modules.team.Team;
import au.com.mineauz.minigames.minigame.modules.team.TeamsModule;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.script.ScriptObject;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AAction implements IAction, Keyed {
    protected final @NotNull Key key;

    protected AAction(final @NotNull Key key) {
        this.key = key;
    }

    /**
     * Logs Debug re these 2 items.
     *
     * @param mgPlayer     the player
     * @param scriptObject a script object
     */
    public void debug(final @Nullable MinigamePlayer mgPlayer, final @NotNull ScriptObject scriptObject) {
        if (Minigames.getPlugin().isDebugging()) {
            MessageManager.debugMessage("Debug: Execute on Obj:"
                    + scriptObject.getAsString() + " as Action: " + this + " Player: "
                    + ((mgPlayer == null) ? "no player" : mgPlayer.getAsString()));
        }
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    /**
     * Set winners losers.
     *
     * @param winner the winner
     */
    void winMinigame(final @NotNull MinigamePlayer winner) {
        if (winner.getMinigame().getType() != MinigameType.SINGLEPLAYER) {
            final @NotNull List<@NotNull MinigamePlayer> winners;
            final @NotNull List<@NotNull MinigamePlayer> losers;
            if (winner.getMinigame().isTeamGame()) {
                winners = new ArrayList<>(winner.getTeam().getPlayers());
                losers = new ArrayList<>(winner.getMinigame().getPlayers().size()
                        - winner.getTeam().getPlayers().size());
                for (final @NotNull Team team
                        : TeamsModule.getMinigameModule(winner.getMinigame()).getTeams()) {
                    if (team != winner.getTeam()) {
                        losers.addAll(team.getPlayers());
                    }
                }
            } else {
                winners = new ArrayList<>(1);
                losers = new ArrayList<>(winner.getMinigame().getPlayers().size());
                winners.add(winner);
                losers.addAll(winner.getMinigame().getPlayers());
                losers.remove(winner);
            }
            Minigames.getPlugin().getPlayerManager().endMinigame(winner.getMinigame(), winners, losers);
        } else {
            Minigames.getPlugin().getPlayerManager().winMinigame(winner);
        }
    }
}
