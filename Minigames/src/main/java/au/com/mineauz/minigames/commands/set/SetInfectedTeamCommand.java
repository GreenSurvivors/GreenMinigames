package au.com.mineauz.minigames.commands.set;

import au.com.mineauz.minigames.commands.CommandDispatcher;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.Team;
import au.com.mineauz.minigames.minigame.TeamColor;
import au.com.mineauz.minigames.minigame.modules.InfectionModule;
import au.com.mineauz.minigames.minigame.modules.MgModules;
import au.com.mineauz.minigames.minigame.modules.TeamsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.apache.commons.text.WordUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SetInfectedTeamCommand extends ASetCommand {

    @Override
    public @NotNull String getName() {
        return "infectedteam";
    }

    @Override
    public @NotNull String @NotNull [] getAliases() {
        return new String[]{"infteam"};
    }

    @Override
    public boolean canBeConsole() {
        return true;
    }

    @Override
    public @NotNull Component getDescription() {
        return MinigameMessageManager.getMgMessage(MgCommandLangKey.COMMAND_SET_INFECTEDTEAM_DESCRIPTION);
    }

    @Override
    public @NotNull Component getUsage() {
        return MinigameMessageManager.getMgMessage(MgCommandLangKey.COMMAND_SET_INFECTEDTEAM_USAGE);
    }

    @Override
    public @Nullable String getPermission() {
        return "minigame.set.infectedteam";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Minigame minigame,
                             @NotNull String @Nullable [] args) {
        if (args != null) {
            InfectionModule infectionModule = InfectionModule.getMinigameModule(minigame);
            TeamsModule teamsModule = TeamsModule.getMinigameModule(minigame);

            if (infectionModule != null) {
                if (teamsModule != null) {
                    TeamColor teamColor = TeamColor.matchColor(args[0]);

                    final Predicate<TeamColor> teamCheck = teamColor1 -> teamColor1 == infectionModule.getDefaultInfectedTeam() ||
                        teamColor1 == infectionModule.getDefaultSurvivorTeam() ||
                        teamsModule.hasTeam(teamColor1) ||
                        teamColor1 == TeamColor.NONE;

                    if (teamColor != null) {
                        if (teamCheck.test(teamColor)) {
                            infectionModule.setInfectedTeam(teamColor);
                            MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.SUCCESS, MgCommandLangKey.COMMAND_SET_INFECTEDTEAM_SUCCESS,
                                Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                                Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), teamColor.getCompName()));
                            return true;
                        } else {
                            MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTTEAM,
                                Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), args[0]),
                                Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), TeamColor.inputColorNamesComp(
                                    Arrays.stream(TeamColor.values()).filter(teamCheck).collect(Collectors.toSet()))));
                        }
                    } else {
                        if (args[0].equalsIgnoreCase("Default")) {
                            teamColor = infectionModule.getDefaultInfectedTeam();
                            infectionModule.setInfectedTeam(teamColor);

                            MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.SUCCESS, MgCommandLangKey.COMMAND_SET_INFECTEDTEAM_SUCCESS,
                                Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                                Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), teamColor.getCompName()));
                            return true;
                        } else {
                            MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTTEAM,
                                Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), args[0]),
                                Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), TeamColor.inputColorNamesComp(
                                    Arrays.stream(TeamColor.values()).filter(teamCheck).collect(Collectors.toSet()))));
                        }
                    }
                } else {
                    MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTGAMEMECHANIC,
                            Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                            Placeholder.unparsed(MinigamePlaceHolderKey.TYPE.getKey(), MgModules.TEAMS.getKey().value()));
                }
            } else {
                MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTGAMEMECHANIC,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                        Placeholder.unparsed(MinigamePlaceHolderKey.TYPE.getKey(), MgModules.INFECTION.getKey().value()));
            }
        }
        return false;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(@NotNull CommandSender sender, @NotNull Minigame minigame,
                                                         @NotNull String @NotNull [] args) {
        InfectionModule infectionModule = InfectionModule.getMinigameModule(minigame);
        TeamsModule teamsModule = TeamsModule.getMinigameModule(minigame);
        if (infectionModule != null && teamsModule != null) {
            if (args.length == 1) {
                List<String> teams = new ArrayList<>();
                for (Team team : teamsModule.getTeams()) {
                    teams.add(team.getColor().name().toLowerCase(Locale.ENGLISH));
                }
                teams.add(TeamColor.NONE.name().toLowerCase(Locale.ENGLISH));
                teams.add("default");
                teams.add(WordUtils.capitalizeFully(infectionModule.getDefaultInfectedTeam().toString().toLowerCase()));
                teams.add(WordUtils.capitalizeFully(infectionModule.getDefaultSurvivorTeam().toString().toLowerCase()));
                return CommandDispatcher.tabCompleteMatch(teams, args[0]);
            }
        }
        return null;
    }
}
