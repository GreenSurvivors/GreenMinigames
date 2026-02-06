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

public class SetLocationHintCommand extends ASetCommand { //todo i feel like this should be better identifiable:it isn't intuitive, that this belongs to treasure hunt

    @Override
    public @NotNull String getName() {
        return "locationHint";
    }

    @Override
    public boolean canBeConsole() {
        return true;
    }

    @Override
    public @NotNull String @Nullable [] getAliases() {
        return new String[]{"lh", "locHint"};
    }

    @Override
    public @NotNull Component getDescription() {
        return MinigameMessageManager.getMgMessage(MgCommandLangKey.COMMAND_SET_LOCATION_DESCRIPTION);
    }

    @Override
    public @NotNull Component getUsage() {
        return MinigameMessageManager.getMgMessage(MgCommandLangKey.COMMAND_SET_LOCATION_USAGE);
    }

    @Override
    public @Nullable String getPermission() {
        return "minigame.set.location";
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Minigame minigame,
                             final @NotNull String @Nullable [] args) {
        if (args != null) {
            if (minigame.getMechanic() instanceof final @NotNull TreasureHuntMechanic treasureHuntMechanic) {
                final @NotNull String locationName = String.join(" ", args);
                treasureHuntMechanic.setLocationName(locationName);

                MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.SUCCESS, MgCommandLangKey.COMMAND_SET_LOCATION_SUCCESS,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                        Placeholder.unparsed(MinigamePlaceHolderKey.LOCATION.getKey(), locationName));
            } else {
                MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTGAMEMECHANIC,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                        Placeholder.unparsed(MinigamePlaceHolderKey.TYPE.getKey(), GameMechanicRegistry.MgDefaultMechanic.TREASURE_HUNT.getKey().value()));
            }
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(@NotNull CommandSender sender, @NotNull Minigame minigame,
                                                         @NotNull String @NotNull [] args) {
        return null;
    }
}
