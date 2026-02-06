package au.com.mineauz.minigames.commands.set;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.mechanics.GameMechanicRegistry;
import au.com.mineauz.minigames.mechanics.TreasureHuntMechanic;
import au.com.mineauz.minigames.minigame.Minigame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SetHintDelayCommand extends ASetCommand {

    @Override
    public @NotNull String getName() {
        return "hintdelay";
    }

    @Override
    public boolean canBeConsole() {
        return true;
    }

    @Override
    public @NotNull Component getDescription() {
        return MinigameMessageManager.getMgMessage(MgCommandLangKey.COMMAND_SET_HINTDELAY_DESCRIPTION);
    }

    @Override
    public @NotNull Component getUsage() {
        return MinigameMessageManager.getMgMessage(MgCommandLangKey.COMMAND_SET_HINTDELAY_USAGE);
    }

    @Override
    public @Nullable String getPermission() {
        return "minigame.set.hintdelay";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Minigame minigame,
                             @NotNull String @Nullable [] args) {
        if (args != null) {
            if (minigame.getMechanic() instanceof final @NotNull TreasureHuntMechanic treasureHuntMechanic) {
                Long millis = MinigameUtils.parsePeriod(args[0]);

                if (millis != null) {
                    treasureHuntMechanic.setHintDelay(TimeUnit.MILLISECONDS.toSeconds(millis));

                    MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.SUCCESS, MgCommandLangKey.COMMAND_SET_HINTDELAY_SUCCESS,
                            Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                            Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(Duration.ofMillis(millis))));
                    return true;
                } else {
                    MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTTIME,
                            Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), args[0]));
                }
            } else {
                MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTGAMEMECHANIC,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                        Placeholder.unparsed(MinigamePlaceHolderKey.TYPE.getKey(), GameMechanicRegistry.MgDefaultMechanic.TREASURE_HUNT.getKey().value()));
            }
        }
        return false;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(@NotNull CommandSender sender, @NotNull Minigame minigame,
                                                         @NotNull String @NotNull [] args) {
        return List.of("s", "m", "h");
    }
}
