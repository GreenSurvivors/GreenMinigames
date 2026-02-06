package au.com.mineauz.minigames.commands.set;

import au.com.mineauz.minigames.commands.CommandDispatcher;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.mechanics.GameMechanicRegistry;
import au.com.mineauz.minigames.mechanics.IGameMechanicFactory;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamesKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SetGameMechanicCommand extends ASetCommand {

    @Override
    public @NotNull String getName() {
        return "gamemechanic";
    }

    @Override
    public @NotNull String @Nullable [] getAliases() {
        return new String[]{"scoretype", "mech", "gamemech", "mechanic"};
    }

    @Override
    public boolean canBeConsole() {
        return true;
    }

    @Override
    public @NotNull Component getDescription() {
        return MinigameMessageManager.getMgMessage(MgCommandLangKey.COMMAND_SET_GAMEMECHANIC_DESCRIPTION);
    }

    @Override
    public @NotNull Component getUsage() {
        return MinigameMessageManager.getMgMessage(MgCommandLangKey.COMMAND_SET_GAMEMECHANIC_USAGE);
    }

    @Override
    public @Nullable String getPermission() {
        return "minigame.set.gamemechanic";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Minigame minigame,
                             @NotNull String @Nullable [] args) {
        if (args != null) {
            final @Nullable Key mechanicKey = MinigamesKey.fromString(args[0]);

            if (mechanicKey != null) {
                final @Nullable IGameMechanicFactory mechanicFactory = GameMechanicRegistry.getMechanicFactory(mechanicKey);

                if (mechanicFactory != null) {
                    minigame.setMechanic(mechanicFactory.makeNewMechanic(PLUGIN, minigame));
                    MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.SUCCESS, MgCommandLangKey.COMMAND_SET_GAMEMECHANIC_SUCCESS,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                        Placeholder.unparsed(MinigamePlaceHolderKey.TYPE.getKey(), args[0]));
                    return true;
                }
            }

            MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTGAMEMECHANIC,
                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                    Placeholder.unparsed(MinigamePlaceHolderKey.TYPE.getKey(), args[0]));
        }
        return false;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(final @NotNull CommandSender sender, final @NotNull Minigame minigame,
                                                         final @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            final @NotNull List<@NotNull String> suggestions = new ArrayList<>();
            for (final @NotNull IGameMechanicFactory iGameMechanicFactory : GameMechanicRegistry.getAllFactories()) {
                final @NotNull Key key = iGameMechanicFactory.getKey();
                suggestions.add(key.asString());
                suggestions.add(key.asMinimalString());
            }
            return CommandDispatcher.tabCompleteMatch(suggestions, args[0]);
        }
        return null;
    }
}
