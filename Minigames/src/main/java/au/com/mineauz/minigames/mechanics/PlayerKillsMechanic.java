package au.com.mineauz.minigames.mechanics;

import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.Team;
import au.com.mineauz.minigames.minigame.modules.MinigameModule;
import au.com.mineauz.minigames.minigame.modules.TeamsModule;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class PlayerKillsMechanic extends GameMechanicBase {

    protected PlayerKillsMechanic() {
    }

    @Override
    public @NotNull String getMechanicName() {
        return "kills";
    }

    @Override
    public @NotNull EnumSet<@NotNull MinigameType> validTypes() {
        return EnumSet.of(MinigameType.MULTIPLAYER);
    }

    @Override
    public boolean checkCanStart(@NotNull Minigame minigame, @Nullable MinigamePlayer caller) {
        return true;
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
    private void playerAttackPlayer(@NotNull PlayerDeathEvent event) {
        MinigamePlayer mgPlayerWhoDied = pdata.getMinigamePlayer(event.getEntity());
        Minigame mgm = mgPlayerWhoDied.getMinigame();
        if (mgPlayerWhoDied.isInMinigame() && mgm.getMechanicName().equals("kills")) {
            MinigamePlayer attacker;
            if (mgPlayerWhoDied.getPlayer().getKiller() != null) {
                attacker = pdata.getMinigamePlayer(mgPlayerWhoDied.getPlayer().getKiller());
                if (attacker == mgPlayerWhoDied) {
                    return;
                }
            } else {
                return;
            }

            if (!mgm.equals(attacker.getMinigame())) {
                return;
            }

            if (mgPlayerWhoDied.getTeam() == null) {
                attacker.addScore();
                mgm.setScore(attacker, attacker.getScore());

                if (mgm.getMaxScore() != 0 && attacker.getScore() >= mgm.getMaxScorePerPlayer()) {
                    List<MinigamePlayer> losers = new ArrayList<>(mgm.getPlayers().size() - 1);
                    List<MinigamePlayer> winner = new ArrayList<>(1);
                    winner.add(attacker);
                    for (MinigamePlayer player : mgm.getPlayers()) {
                        if (player != attacker)
                            losers.add(player);
                    }
                    pdata.endMinigame(mgm, winner, losers);
                }
            } else {
                Team team = mgPlayerWhoDied.getTeam();
                Team ateam = attacker.getTeam();

                if (team != ateam) {
                    attacker.addScore();
                    mgm.setScore(attacker, attacker.getScore());

                    ateam.addScore();
                    if (mgm.getMaxScore() != 0 && mgm.getMaxScorePerPlayer() <= ateam.getScore()) {
                        MinigameMessageManager.sendMinigameMessage(mgm, MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_KILLS_FINALKILL,
                                Placeholder.unparsed(MinigamePlaceHolderKey.PLAYER.getKey(), attacker.getName()),
                                Placeholder.unparsed(MinigamePlaceHolderKey.OTHER_PLAYER.getKey(), mgPlayerWhoDied.getName())));

                        List<MinigamePlayer> w = new ArrayList<>(ateam.getPlayers());
                        List<MinigamePlayer> l = new ArrayList<>(mgm.getPlayers().size() - ateam.getPlayers().size());
                        for (Team t : TeamsModule.getMinigameModule(mgm).getTeams()) {
                            if (t != ateam)
                                l.addAll(t.getPlayers());
                        }
                        plugin.getPlayerManager().endMinigame(mgm, w, l);
                    }
                }
            }
        }
    }

    @EventHandler
    private void playerSuicide(@NotNull PlayerDeathEvent event) {
        MinigamePlayer mgPlayer = pdata.getMinigamePlayer(event.getEntity());
        if (mgPlayer.isInMinigame() &&
                (mgPlayer.getPlayer().getKiller() == null || mgPlayer.getPlayer().getKiller() == mgPlayer.getPlayer()) &&
                mgPlayer.getMinigame().hasStarted()) {
            Minigame mgm = mgPlayer.getMinigame();
            if (mgm.getMechanicName().equals("kills")) {
                mgPlayer.takeScore();
                mgm.setScore(mgPlayer, mgPlayer.getScore());
                if (mgm.isTeamGame())
                    mgPlayer.getTeam().setScore(mgPlayer.getTeam().getScore() - 1);
            }
        }
    }

    @EventHandler
    public void playerAutoBalance(@NotNull PlayerDeathEvent event) {
        MinigamePlayer mgPlayer = pdata.getMinigamePlayer(event.getEntity());
        if (mgPlayer.isInMinigame() && mgPlayer.getMinigame().isTeamGame()) {
            Minigame mgm = mgPlayer.getMinigame();

            if (mgm.getMechanicName().equals("kills")) {
                autoBalanceOnDeath(mgPlayer, mgm);
            }
        }
    }
}
