package au.com.mineauz.minigames.commands;

import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.team.Team;
import au.com.mineauz.minigames.minigame.modules.team.TeamColor;
import au.com.mineauz.minigames.minigame.modules.team.TeamsModule;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ScoreCommand extends ACommand {

    @Override
    public @NotNull String getName() {
        return "score";
    }

    @Override
    public boolean canBeConsole() {
        return true;
    }

    @Override
    public @NotNull Component getDescription() {
        return MessageManager.getMessage(MgCommandLangKey.COMMAND_SCORE_DESCRIPTION);
    }

    @Override
    public @NotNull Component getUsage() {
        return MessageManager.getMessage(MgCommandLangKey.COMMAND_SCORE_USAGE);
    }

    @Override
    public @Nullable String getPermission() {
        return "minigame.score";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull String @NotNull [] args) { // todo make minigame optional if player was used.
        if (args.length >= 3) {
            final @Nullable Minigame minigame = PLUGIN.getMinigameManager().getMinigame(args[1]);

            if (minigame == null) {
                MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOMINIGAME,
                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), args[1]));
                return true;
            }

            final @Nullable TeamColor color = TeamColor.matchColor(args[2]);
            @Nullable MinigamePlayer mgPlayer = null;

            if (color == null) {
                List<Player> plys = PLUGIN.getServer().matchPlayer(args[2]);
                if (!plys.isEmpty()) {
                    mgPlayer = PLUGIN.getPlayerManager().getMinigamePlayer(plys.getFirst());

                    if (!mgPlayer.isInMinigame()) {
                        MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTINMINIGAME_PLAYER,
                            Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()));
                        return true;
                    }

                    if (mgPlayer.getMinigame() != minigame) {
                        MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTPARTOFTHISMINIGAME,
                            Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), minigame.getDisplayName()),
                            Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()));
                        return true;
                    }
                } else {
                    MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAD_ERROR_NOTPLAYER,
                        Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), args[2]));
                    return true;
                }
            } else if (!minigame.isTeamGame()) {
                MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOTTEAMGAME,
                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()));
                return true;
            }

            switch (args[0].toLowerCase(Locale.ENGLISH)) {
                case "get" -> {
                    if (mgPlayer != null) {
                        MessageManager.sendMessage(sender, MinigameMessageType.INFO, MgCommandLangKey.COMMAND_SCORE_GET_PLAYER,
                            Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()),
                            Placeholder.unparsed(MinigamePlaceHolderKey.SCORE.getKey(), String.valueOf(mgPlayer.getScore())));
                    } else {
                        final TeamsModule tmod = TeamsModule.getMinigameModule(minigame);

                        if (tmod.hasTeam(color)) {
                            final Team changedTeam = tmod.getTeam(color);
                            MessageManager.sendMessage(sender, MinigameMessageType.INFO, MgCommandLangKey.COMMAND_SCORE_GET_TEAM,
                                Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), changedTeam.getColoredDisplayName()),
                                Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                                Placeholder.unparsed(MinigamePlaceHolderKey.SCORE.getKey(), String.valueOf(changedTeam.getScore())));
                        } else {
                            MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOTEAM,
                                Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                                Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), color.getCompName()));
                        }
                    }
                    return true;
                } // end case

                case "set" -> {
                    if (args.length >= 4) {
                        if (args[3].matches("^[+\\-]?[0-9]+$")) {
                            int score = Integer.parseInt(args[3]);

                            if (mgPlayer != null) {
                                mgPlayer.setScore(score);
                                mgPlayer.getMinigame().setScore(mgPlayer, mgPlayer.getScore());
                                MessageManager.sendMessage(sender, MinigameMessageType.INFO, MgCommandLangKey.COMMAND_SCORE_SET_PLAYER,
                                    Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()),
                                    Placeholder.unparsed(MinigamePlaceHolderKey.SCORE.getKey(), String.valueOf(score)));

                                if (mgPlayer.getMinigame().getMaxScore() != 0 && score >= mgPlayer.getMinigame().getMaxScorePerPlayer()) {
                                    PLUGIN.getPlayerManager().winMinigame(mgPlayer);
                                }
                            } else {
                                TeamsModule tmod = TeamsModule.getMinigameModule(minigame);

                                if (minigame.hasPlayers()) {
                                    Team changedTeam;

                                    if (tmod.hasTeam(color)) {
                                        changedTeam = tmod.getTeam(color);
                                        changedTeam.setScore(score);
                                        MessageManager.sendMessage(sender, MinigameMessageType.INFO, MgCommandLangKey.COMMAND_SCORE_SET_TEAM,
                                            Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), changedTeam.getColoredDisplayName()),
                                            Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                                            Placeholder.unparsed(MinigamePlaceHolderKey.SCORE.getKey(), String.valueOf(score)));
                                    } else {
                                        MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOTEAM,
                                            Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                                            Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), color.getCompName()));
                                        return true;
                                    }

                                    // check new score
                                    if (minigame.getMaxScore() != 0 && score >= minigame.getMaxScorePerPlayer()) {
                                        List<MinigamePlayer> winners = new ArrayList<>(changedTeam.getPlayers());
                                        List<MinigamePlayer> losers = new ArrayList<>(minigame.getPlayers().size() - changedTeam.getPlayers().size());
                                        for (Team team : tmod.getTeams()) {
                                            if (team != changedTeam) {
                                                losers.addAll(team.getPlayers());
                                            }
                                        }
                                        PLUGIN.getPlayerManager().endMinigame(minigame, winners, losers);
                                    }
                                } else {
                                    MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_ISEMPTY,
                                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()));
                                }
                            }
                        } else {
                            MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTNUMBER,
                                Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), args[3]));
                        }
                    } else {
                        return false;
                    }

                    return true;
                } // end case

                case "add" -> {
                    final int score;

                    if (args.length >= 4) {
                        if (args[3].matches("^[+\\-]?[0-9]+$")) {
                            score = Integer.parseInt(args[3]);
                        } else {
                            MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTNUMBER,
                                Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), args[3]));
                            return true;
                        }
                    } else {
                        score = 1;
                    }

                    if (mgPlayer != null) {
                        mgPlayer.addScore(score);
                        mgPlayer.getMinigame().setScore(mgPlayer, mgPlayer.getScore());
                        MessageManager.sendMessage(sender, MinigameMessageType.INFO, MgCommandLangKey.COMMAND_SCORE_ADD_PLAYER,
                            Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), mgPlayer.displayName()),
                            Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(score)),
                            Placeholder.unparsed(MinigamePlaceHolderKey.SCORE.getKey(), String.valueOf(mgPlayer.getScore())));

                        if (mgPlayer.getMinigame().getMaxScore() != 0 && mgPlayer.getScore() >= mgPlayer.getMinigame().getMaxScorePerPlayer()) {
                            PLUGIN.getPlayerManager().winMinigame(mgPlayer);
                        }
                    } else {
                        final TeamsModule tmod = TeamsModule.getMinigameModule(minigame);

                        if (minigame.hasPlayers()) {
                            Team changedTeam;
                            if (tmod.hasTeam(color)) {
                                changedTeam = tmod.getTeam(color);
                                changedTeam.addScore(score);
                                MessageManager.sendMessage(sender, MinigameMessageType.INFO, MgCommandLangKey.COMMAND_SCORE_ADD_TEAM,
                                    Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), changedTeam.getColoredDisplayName()),
                                    Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(score)),
                                    Placeholder.unparsed(MinigamePlaceHolderKey.SCORE.getKey(), String.valueOf(changedTeam.getScore())));
                            } else {
                                MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOTEAM,
                                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                                    Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), color.getCompName()));
                                return true;
                            }

                            if (minigame.getMaxScore() != 0 && changedTeam.getScore() >= minigame.getMaxScorePerPlayer()) {
                                List<MinigamePlayer> winners = new ArrayList<>(changedTeam.getPlayers());
                                List<MinigamePlayer> losers = new ArrayList<>(minigame.getPlayers().size() - changedTeam.getPlayers().size());
                                for (Team team : tmod.getTeams()) {
                                    if (team != changedTeam) {
                                        losers.addAll(team.getPlayers());
                                    }
                                }
                                PLUGIN.getPlayerManager().endMinigame(minigame, winners, losers);
                            }
                        } else {
                            MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_ISEMPTY,
                                Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()));
                        }
                    }
                    return true;
                } // end case
            } // end switch
        }
        return false;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(@NotNull CommandSender sender,
                                                         @NotNull String @NotNull [] args) {
        switch (args.length) {
            case 1 -> {
                return CommandDispatcher.tabCompleteMatch(List.of("get", "set", "add"), args[0]);
            }
            case 2 -> {
                List<String> mgs = new ArrayList<>(PLUGIN.getMinigameManager().getAllMinigames().keySet());
                return CommandDispatcher.tabCompleteMatch(mgs, args[1]);
            }
            case 3 -> {
                List<String> pt = new ArrayList<>(PLUGIN.getServer().getOnlinePlayers().size());
                for (Player pl : PLUGIN.getServer().getOnlinePlayers()) {
                    pt.add(pl.getName());
                }

                Minigame mgm = PLUGIN.getMinigameManager().getMinigame(args[1]);

                if (mgm != null && mgm.isTeamGame()) {
                    pt.addAll(TeamsModule.getMinigameModule(mgm).getTeams().stream().map(t -> t.getColor().name()).toList());
                }

                return CommandDispatcher.tabCompleteMatch(pt, args[2]);
            }
            case 4 -> {
                if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("add")) {
                    if (args[3].matches("^[+\\-]?[0-9]+$")) {
                        List<String> numbers = new ArrayList<>(10);

                        for (int i = 0; i < 10; i++) {
                            numbers.add(args[3] + i);
                        }

                        return CommandDispatcher.tabCompleteMatch(numbers, args[3]);
                    } // not a number
                } // not add / set
            } // more than 4 arguments
        } // end switch

        return null;
    }
}
