package au.com.mineauz.minigames.commands.set;

import au.com.mineauz.minigames.commands.CommandDispatcher;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.mechanics.GameMechanicRegistry;
import au.com.mineauz.minigames.mechanics.InfectionMechanic;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.MgDefaultModules;
import au.com.mineauz.minigames.minigame.modules.team.Team;
import au.com.mineauz.minigames.minigame.modules.team.TeamColor;
import au.com.mineauz.minigames.minigame.modules.team.TeamsModule;
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
        return MessageManager.getMessage(MgCommandLangKey.COMMAND_SET_INFECTEDTEAM_DESCRIPTION);
    }

    @Override
    public @NotNull Component getUsage() {
        return MessageManager.getMessage(MgCommandLangKey.COMMAND_SET_INFECTEDTEAM_USAGE);
    }

    @Override
    public @Nullable String getPermission() {
        return "minigame.set.infectedteam";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Minigame minigame,
                             @NotNull String @Nullable [] args) {
        if (args != null) {

            if (minigame.getMechanic() instanceof final @NotNull InfectionMechanic infectionMechanic) {
                final @Nullable TeamsModule teamsModule = TeamsModule.getMinigameModule(minigame);
                if (teamsModule != null) {
                    TeamColor teamColor = TeamColor.matchColor(args[0]);

                    final Predicate<TeamColor> teamCheck = teamColor1 -> teamColor1 == infectionMechanic.getDefaultInfectedTeam() ||
                        teamColor1 == infectionMechanic.getDefaultSurvivorTeam() ||
                        teamsModule.hasTeam(teamColor1) ||
                        teamColor1 == TeamColor.NONE;

                    if (teamColor != null) {
                        if (teamCheck.test(teamColor)) {
                            infectionMechanic.setInfectedTeam(teamColor);
                            MessageManager.sendMessage(sender, MinigameMessageType.SUCCESS, MgCommandLangKey.COMMAND_SET_INFECTEDTEAM_SUCCESS,
                                Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                                Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), teamColor.getCompName()));
                            return true;
                        } else {
                            MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTTEAM,
                                Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), args[0]),
                                Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), TeamColor.inputColorNamesComp(
                                    Arrays.stream(TeamColor.values()).filter(teamCheck).collect(Collectors.toSet()))));
                        }
                    } else {
                        if (args[0].equalsIgnoreCase("Default")) {
                            teamColor = infectionMechanic.getDefaultInfectedTeam();
                            infectionMechanic.setInfectedTeam(teamColor);

                            MessageManager.sendMessage(sender, MinigameMessageType.SUCCESS, MgCommandLangKey.COMMAND_SET_INFECTEDTEAM_SUCCESS,
                                Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                                Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), teamColor.getCompName()));
                            return true;
                        } else {
                            MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTTEAM,
                                Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), args[0]),
                                Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), TeamColor.inputColorNamesComp(
                                    Arrays.stream(TeamColor.values()).filter(teamCheck).collect(Collectors.toSet()))));
                        }
                    }
                } else {
                    MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTGAMEMECHANIC,
                            Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                            Placeholder.unparsed(MinigamePlaceHolderKey.TYPE.getKey(), MgDefaultModules.TEAMS.getKey().value()));
                }
            } else {
                MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTGAMEMECHANIC,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                        Placeholder.unparsed(MinigamePlaceHolderKey.TYPE.getKey(), GameMechanicRegistry.MgDefaultMechanic.INFECTION.getKey().value()));
            }
        }
        return false;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(final @NotNull CommandSender sender, final @NotNull Minigame minigame,
                                                         final @NotNull String @NotNull [] args) {
        final @Nullable TeamsModule teamsModule = TeamsModule.getMinigameModule(minigame);
        if (minigame.getMechanic() instanceof final @NotNull InfectionMechanic infectionMechanic &&
            teamsModule != null) {
            if (args.length == 1) {
                List<String> teams = new ArrayList<>();
                for (Team team : teamsModule.getTeams()) {
                    teams.add(team.getColor().name().toLowerCase(Locale.ENGLISH));
                }
                teams.add(TeamColor.NONE.name().toLowerCase(Locale.ENGLISH));
                teams.add("default");
                teams.add(WordUtils.capitalizeFully(infectionMechanic.getDefaultInfectedTeam().toString().toLowerCase()));
                teams.add(WordUtils.capitalizeFully(infectionMechanic.getDefaultSurvivorTeam().toString().toLowerCase()));
                return CommandDispatcher.tabCompleteMatch(teams, args[0]);
            }
        }
        return null;
    }
}
