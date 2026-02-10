package au.com.mineauz.minigames.commands;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ToggleTimerCommand extends ACommand {

    @Override
    public @NotNull String getName() {
        return "toggletimer";
    }

    @Override
    public boolean canBeConsole() {
        return true;
    }

    @Override
    public @NotNull Component getDescription() {
        return MinigameMessageManager.getMgMessage(MgCommandLangKey.COMMAND_TOGGLETIMER_DESCRIPTION);
    }

    @Override
    public @NotNull Component getUsage() {
        return MinigameMessageManager.getMgMessage(MgCommandLangKey.COMMAND_TOGGLETIMER_USAGE);
    }

    @Override
    public @Nullable String getPermission() {
        return "minigame.toggletimer";
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull String @NotNull [] args) {
        if (args.length > 0) {
            final @Nullable Minigame minigame = PLUGIN.getMinigameManager().getMinigame(args[0]);
            if (minigame != null) {
                if (minigame.getMultiplayerTimer() != null) {
                    if (minigame.getMultiplayerTimer().isPaused()) {
                        minigame.getMultiplayerTimer().resumeTimer();

                        MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.SUCCESS, MgCommandLangKey.COMMAND_TOGGLETIMER_RESUME_SUCCESS,
                                Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()));
                    } else {
                        // message to players of Minigame
                        minigame.getMultiplayerTimer().pauseTimer(MinigameMessageManager.getMgMessage(MgCommandLangKey.COMMAND_TOGGLETIMER_PAUSE_MSG,
                                Placeholder.unparsed(MinigamePlaceHolderKey.PLAYER.getKey(), sender.getName())));
                        // message to sender
                        MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.SUCCESS, MgCommandLangKey.COMMAND_TOGGLETIMER_PAUSE_SUCCESS,
                                Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                                Placeholder.unparsed(MinigamePlaceHolderKey.TIME.getKey(), String.valueOf(minigame.getMultiplayerTimer().getPlayerWaitTimeLeft())));
                    }
                } else {
                    MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_TOGGLETIMER_ERROR_NOTIMER,
                            Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()));
                }
            } else {
                MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOMINIGAME,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), args[0]));
            }
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(final @NotNull CommandSender sender, final @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            List<String> mgs = new ArrayList<>(PLUGIN.getMinigameManager().getAllMinigames().keySet());
            return CommandDispatcher.tabCompleteMatch(mgs, args[0]);
        }

        return null;
    }
}
