package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.EnumFlag;
import au.com.mineauz.minigames.config.StringFlag;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.script.ExpressionParser;
import au.com.mineauz.minigames.script.ScriptObject;
import au.com.mineauz.minigames.script.ScriptReference;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Map;
import java.util.Set;

public class BroadcastAction extends AAction {
    private final StringFlag message = new StringFlag("message", "Hello World");
    private final BooleanFlag excludeExecutor = new BooleanFlag("exludeExecutor", false);
    private final EnumFlag<MinigameMessageType> messageType = new EnumFlag<>("messageType", MinigameMessageType.INFO);

    protected BroadcastAction(final @NotNull Key key) {
        super(key);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_BROADCAST_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.MINIGAME;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_BROADCAST_MESSAGE_NAME),
                MinigameUtils.limitIgnoreFormat(MiniMessage.miniMessage().deserialize(message.getFlag()), 16),

                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_BROADCAST_EXCLUDING_NAME),
                MinigameMessageManager.getMgMessage(
                        excludeExecutor.getFlag() ? MgCommandLangKey.COMMAND_STATE_ENABLED : MgCommandLangKey.COMMAND_STATE_DISABLED),

                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_BROADCAST_MSGTYPE_NAME),
                Component.text(messageType.getFlag().toString()));
    }

    @Override
    public boolean useInRegions() {
        return true;
    }

    @Override
    public boolean useInNodes() {
        return true;
    }

    @Override //todo datafixerupper
    public void executeRegionAction(final @NotNull MinigamePlayer mgPlayer, final @NotNull Region region) {
        ScriptObject base = new ScriptObject() {
            @Override
            public @NotNull Set<String> getReferenceKeys() {
                return Set.of("player", "area", "minigame", "team");
            }

            @Override
            public String getAsString() { // todo
                return "";
            }

            @Override
            public @Nullable ScriptReference resolveReference(@NotNull String name) {
                if (name.equalsIgnoreCase("player")) {
                    return mgPlayer;
                } else if (name.equalsIgnoreCase("area")) {
                    return region;
                } else if (name.equalsIgnoreCase("minigame")) {
                    return mgPlayer.getMinigame();
                } else if (name.equalsIgnoreCase("team")) {
                    return mgPlayer.getTeam();
                }

                return null;
            }
        };
        debug(mgPlayer, base);
        execute(mgPlayer, base);
    }

    @Override
    public void executeNodeAction(final @NotNull MinigamePlayer mgPlayer, final @NotNull Node node) {
        ScriptObject base = new ScriptObject() {
            @Override
            public @NotNull Set<String> getReferenceKeys() {
                return Set.of("player", "area", "minigame", "team");
            }

            @Override
            public String getAsString() { // todo
                return "";
            }

            @Override
            public @Nullable ScriptReference resolveReference(@NotNull String name) {
                if (name.equalsIgnoreCase("player")) {
                    return mgPlayer;
                } else if (name.equalsIgnoreCase("area")) {
                    return node;
                } else if (name.equalsIgnoreCase("minigame")) {
                    return mgPlayer.getMinigame();
                } else if (name.equalsIgnoreCase("team")) {
                    return mgPlayer.getTeam();
                }

                return null;
            }
        };
        debug(mgPlayer, base);
        execute(mgPlayer, base);
    }

    private void execute(final @Nullable MinigamePlayer mgPlayer, @NotNull ScriptObject base) {
        MinigamePlayer exclude = null;
        if (excludeExecutor.getFlag()) {
            exclude = mgPlayer;
        }

        // Old replacement
        String message = this.message.getFlag();
        if (mgPlayer != null) {
            message = message.replace("%player%", mgPlayer.getName());
        }
        // New expression system
        message = ExpressionParser.stringResolve(message, base, true, true);
        if (exclude != null) {
            MinigameMessageManager.sendMinigameMessage(mgPlayer.getMinigame(), MiniMessage.miniMessage().deserialize(message), messageType.getFlag(), exclude);
        } else {
            MinigameMessageManager.sendMinigameMessage(mgPlayer.getMinigame(), MiniMessage.miniMessage().deserialize(message), messageType.getFlag());
        }

    }

    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        message.saveValue(config);
        excludeExecutor.saveValue(config);
        messageType.saveValue(config);

        // dataFixerUpper
        config.removeChild("redText");
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
        message.loadValue(config);
        excludeExecutor.loadValue(config);

        // dataFixerUpper
        if (config.node("redText").getBoolean(false)) {
            messageType.setFlag(MinigameMessageType.ERROR);
        } else {
            messageType.loadValue(config);
        }
    }

    @Override
    public boolean displayMenu(final @NotNull Menu previous) {
        final @NotNull Menu menu = new Menu(3, getDisplayname(), previous.getIntendedViewer());
        menu.addItem(new MenuItemBack(previous), menu.getSize() - 9);

        menu.addItem(message.getMenuItem(ItemType.NAME_TAG, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_BROADCAST_MESSAGE_NAME)));
        menu.addItem(excludeExecutor.getMenuItem(ItemType.ENDER_PEARL, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_BROADCAST_EXCLUDING_NAME)));
        menu.addItem(messageType.getMenuItem(ItemType.ENDER_PEARL, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_BROADCAST_MSGTYPE_NAME)));

        menu.displayMenu();
        return true;
    }
}
