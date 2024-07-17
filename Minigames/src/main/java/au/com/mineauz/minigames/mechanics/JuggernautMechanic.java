package au.com.mineauz.minigames.mechanics;

import au.com.mineauz.minigames.events.StartMinigameEvent;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.JuggernautModule;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class JuggernautMechanic extends GameMechanicBase {

    protected JuggernautMechanic() {
    }

    @Override
    public String getMechanicName() {
        return "juggernaut";
    }

    @Override
    public @NotNull EnumSet<@NotNull MinigameType> validTypes() {
        return EnumSet.of(MinigameType.MULTIPLAYER);
    }

    @Override
    public boolean checkCanStart(@NotNull Minigame minigame, @Nullable MinigamePlayer caller) {
        if (minigame.isTeamGame()) { // caller should not be null since that is only possible on global != multiplayer aka team game types
            MinigameMessageManager.sendMgMessage(caller, MinigameMessageType.ERROR, MgMiscLangKey.PLAYER_JUGGERNAUT_ERROR_TEAM);
            return false;
        }
        return true;
    }

    @Override
    public JuggernautModule displaySettings(@NotNull Minigame minigame) {
        return JuggernautModule.getMinigameModule(minigame);
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
    public void quitMinigame(@NotNull Minigame minigame, @NotNull MinigamePlayer mgPlayer,
                             boolean forced) {
        JuggernautModule juggernautModule = JuggernautModule.getMinigameModule(minigame);
        if (juggernautModule.getJuggernaut() != null && juggernautModule.getJuggernaut() == mgPlayer) {
            juggernautModule.setJuggernaut(null);

            if (!forced && minigame.getPlayers().size() > 1) {
                MinigamePlayer juggernaut = assignNewJuggernaut(minigame.getPlayers(), mgPlayer);

                if (juggernaut != null) {
                    juggernautModule.setJuggernaut(juggernaut);
                    MinigameMessageManager.sendMgMessage(juggernaut, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_JUGGERNAUT_PLAYERMSG);
                    MinigameMessageManager.sendMinigameMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_JUGGERNAUT_GAMEMSG,
                                    Placeholder.unparsed(MinigamePlaceHolderKey.PLAYER.getKey(), juggernaut.getDisplayName(minigame.usePlayerDisplayNames()))),
                            MinigameMessageType.INFO, juggernaut);
                }
            }
        }

        if (minigame.getPlayers().size() == 1) {
            if (minigame.getScoreboard().getTeam("juggernaut") != null)
                minigame.getScoreboard().getTeam("juggernaut").unregister();
        }
    }

    @Override
    public void endMinigame(@NotNull Minigame minigame, @NotNull List<@NotNull MinigamePlayer> winners,
                            @NotNull List<@NotNull MinigamePlayer> losers) {
        JuggernautModule.getMinigameModule(minigame).setJuggernaut(null);

        minigame.getScoreboard().getTeam("juggernaut").unregister();
    }

    private MinigamePlayer assignNewJuggernaut(List<MinigamePlayer> players, MinigamePlayer exclude) {
        List<MinigamePlayer> plys = new ArrayList<>(players);
        if (exclude != null) {
            plys.remove(exclude);
        }
        Collections.shuffle(plys);

        return plys.get(0);
    }

    private void checkScore(MinigamePlayer mgPlayer) {
        if (mgPlayer.getScore() >= mgPlayer.getMinigame().getMaxScorePerPlayer()) {
            List<MinigamePlayer> winners = new ArrayList<>();
            winners.add(mgPlayer);
            List<MinigamePlayer> losers = new ArrayList<>(mgPlayer.getMinigame().getPlayers());
            losers.remove(mgPlayer);
            pdata.endMinigame(mgPlayer.getMinigame(), winners, losers);
        }
    }

    @EventHandler
    private void minigameStart(StartMinigameEvent event) {
        if (event.getMinigame().getMechanic() == this) {
            Minigame mgm = event.getMinigame();

            mgm.getScoreboard().registerNewTeam("juggernaut");
            mgm.getScoreboard().getTeam("juggernaut").setPrefix(ChatColor.RED.toString());

            MinigamePlayer j = assignNewJuggernaut(event.getPlayers(), null);
            JuggernautModule.getMinigameModule(event.getMinigame()).setJuggernaut(j);
        }
    }

    @EventHandler
    private void playerDeath(PlayerDeathEvent event) {
        MinigamePlayer mgPlayer = pdata.getMinigamePlayer(event.getEntity());
        if (mgPlayer.getMinigame() != null && mgPlayer.getMinigame().getMechanic() == this) {
            JuggernautModule jm = JuggernautModule.getMinigameModule(mgPlayer.getMinigame());

            if (jm.getJuggernaut() == mgPlayer) {
                if (event.getEntity().getKiller() != null) {
                    MinigamePlayer pk = pdata.getMinigamePlayer(event.getEntity().getKiller());
                    jm.setJuggernaut(pk);
                    pk.addScore();
                    pk.getMinigame().setScore(pk, pk.getScore());
                    checkScore(pk);

                } else {
                    jm.setJuggernaut(assignNewJuggernaut(mgPlayer.getMinigame().getPlayers(), mgPlayer));
                }
            } else {
                if (event.getEntity().getKiller() != null) {
                    MinigamePlayer pk = pdata.getMinigamePlayer(event.getEntity().getKiller());
                    if (jm.getJuggernaut() == pk) {
                        pk.addScore();
                        pk.getMinigame().setScore(pk, pk.getScore());
                        checkScore(pk);
                    }
                }
            }
        }
    }
}
