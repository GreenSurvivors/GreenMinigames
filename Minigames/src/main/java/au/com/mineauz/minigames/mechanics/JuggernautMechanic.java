package au.com.mineauz.minigames.mechanics;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.events.StartMinigameEvent;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemPage;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.loadout.LoadoutModule;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class JuggernautMechanic extends AGameMechanic {
    private @Nullable MinigamePlayer juggernaut = null;

    public JuggernautMechanic(@NotNull Minigames plugin, @NotNull Key key, @NotNull Minigame minigame) {
        super(plugin, key, minigame);
    }

    @Override
    public @NotNull EnumSet<@NotNull MinigameType> validTypes() {
        return EnumSet.of(MinigameType.MULTIPLAYER);
    }

    @Override
    public boolean checkCanStart(final @Nullable MinigamePlayer caller) {
        if (minigame.isTeamGame()) { // caller should not be null since that is only possible on global != multiplayer aka team game types
            MinigameMessageManager.sendMgMessage(caller, MinigameMessageType.ERROR, MgMiscLangKey.PLAYER_JUGGERNAUT_ERROR_TEAM);
            return false;
        }
        return true;
    }

    @Override
    public @Nullable MenuItemPage displayMechanicSettings(final @NotNull Menu previous) {
        return null;
    }

    @Override
    public void startMinigame(final @Nullable MinigamePlayer caller) {
    }

    @Override
    public void stopMinigame() {
    }

    @Override
    public void onJoinMinigame(@NotNull MinigamePlayer player) {
    }

    public @Nullable MinigamePlayer getJuggernaut() {
        return juggernaut;
    }

    public void setJuggernaut(final @Nullable MinigamePlayer mgPlayer) {
        if (juggernaut != null) {
            final Team team = minigame.getScoreboard().getTeam("juggernaut");
            juggernaut.setLoadout(null);
            team.removePlayer(juggernaut.getPlayer());
        }
        juggernaut = mgPlayer;

        if (juggernaut != null) {
            final Team team = minigame.getScoreboard().getTeam("juggernaut");
            team.addPlayer(mgPlayer.getPlayer());

            MinigameMessageManager.sendMgMessage(juggernaut, MinigameMessageType.SUCCESS, MgMiscLangKey.PLAYER_JUGGERNAUT_PLAYERMSG);
            MinigameMessageManager.sendMinigameMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_JUGGERNAUT_GAMEMSG,
                Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), juggernaut.displayName())
            ), MinigameMessageType.INFO, juggernaut);

            LoadoutModule lm = LoadoutModule.getMinigameModule(minigame);
            if (lm.hasLoadout("juggernaut")) {
                mgPlayer.setLoadout(lm.getLoadout("juggernaut"));
                mgPlayer.getLoadout().equipLoadout(mgPlayer);
            }
        }
    }

    @Override
    public void quitMinigame(final @NotNull MinigamePlayer mgPlayer, final boolean forced) {
        if (getJuggernaut() != null && getJuggernaut() == mgPlayer) {
            setJuggernaut(null);

            if (!forced && minigame.getPlayers().size() > 1) {
                final MinigamePlayer juggernaut = assignNewJuggernaut(minigame.getPlayers(), mgPlayer);

                setJuggernaut(juggernaut);
                MinigameMessageManager.sendMgMessage(juggernaut, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_JUGGERNAUT_PLAYERMSG);
                MinigameMessageManager.sendMinigameMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_JUGGERNAUT_GAMEMSG,
                        Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), juggernaut.displayName())),
                    MinigameMessageType.INFO, juggernaut);
            }
        }

        if (minigame.getPlayers().size() == 1) {
            if (minigame.getScoreboard().getTeam("juggernaut") != null)
                minigame.getScoreboard().getTeam("juggernaut").unregister();
        }
    }

    @Override
    public void endMinigame(final @NotNull List<@NotNull MinigamePlayer> winners,
                            final @NotNull List<@NotNull MinigamePlayer> losers) {
        setJuggernaut(null);

        minigame.getScoreboard().getTeam("juggernaut").unregister();
    }

    private @NotNull MinigamePlayer assignNewJuggernaut(@NotNull List<@NotNull MinigamePlayer> players, @Nullable MinigamePlayer exclude) {
        List<MinigamePlayer> plys = new ArrayList<>(players);
        if (exclude != null) {
            plys.remove(exclude);
        }
        Collections.shuffle(plys);

        return plys.getFirst();
    }

    private void checkScore(@NotNull MinigamePlayer mgPlayer) {
        if (mgPlayer.getScore() >= mgPlayer.getMinigame().getMaxScorePerPlayer()) {
            List<MinigamePlayer> winners = new ArrayList<>();
            winners.add(mgPlayer);
            List<MinigamePlayer> losers = new ArrayList<>(mgPlayer.getMinigame().getPlayers());
            losers.remove(mgPlayer);
            plugin.getPlayerManager().endMinigame(mgPlayer.getMinigame(), winners, losers);
        }
    }

    @EventHandler
    private void minigameStart(@NotNull StartMinigameEvent event) {
        if (event.getMinigame().getMechanic() == this) {
            Minigame mgm = event.getMinigame();

            mgm.getScoreboard().registerNewTeam("juggernaut");
            mgm.getScoreboard().getTeam("juggernaut").prefix(Component.text("", NamedTextColor.RED)); // todo check if this works

            MinigamePlayer newJuggernaut = assignNewJuggernaut(event.getPlayers(), null);
            setJuggernaut(newJuggernaut);
        }
    }

    @EventHandler
    private void playerDeath(@NotNull PlayerDeathEvent event) {
        MinigamePlayer mgPlayer = plugin.getPlayerManager().getMinigamePlayer(event.getEntity());
        if (mgPlayer.getMinigame() != null && mgPlayer.getMinigame().getMechanic() == this) {

            if (getJuggernaut() == mgPlayer) {
                if (event.getEntity().getKiller() != null) {
                    MinigamePlayer pk = plugin.getPlayerManager().getMinigamePlayer(event.getEntity().getKiller());
                    setJuggernaut(pk);
                    pk.addScore();
                    pk.getMinigame().setScore(pk, pk.getScore());
                    checkScore(pk);

                } else {
                    setJuggernaut(assignNewJuggernaut(mgPlayer.getMinigame().getPlayers(), mgPlayer));
                }
            } else {
                if (event.getEntity().getKiller() != null) {
                    MinigamePlayer pk = plugin.getPlayerManager().getMinigamePlayer(event.getEntity().getKiller());
                    if (getJuggernaut() == pk) {
                        pk.addScore();
                        pk.getMinigame().setScore(pk, pk.getScore());
                        checkScore(pk);
                    }
                }
            }
        }
    }

    @Override
    public void save(@NotNull CommentedConfigurationNode config){
    }

    @Override
    public void load(@NotNull CommentedConfigurationNode config) {
    }

    @Override
    public boolean useSeparateConfig() {
        return false;
    }
}
