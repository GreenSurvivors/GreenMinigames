package au.com.mineauz.minigames.commands.set;

import au.com.mineauz.minigames.commands.CommandDispatcher;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.apache.commons.lang3.BooleanUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.BlockType;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SetBlockWhitelistCommand extends ASetCommand {

    @Override
    public @NotNull String getName() {
        return "blockwhitelist";
    }

    @Override
    public @NotNull String @Nullable [] getAliases() {
        return new String[]{"bwl", "blockwl"};
    }

    @Override
    public boolean canBeConsole() {
        return true;
    }

    @Override
    public @NotNull Component getDescription() {
        return MinigameMessageManager.getMgMessage(MgCommandLangKey.COMMAND_SET_WHITELIST_DESCRIPTION);
    }

    @Override
    public @NotNull Component getUsage() {
        return MinigameMessageManager.getMgMessage(MgCommandLangKey.COMMAND_SET_WHITELIST_USAGE);
    }

    @Override
    public @Nullable String getPermission() {
        return "minigame.set.blockwhitelist";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Minigame minigame,
                             @NotNull String @Nullable [] args) {
        if (args != null) {
            if (args[0].equalsIgnoreCase("add") && args.length >= 2) {
                final @Nullable Key key = NamespacedKey.fromString(args[1].toLowerCase(Locale.ROOT));

                if (key != null) {
                    final @Nullable BlockType blockType = Registry.BLOCK.get(key);

                    if (blockType != null) {
                        minigame.getRecorderData().addWBBlock(blockType);

                        MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.INFO, MgCommandLangKey.COMMAND_SET_WHITELIST_ADDED,
                            Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), Component.translatable(blockType.translationKey())),
                            Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()));
                        return true;
                    }
                }

                MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTBLOCKTYPE,
                    Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), args[1]));
            } else if (args[0].equalsIgnoreCase("remove") && args.length >= 2) {
                BlockType blockType = null;
                final @Nullable Key key = NamespacedKey.fromString(args[1].toLowerCase(Locale.ROOT));

                if (key != null) {
                    blockType = Registry.BLOCK.get(key);
                }

                if (blockType != null) {
                    minigame.getRecorderData().removeWBBlock(blockType);

                    MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.INFO, MgCommandLangKey.COMMAND_SET_WHITELIST_REMOVE,
                        Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), Component.translatable(blockType.translationKey())),
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()));
                } else {
                    MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTBLOCKTYPE,
                        Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), args[1]));
                }
            } else if (args[0].equalsIgnoreCase("clear")) {
                minigame.getRecorderData().getWBBlocks().clear();

                MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.INFO, MgCommandLangKey.COMMAND_SET_WHITELIST_CLEAR,
                    Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), MinigameMessageManager.getMgMessage(
                        minigame.getRecorderData().getWhitelistMode() ? MgMiscLangKey.CONFIG_WHITELIST : MgMiscLangKey.CONFIG_BLACKLIST)),
                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()));
            } else if (args[0].equalsIgnoreCase("list")) { //todo set list doesn't feel right
                String whiteListedBlocks = minigame.getRecorderData().getWBBlocks().stream().
                    map(type -> type.getKey().asMinimalString()).
                    collect(Collectors.joining("<gray>, </gray>"));

                MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.INFO, MgCommandLangKey.COMMAND_SET_WHITELIST_LIST,
                    Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), MinigameMessageManager.getMgMessage(
                        minigame.getRecorderData().getWhitelistMode() ? MgMiscLangKey.CONFIG_WHITELIST : MgMiscLangKey.CONFIG_BLACKLIST)),
                    Placeholder.parsed(MinigamePlaceHolderKey.TEXT.getKey(), whiteListedBlocks));
            } else {
                Boolean bool = BooleanUtils.toBooleanObject(args[0]);

                if (bool != null) {
                    minigame.getRecorderData().setWhitelistMode(bool);

                    MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.INFO, MgCommandLangKey.COMMAND_SET_WHITELIST_MODE,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                        Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), MinigameMessageManager.getMgMessage(
                            bool ? MgMiscLangKey.CONFIG_WHITELIST : MgMiscLangKey.CONFIG_BLACKLIST)));
                } else {
                    MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTBOOL,
                        Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), args[0]));
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(@NotNull CommandSender sender, @NotNull Minigame minigame,
                                                         @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return CommandDispatcher.tabCompleteMatch(List.of("true", "false", "add", "remove", "list", "clear"), args[0]);
        } else if (args.length == 2) {
            switch (args[0]) {
                case "add" -> {
                    List<String> ls = Registry.BLOCK.keyStream().flatMap(it -> Stream.of(it.key().asMinimalString(), it.key().asString())).toList();

                    return CommandDispatcher.tabCompleteMatch(ls, args[1]);
                }
                case "remove" -> {
                    List<String> ls = new ArrayList<>();
                    for (final @NotNull BlockType type : minigame.getRecorderData().getWBBlocks()) {
                        ls.add(type.getKey().asMinimalString());
                    }
                    return CommandDispatcher.tabCompleteMatch(ls, args[1]);
                }
            }
        }
        return null;
    }
}
