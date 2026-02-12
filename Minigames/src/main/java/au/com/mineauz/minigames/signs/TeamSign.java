package au.com.mineauz.minigames.signs;

import au.com.mineauz.minigames.gametypes.MultiplayerType;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgSignLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.team.Team;
import au.com.mineauz.minigames.minigame.modules.team.TeamColor;
import au.com.mineauz.minigames.minigame.modules.team.TeamsModule;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TeamSign extends AMinigameSign {

    @Override
    public @NotNull Component getName() {
        return MessageManager.getMessage(MgSignLangKey.TYPE_TEAM);
    }

    @Override
    public @Nullable String getCreatePermission() {
        return "minigame.sign.create.team";
    }

    @Override
    public @Nullable String getUsePermission() {
        return "minigame.sign.use.team";
    }

    @Override
    public boolean signCreate(@NotNull SignChangeEvent event) {
        event.line(1, getName());
        PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();

        if (isNeutral(event.line(2))) {
            event.line(2, MessageManager.getMessage(MgSignLangKey.TEAM_NEUTRAL));
            return true;
        } else {
            TeamColor color = TeamColor.matchColor(plainSerializer.serialize(event.line(2)));
            if (color != null) {
                event.line(2, color.getCompName());
                return true;
            }
        }

        MessageManager.sendMessage(event.getPlayer(), MinigameMessageType.ERROR, MgMiscLangKey.SIGN_ERROR_TEAM_INVALIDFORMAT,
                Placeholder.component(MinigamePlaceHolderKey.TEXT.getKey(), MessageManager.getMessage(MgSignLangKey.TEAM_NEUTRAL)));
        return false;
    }

    @Override
    public boolean signUse(@NotNull Sign sign, @NotNull MinigamePlayer mgPlayer) {
        final @Nullable Minigame mgm = mgPlayer.getMinigame();
        if (mgm != null) {
            if (mgm.isTeamGame()) {
                SignSide frontSide = sign.getSide(Side.FRONT);

                if (mgPlayer.getTeam() != matchTeam(mgm, frontSide.line(2))) {
                    if (!mgm.isWaitingForPlayers() && !isNeutral(frontSide.line(2))) {
                        Team teamToJoin = null;
                        final Team teamChosen = matchTeam(mgm, frontSide.line(2));
                        if (teamChosen != null) {
                            if (teamChosen.hasRoom()) {
                                for (final @NotNull Team team : TeamsModule.getMinigameModule(mgm).getTeams()) {
                                    if (teamToJoin == null || team.getPlayers().size() < teamToJoin.getPlayers().size()) {
                                        teamToJoin = team;
                                    }
                                }
                                if (teamChosen.getPlayers().size() - teamToJoin.getPlayers().size() < 1) {
                                    MultiplayerType.switchTeam(mgm, mgPlayer, teamChosen);
                                    MessageManager.sendMinigameMessage(mgm, MiniMessage.miniMessage().deserialize(teamChosen.getJoinAnnounceMessage(),
                                                    Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()),
                                                    Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(teamChosen.getDisplayName(), teamChosen.getTextColor()))),
                                            MinigameMessageType.INFO, mgPlayer);

                                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MiniMessage.miniMessage().deserialize(teamChosen.getPlayerAssignMessage(),
                                            Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(teamChosen.getDisplayName(), teamChosen.getTextColor()))));
                                } else {
                                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.SIGN_TEAM_ERROR_UNBALANCE);
                                }

                                mgPlayer.getPlayer().setHealth(0);
                            } else {
                                MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.PLAYER_TEAM_ASSIGN_ERROR_FULL);
                            }
                        }
                    } else if (isNeutral(frontSide.line(2)) || matchTeam(mgm, frontSide.line(2)) != mgPlayer.getTeam()) {
                        Team currentTeam = mgPlayer.getTeam();
                        Team nt = matchTeam(mgm, sign.getSide(Side.FRONT).line(2));
                        if (currentTeam != null) {
                            if (nt != null) {
                                if (nt.getPlayers().size() - currentTeam.getPlayers().size() < 2) { //todo this breaks with more then 2 teams
                                    MultiplayerType.switchTeam(mgm, mgPlayer, nt);
                                    MessageManager.sendMinigameMessage(mgm, MiniMessage.miniMessage().deserialize(nt.getJoinAnnounceMessage(),
                                                    Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()),
                                                    Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(nt.getDisplayName(), nt.getTextColor()))),
                                            MinigameMessageType.INFO, mgPlayer);

                                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MiniMessage.miniMessage().deserialize(nt.getPlayerAssignMessage(),
                                            Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(nt.getDisplayName(), nt.getTextColor()))));
                                } else {
                                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.SIGN_TEAM_ERROR_UNBALANCE);
                                }
                            } else {
                                mgPlayer.removeTeam();
                            }
                        } else {
                            if (nt != null) {
                                if (nt.getPlayers().size() < nt.getMaxPlayers()) { // todo this does not check balancing
                                    MultiplayerType.switchTeam(mgm, mgPlayer, nt);
                                    MessageManager.sendMinigameMessage(mgm, MiniMessage.miniMessage().deserialize(nt.getJoinAnnounceMessage(),
                                                    Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()),
                                                    Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(nt.getDisplayName(), nt.getTextColor()))),
                                            MinigameMessageType.INFO, mgPlayer);

                                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MiniMessage.miniMessage().deserialize(nt.getPlayerAssignMessage(),
                                            Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(nt.getDisplayName(), nt.getTextColor()))));
                                } else {
                                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.PLAYER_TEAM_ASSIGN_ERROR_FULL);
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void signBreak(@NotNull Sign sign, @NotNull MinigamePlayer mgPlayer) {
    }

    private @Nullable Team matchTeam(@NotNull Minigame mgm, @NotNull Component text) {
        TeamColor col = TeamColor.matchColor(PlainTextComponentSerializer.plainText().serialize(text).replace(" ", "_"));
        if (col != null && TeamsModule.getMinigameModule(mgm).hasTeam(col)) {
            return TeamsModule.getMinigameModule(mgm).getTeam(col);
        }
        return null;
    }
}
