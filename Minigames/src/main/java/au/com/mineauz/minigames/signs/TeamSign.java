package au.com.mineauz.minigames.signs;

import au.com.mineauz.minigames.gametypes.MultiplayerType;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgSignLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.Team;
import au.com.mineauz.minigames.minigame.TeamColor;
import au.com.mineauz.minigames.minigame.modules.TeamsModule;
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
        return MinigameMessageManager.getMgMessage(MgSignLangKey.TYPE_TEAM);
    }

    @Override
    public String getCreatePermission() {
        return "minigame.sign.create.team";
    }

    @Override
    public String getUsePermission() {
        return "minigame.sign.use.team";
    }

    @Override
    public boolean signCreate(@NotNull SignChangeEvent event) {
        event.line(1, getName());
        PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();

        if (isNeutral(event.line(2))) {
            event.line(2, MinigameMessageManager.getMgMessage(MgSignLangKey.TEAM_NEUTRAL));
            return true;
        } else {
            TeamColor color = TeamColor.matchColor(plainSerializer.serialize(event.line(2)));
            if (color != null) {
                event.line(2, color.getCompName());
                return true;
            }
        }

        MinigameMessageManager.sendMgMessage(event.getPlayer(), MinigameMessageType.ERROR, MgMiscLangKey.SIGN_ERROR_TEAM_INVALIDFORMAT,
                Placeholder.component(MinigamePlaceHolderKey.TEXT.getKey(), MinigameMessageManager.getMgMessage(MgSignLangKey.TEAM_NEUTRAL)));
        return false;
    }

    @Override
    public boolean signUse(@NotNull Sign sign, @NotNull MinigamePlayer mgPlayer) {
        if (mgPlayer.isInMinigame()) {
            Minigame mgm = mgPlayer.getMinigame();
            if (mgm.isTeamGame()) {
                SignSide frontSide = sign.getSide(Side.FRONT);
                PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();

                if (mgPlayer.getTeam() != matchTeam(mgm, frontSide.line(2))) {
                    if (!mgm.isWaitingForPlayers() && !isNeutral(frontSide.line(2))) {
                        Team sm = null;
                        Team nt = matchTeam(mgm, frontSide.line(2));
                        if (nt != null) {
                            if (nt.hasRoom()) {
                                for (Team t : TeamsModule.getMinigameModule(mgm).getTeams()) {
                                    if (sm == null || t.getPlayers().size() < sm.getPlayers().size())
                                        sm = t;
                                }
                                if (nt.getPlayers().size() - sm.getPlayers().size() < 1) {
                                    MultiplayerType.switchTeam(mgm, mgPlayer, nt);
                                    MinigameMessageManager.sendMinigameMessage(mgm, MiniMessage.miniMessage().deserialize(nt.getJoinAnnounceMessage(),
                                                    Placeholder.unparsed(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.getDisplayName(mgm.usePlayerDisplayNames())),
                                                    Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(nt.getDisplayName(), nt.getTextColor()))),
                                            MinigameMessageType.INFO, mgPlayer);

                                    MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MiniMessage.miniMessage().deserialize(nt.getPlayerAssignMessage(),
                                            Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(nt.getDisplayName(), nt.getTextColor()))));
                                } else {
                                    MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.SIGN_TEAM_ERROR_UNBALANCE);
                                }

                                mgPlayer.getPlayer().damage(mgPlayer.getPlayer().getHealth());
                            } else {
                                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.PLAYER_TEAM_ASSIGN_ERROR_FULL);
                            }
                        }
                    } else if (isNeutral(frontSide.line(2)) || matchTeam(mgm, frontSide.line(2)) != mgPlayer.getTeam()) {
                        Team currentTeam = mgPlayer.getTeam();
                        Team nt = matchTeam(mgm, sign.getSide(Side.FRONT).line(2));
                        if (currentTeam != null) {
                            if (nt != null) {
                                if (nt.getPlayers().size() - currentTeam.getPlayers().size() < 2) { //todo this breaks with more then 2 teams
                                    MultiplayerType.switchTeam(mgm, mgPlayer, nt);
                                    MinigameMessageManager.sendMinigameMessage(mgm, MiniMessage.miniMessage().deserialize(nt.getJoinAnnounceMessage(),
                                                    Placeholder.unparsed(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.getDisplayName(mgm.usePlayerDisplayNames())),
                                                    Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(nt.getDisplayName(), nt.getTextColor()))),
                                            MinigameMessageType.INFO, mgPlayer);

                                    MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MiniMessage.miniMessage().deserialize(nt.getPlayerAssignMessage(),
                                            Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(nt.getDisplayName(), nt.getTextColor()))));
                                } else {
                                    MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.SIGN_TEAM_ERROR_UNBALANCE);
                                }
                            } else {
                                mgPlayer.removeTeam();
                            }
                        } else {
                            if (nt != null) {
                                if (nt.getPlayers().size() < nt.getMaxPlayers()) { // todo this does not check balancing
                                    MultiplayerType.switchTeam(mgm, mgPlayer, nt);
                                    MinigameMessageManager.sendMinigameMessage(mgm, MiniMessage.miniMessage().deserialize(nt.getJoinAnnounceMessage(),
                                                    Placeholder.unparsed(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.getDisplayName(mgm.usePlayerDisplayNames())),
                                                    Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(nt.getDisplayName(), nt.getTextColor()))),
                                            MinigameMessageType.INFO, mgPlayer);

                                    MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MiniMessage.miniMessage().deserialize(nt.getPlayerAssignMessage(),
                                            Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(nt.getDisplayName(), nt.getTextColor()))));
                                } else {
                                    MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.PLAYER_TEAM_ASSIGN_ERROR_FULL);
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
