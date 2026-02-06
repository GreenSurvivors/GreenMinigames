package au.com.mineauz.minigames.mechanics;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.managers.MinigamePlayerManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemPage;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.team.Team;
import au.com.mineauz.minigames.minigame.modules.team.TeamsModule;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class PlayerKillsMechanic extends AGameMechanic {


    public PlayerKillsMechanic(final @NotNull Minigames plugin, final @NotNull Key key, final @NotNull Minigame minigame) {
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
        return true;
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
    private void playerAttackPlayer(final @NotNull PlayerDeathEvent event) {
        final @NotNull MinigamePlayerManager playerManager = plugin.getPlayerManager();
        final @NotNull MinigamePlayer mgPlayerWhoDied = playerManager.getMinigamePlayer(event.getEntity());
        if (minigame.equals(mgPlayerWhoDied.getMinigame())) {
            final @NotNull MinigamePlayer attacker;
            if (mgPlayerWhoDied.getPlayer().getKiller() != null) {
                attacker = playerManager.getMinigamePlayer(mgPlayerWhoDied.getPlayer().getKiller());
                if (attacker == mgPlayerWhoDied) {
                    return;
                }
            } else {
                return;
            }

            if (!minigame.equals(attacker.getMinigame())) {
                return;
            }

            if (mgPlayerWhoDied.getTeam() == null) {
                attacker.addScore();
                minigame.setScore(attacker, attacker.getScore());

                if (minigame.getMaxScore() != 0 && attacker.getScore() >= minigame.getMaxScorePerPlayer()) {
                    final @NotNull List<@NotNull MinigamePlayer> losers = new ArrayList<>(minigame.getPlayers().size() - 1);
                    final @NotNull List<MinigamePlayer> winner = new ArrayList<>(1);
                    winner.add(attacker);
                    for (final @NotNull MinigamePlayer player : minigame.getPlayers()) {
                        if (player != attacker)
                            losers.add(player);
                    }
                    playerManager.endMinigame(minigame, winner, losers);
                }
            } else {
                final Team team = mgPlayerWhoDied.getTeam();
                final Team attakerTeam = attacker.getTeam();

                if (team != attakerTeam) {
                    attacker.addScore();
                    minigame.setScore(attacker, attacker.getScore());

                    attakerTeam.addScore();
                    if (minigame.getMaxScore() != 0 && minigame.getMaxScorePerPlayer() <= attakerTeam.getScore()) {
                        MinigameMessageManager.sendMinigameMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_KILLS_FINALKILL,
                            Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), attacker.displayName()),
                            Placeholder.component(MinigamePlaceHolderKey.OTHER_PLAYER.getKey(), mgPlayerWhoDied.displayName())));

                        final @NotNull List<@NotNull MinigamePlayer> winners = new ArrayList<>(attakerTeam.getPlayers());
                        final @NotNull List<@NotNull MinigamePlayer> losers = new ArrayList<>(minigame.getPlayers().size() - attakerTeam.getPlayers().size());
                        for (Team t : TeamsModule.getMinigameModule(minigame).getTeams()) {
                            if (t != attakerTeam)
                                losers.addAll(t.getPlayers());
                        }
                        playerManager.endMinigame(minigame, winners, losers);
                    }
                }
            }
        }
    }

    @EventHandler
    private void playerSuicide(final @NotNull PlayerDeathEvent event) {
        MinigamePlayer mgPlayer = plugin.getPlayerManager().getMinigamePlayer(event.getEntity());
        if (minigame.equals(mgPlayer.getMinigame())) {
            if ((mgPlayer.getPlayer().getKiller() == null || mgPlayer.getPlayer().getKiller() == mgPlayer.getPlayer()) &&
                minigame.hasStarted()) {

                mgPlayer.takeScore();
                minigame.setScore(mgPlayer, mgPlayer.getScore());
                if (minigame.isTeamGame())
                    mgPlayer.getTeam().setScore(mgPlayer.getTeam().getScore() - 1);
            }
        }
    }

    @EventHandler
    public void playerAutoBalance(final @NotNull PlayerDeathEvent event) {
        final @NotNull MinigamePlayer mgPlayer = plugin.getPlayerManager().getMinigamePlayer(event.getEntity());

        if (minigame.isTeamGame() && minigame.equals(mgPlayer.getMinigame())) {
            autoBalanceOnDeath(mgPlayer, minigame);
        }
    }
}
