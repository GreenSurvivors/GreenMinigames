package au.com.mineauz.minigames.commands.set;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.commands.ICommand;
import au.com.mineauz.minigames.managers.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameLangKey;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.minigame.Minigame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.apache.commons.lang3.BooleanUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SetFlightCommand implements ICommand {

    @Override
    public @NotNull String getName() {
        return "flight";
    }

    @Override
    public @NotNull String @Nullable [] getAliases() {
        return new String[]{"fly"};
    }

    @Override
    public boolean canBeConsole() {
        return true;
    }

    @Override
    public @NotNull Component getDescription() {
        return MinigameMessageManager.getMgMessage(MinigameLangKey.COMMAND_SET_FLIGHT_DESCRIPTION);
    }

    @Override
    public Component getUsage() {
        return MinigameMessageManager.getMgMessage(MinigameLangKey.COMMAND_SET_FLIGHT_USAGE);
    }

    @Override
    public @Nullable String getPermission() {
        return "minigame.set.flight";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Minigame minigame,
                             @NotNull String label, @NotNull String @Nullable [] args) {
        if (args != null && args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "enabled" -> {
                    Boolean bool = BooleanUtils.toBooleanObject(args[1]);

                    if (bool != null) {
                        minigame.setAllowedFlight(bool);

                        MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.INFO, MinigameLangKey.COMMAND_SET_FLIGHT_ALLOWED,
                                Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName(false)),
                                Placeholder.component(MinigamePlaceHolderKey.STATE.getKey(), MinigameMessageManager.getMgMessage(
                                        bool ? MinigameLangKey.COMMAND_STATE_ENABLED : MinigameLangKey.COMMAND_STATE_DISABLED)));
                    } else {
                        MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.ERROR, MinigameLangKey.COMMAND_ERROR_NOTBOOL,
                                Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), args[0]));
                    }
                }
                case "startflying" -> {
                    Boolean bool = BooleanUtils.toBooleanObject(args[1]);

                    if (bool != null) {
                        minigame.setFlightEnabled(bool);

                        MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.INFO, MinigameLangKey.COMMAND_SET_FLIGHT_START,
                                Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName(false)),
                                Placeholder.component(MinigamePlaceHolderKey.STATE.getKey(), MinigameMessageManager.getMgMessage(
                                        bool ? MinigameLangKey.COMMAND_STATE_ENABLED : MinigameLangKey.COMMAND_STATE_DISABLED)));
                    } else {
                        MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.ERROR, MinigameLangKey.COMMAND_ERROR_NOTBOOL,
                                Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), args[0]));
                    }
                }

                default -> {
                    return false;
                }
            }

            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(@NotNull CommandSender sender, Minigame minigame,
                                                         String alias, @NotNull String @Nullable [] args) {
        if (args != null) {
            if (args.length == 1) {
                return MinigameUtils.tabCompleteMatch(List.of("enabled", "startflying"), args[0]);
            } else if (args.length == 2) {
                return MinigameUtils.tabCompleteMatch(List.of("true", "false"), args[1]);
            }
        }

        return null;
    }

}
