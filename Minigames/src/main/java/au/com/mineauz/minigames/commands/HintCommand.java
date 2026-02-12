package au.com.mineauz.minigames.commands;

import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.mechanics.TreasureHuntMechanic;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class HintCommand extends ACommand { //todo make subcommands for all treasure hunt ones e.a. /minigames tr hint;  /minigames tr maxheight etc.

    @Override
    public @NotNull String getName() {
        return "hint";
    }

    @Override
    public boolean canBeConsole() {
        return false;
    }

    @Override
    public @NotNull Component getDescription() {
        return MessageManager.getMessage(MgCommandLangKey.COMMAND_HINT_DESCRIPTION);
    }

    @Override
    public @NotNull Component getUsage() {
        return MessageManager.getMessage(MgCommandLangKey.COMMAND_HINT_USAGE);
    }

    @Override
    public @Nullable String getPermission() {
        return "minigame.treasure.hint";
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender,
                             final @NotNull String @NotNull [] args) {
        if (sender instanceof Player player) {
        final @NotNull MinigamePlayer mgPlayer = PLUGIN.getPlayerManager().getMinigamePlayer(player);
            if (args.length > 0) {
                final @Nullable Minigame minigame = PLUGIN.getMinigameManager().getMinigame(args[0]);

                if (minigame != null && minigame.getMinigameTimer() != null && minigame.getType() == MinigameType.GLOBAL &&
                    minigame.getMechanic() instanceof final @NotNull TreasureHuntMechanic treasureHuntMechanic) {
                    if (treasureHuntMechanic.hasTreasureLocation() && !treasureHuntMechanic.isTreasureFound()) {
                        treasureHuntMechanic.getHints(mgPlayer);
                    } else {
                        MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOTSTARTED,
                            Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()));
                    }
                } else if (minigame == null || minigame.getType() != MinigameType.GLOBAL) {
                    MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_HINT_ERROR_NOTTREASUREHUNT,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), args[0]));
                }
            } else {
                final @NotNull SequencedMap<@NotNull Minigame, @NotNull TreasureHuntMechanic> minigames = new LinkedHashMap<>();
                for (final @NotNull Minigame minigame : PLUGIN.getMinigameManager().getAllMinigames().values()) {
                    if (minigame.getType() == MinigameType.GLOBAL && minigame.getMechanic() instanceof final @NotNull TreasureHuntMechanic treasureHuntMechanic) {
                        minigames.put(minigame, treasureHuntMechanic);
                    }
                }
                if (!minigames.isEmpty()) {
                    if (minigames.size() > 1) {
                        MessageManager.sendMessage(sender, MinigameMessageType.INFO, MgCommandLangKey.COMMAND_HINT_LISTHUNTS,
                            Placeholder.component(MinigamePlaceHolderKey.TEXT.getKey(),
                                Component.join(JoinConfiguration.commas(true), minigames.keySet().stream().map(Minigame::getDisplayName).toList())));

                    } else {
                        final @NotNull Map.Entry<@NotNull Minigame, @NotNull TreasureHuntMechanic> first = minigames.firstEntry();

                        if (first.getValue().hasTreasureLocation() && !first.getValue().isTreasureFound()) {
                            first.getValue().getHints(mgPlayer);
                        } else {
                            MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOTSTARTED,
                                Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(),first.getKey().getName()));
                        }
                    }
                } else {
                    MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_HINT_ERROR_NORUNNING);
                }
            }
        } else {
            MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAD_ERROR_NOTPLAYER);
        }
        return true;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(final @NotNull CommandSender sender,
                                                         final @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            final @NotNull List<String> mgs = new ArrayList<>();
            for (final @NotNull Minigame minigame : PLUGIN.getMinigameManager().getAllMinigames().values()) {
                if (minigame.getType() == MinigameType.GLOBAL && minigame.getMechanic() instanceof TreasureHuntMechanic) {
                    mgs.add(minigame.getName());
                }
            }
            return CommandDispatcher.tabCompleteMatch(mgs, args[0]);
        }
        return null;
    }
}
