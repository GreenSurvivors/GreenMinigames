package au.com.mineauz.minigames.commands.set;

import au.com.mineauz.minigames.commands.CommandDispatcher;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.MgDefaultModules;
import au.com.mineauz.minigames.minigame.modules.team.TeamColor;
import au.com.mineauz.minigames.minigame.modules.team.TeamsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class SetDefaultWinnerCommand extends ASetCommand {

    @Override
    public @NotNull String getName() {
        return "defaultwinner";
    }

    @Override
    public @NotNull String @Nullable [] getAliases() {
        return new String[]{"defwin"};
    }

    @Override
    public boolean canBeConsole() {
        return true;
    }

    @Override
    public @NotNull Component getDescription() {
        return MessageManager.getMessage(MgCommandLangKey.COMMAND_SET_DEFAULTWINNER_DESCRIPTION);
    }

    @Override
    public @NotNull Component getUsage() {
        return MessageManager.getMessage(MgCommandLangKey.COMMAND_SET_DEFAULTWINNER_USAGE);
    }

    @Override
    public @Nullable String getPermission() {
        return "minigame.set.defaultwinner";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Minigame minigame,
                             @NotNull String @Nullable [] args) {
        if (args != null) {
            TeamsModule teamsModule = TeamsModule.getMinigameModule(minigame);

            if (teamsModule != null) {
                TeamColor teamColor = TeamColor.matchColor(args[0]);

                if (teamColor != null) {
                    teamsModule.setDefaultWinner(teamColor);
                    MessageManager.sendMessage(sender, MinigameMessageType.SUCCESS, MgCommandLangKey.COMMAND_SET_DEFAULTWINNER_SUCCESS,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                        Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), teamColor.getCompName()));
                } else {
                    MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTTEAM,
                        Placeholder.unparsed(MinigamePlaceHolderKey.TEAM.getKey(), args[0]),
                        Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), TeamColor.inputColorNamesComp(List.of(TeamColor.values()))));
                }
                return true;
            } else {
                MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTGAMEMECHANIC,
                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                    Placeholder.unparsed(MinigamePlaceHolderKey.TYPE.getKey(), MgDefaultModules.TEAMS.getKey().value()));
            }
        }
        return false;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(@NotNull CommandSender sender, @NotNull Minigame minigame,
                                                         @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            TeamsModule teamsModule = TeamsModule.getMinigameModule(minigame);

            if (teamsModule != null) {
                return CommandDispatcher.tabCompleteMatch(teamsModule.getTeams().stream().map(t ->
                    t.getColor().name().toLowerCase(Locale.ENGLISH)).toList(), args[0]);
            }
        }
        return null;
    }

}
