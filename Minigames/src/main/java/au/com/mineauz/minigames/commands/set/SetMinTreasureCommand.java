package au.com.mineauz.minigames.commands.set;

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

import java.util.List;

public class SetMinTreasureCommand extends ASetCommand {

    @Override
    public @NotNull String getName() {
        return "mintreasure";
    }

    @Override
    public boolean canBeConsole() {
        return true;
    }

    @Override
    public @NotNull Component getDescription() {
        return MinigameMessageManager.getMgMessage(MgCommandLangKey.COMMAND_SET_MINTREASURE_DESCRIPTION);
    }

    @Override
    public @NotNull Component getUsage() {
        return MinigameMessageManager.getMgMessage(MgCommandLangKey.COMMAND_SET_MINTREASURE_USAGE);
    }

    @Override
    public @Nullable String getPermission() {
        return "minigame.set.mintreasure";
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Minigame minigame,
                             final @NotNull String @Nullable [] args) {
        if (args != null) {
            if (minigame.getMechanic() instanceof final @NotNull TreasureHuntMechanic treasureHuntMechanic) {
                if (args[0].matches("[0-9]+")) {
                    final int amount = Integer.parseInt(args[0]); // todo error handling
                    treasureHuntMechanic.setMinTreasure(amount);

                    MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.SUCCESS, MgCommandLangKey.COMMAND_SET_MINTREASURE_SUCCESS,
                            Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                            Placeholder.unparsed(MinigamePlaceHolderKey.MIN.getKey(), String.valueOf(amount)));
                    return true;
                } else {
                    MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTNUMBER,
                            Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), args[0]));
                }
            } else {
                MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTGAMEMECHANIC,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                        Placeholder.unparsed(MinigamePlaceHolderKey.MECHANIC.getKey(), GameMechanicRegistry.MgDefaultMechanic.TREASURE_HUNT.getKey().value()));
            }
        }
        return false;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(@NotNull CommandSender sender, @NotNull Minigame minigame,
                                                         @NotNull String @NotNull [] args) {
        return null;
    }
}
