package au.com.mineauz.minigames.commands.set;

import au.com.mineauz.minigames.commands.CommandDispatcher;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.apache.commons.lang3.BooleanUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SetDisplayScoreboardCommand extends ASetCommand { //todo allow sidebar, below name all of the vanilla stuff

    @Override
    public @NotNull String getName() {
        return "displayscoreboard";
    }

    @Override
    public @NotNull String @Nullable [] getAliases() {
        return new String[]{
                "showscoreboard",
                "dispscore",
                "displayscore",
                "showscore"
        };
    }

    @Override
    public boolean canBeConsole() {
        return true;
    }

    @Override
    public @NotNull Component getDescription() {
        return MinigameMessageManager.getMgMessage(MgCommandLangKey.COMMAND_SET_DISPLAYSCOREBOARD_DESCRIPTION);
    }

    @Override
    public @NotNull Component getUsage() {
        return MinigameMessageManager.getMgMessage(MgCommandLangKey.COMMAND_SET_DISPLAYSCOREBOARD_USAGE);
    }

    @Override
    public @Nullable String getPermission() {
        return "minigame.set.displayscoreboard";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Minigame minigame,
                             @NotNull String @Nullable [] args) {
        if (args != null) {
            Boolean bool = BooleanUtils.toBooleanObject(args[0]);

            if (bool != null) {
                minigame.setDisplayScoreboard(bool);

                if (bool) {
                    MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.SUCCESS,
                            MgCommandLangKey.COMMAND_SET_DISPLAYSCOREBOARD_SUCCESS,
                            Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()));
                } else {
                    MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.INFO,
                            MgCommandLangKey.COMMAND_SET_DISPLAYSCOREBOARD_REMOVED,
                            Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()));
                }
                return true;
            } else {
                MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTBOOL,
                        Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), args[0]));
            }
        }
        return false;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(@NotNull CommandSender sender, @NotNull Minigame minigame,
                                                         @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return CommandDispatcher.tabCompleteMatch(List.of("true", "false"), args[0]);
        }
        return null;
    }
}
