package au.com.mineauz.minigames.commands.set;

import au.com.mineauz.minigames.commands.CommandDispatcher;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.mechanics.GameMechanicRegistry;
import au.com.mineauz.minigames.mechanics.InfectionMechanic;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.team.Team;
import au.com.mineauz.minigames.minigame.modules.team.TeamColor;
import au.com.mineauz.minigames.minigame.modules.team.TeamsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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

public class SetSurvivorTeamCommand extends ASetCommand {
    @Override
    public @NotNull String getName() {
        return "survivorteam";
    }

    @Override
    public @NotNull String @Nullable [] getAliases() {
        return new String[]{"svteam"};
    }

    @Override
    public boolean canBeConsole() {
        return true;
    }

    @Override
    public @NotNull Component getDescription() {
        return MinigameMessageManager.getMgMessage(MgCommandLangKey.COMMAND_SET_SURVIVORTEAM_DESCRIPTION);
    }

    @Override
    public @NotNull Component getUsage() {
        return MinigameMessageManager.getMgMessage(MgCommandLangKey.COMMAND_SET_SURVIVORTEAM_USAGE);
    }

    @Override
    public @Nullable String getPermission() {
        return "minigame.set.survivorteam";
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Minigame minigame,
                             final @NotNull String @Nullable [] args) {
        if (args != null) {
            if (minigame.getMechanic() instanceof final @NotNull InfectionMechanic infectionMechanic) {
                TeamColor teamColor = TeamColor.matchColor(args[0]);

                if (args[0].equalsIgnoreCase("Default")) {
                    teamColor = infectionMechanic.getDefaultSurvivorTeam();
                }

                final TeamsModule teamsModule = TeamsModule.getMinigameModule(minigame);
                final Predicate<TeamColor> teamCheck = teamColor1 -> teamColor1 == infectionMechanic.getDefaultInfectedTeam() ||
                    teamColor1 == infectionMechanic.getDefaultSurvivorTeam() ||
                    (teamsModule != null && teamsModule.hasTeam(teamColor1));

                if (teamColor != null) {
                    if (teamCheck.test(teamColor)) {
                        infectionMechanic.setSurvivorTeam(teamColor);
                        MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.SUCCESS, MgCommandLangKey.COMMAND_SET_SURVIVORTEAM_SUCCESS,
                            Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                            Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), teamColor.getCompName()));
                    } else {
                        MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTTEAM,
                            Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), args[0]),
                            Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), TeamColor.inputColorNamesComp(
                                Arrays.stream(TeamColor.values()).filter(teamCheck).collect(Collectors.toSet())).append(MiniMessage.miniMessage().
                                deserialize("<gray>, </gray><white>Default</white>"))));
                    }
                } else {
                    MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTTEAM,
                        Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), args[0]),
                        Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), TeamColor.inputColorNamesComp(
                            Arrays.stream(TeamColor.values()).filter(teamCheck).collect(Collectors.toSet())).append(MiniMessage.miniMessage().
                            deserialize("<gray>, </gray><white>Default</white>"))));
                    return false;
                }
            } else {
                MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTGAMEMECHANIC,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                        Placeholder.unparsed(MinigamePlaceHolderKey.TYPE.getKey(), GameMechanicRegistry.MgDefaultMechanic.INFECTION.getKey().value()));
            }
        }
        return false;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(@NotNull CommandSender sender, @NotNull Minigame minigame,
                                                         @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            if (minigame.getMechanic() instanceof final @NotNull InfectionMechanic infectionMechanic) {
                List<String> teams = new ArrayList<>();

                TeamsModule teamsModule = TeamsModule.getMinigameModule(minigame);
                if (teamsModule != null) {
                    for (Team team : teamsModule.getTeams()) {
                        teams.add(team.getColor().name().toLowerCase(Locale.ENGLISH));
                    }
                }

                teams.add(TeamColor.NONE.name().toLowerCase(Locale.ENGLISH));
                teams.add("default");

                teams.add(WordUtils.capitalizeFully(infectionMechanic.getDefaultInfectedTeam().toString().toLowerCase().replace("_", " ")));
                teams.add(WordUtils.capitalizeFully(infectionMechanic.getDefaultSurvivorTeam().toString().toLowerCase().replace("_", " ")));

                return CommandDispatcher.tabCompleteMatch(teams, args[0]);
            }
        }
        return null;
    }
}
