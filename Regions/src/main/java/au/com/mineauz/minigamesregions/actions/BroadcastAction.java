package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.EnumFlag;
import au.com.mineauz.minigames.config.StringFlag;
import au.com.mineauz.minigames.managers.MinigameMessageManager;
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
import au.com.mineauz.minigamesregions.RegionMessageManager;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class BroadcastAction extends AAction {
    private final StringFlag message = new StringFlag("Hello World", "message");
    private final BooleanFlag excludeExecutor = new BooleanFlag(false, "exludeExecutor");
    private final EnumFlag<MinigameMessageType> messageType = new EnumFlag<>(MinigameMessageType.INFO, "messageType");

    protected BroadcastAction(@NotNull String name) {
        super(name);
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
    public @NotNull Map<@NotNull Component, @Nullable ComponentLike> describe() {
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
    public void executeRegionAction(final @Nullable MinigamePlayer mgPlayer, final @NotNull Region region) {
        ScriptObject base = new ScriptObject() {
            @Override
            public Set<String> getKeys() {
                return Set.of("player", "area", "minigame", "team");
            }

            @Override
            public String getAsString() {
                return "";
            }

            @Override
            public ScriptReference get(String name) {
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
            public Set<String> getKeys() {
                return Set.of("player", "area", "minigame", "team");
            }

            @Override
            public String getAsString() {
                return "";
            }

            @Override
            public ScriptReference get(String name) {
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

    private void execute(@Nullable MinigamePlayer mgPlayer, @NotNull ScriptObject base) {
        MinigamePlayer exclude = null;
        if (excludeExecutor.getFlag()) {
            exclude = mgPlayer;
        }

        // Old replacement
        String message = this.message.getFlag();
        if (mgPlayer != null) {
            message = message.replace("%player%", mgPlayer.getDisplayName(mgPlayer.getMinigame().usePlayerDisplayNames()));
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
    public void saveArguments(@NotNull FileConfiguration config, @NotNull String path) {
        message.saveValue(config, path);
        excludeExecutor.saveValue(config, path);
        messageType.saveValue(config, path);

        // dataFixerUpper
        config.set(path + ".redText", null);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config, @NotNull String path) {
        message.loadValue(config, path);
        excludeExecutor.loadValue(config, path);

        // dataFixerUpper
        if (config.getBoolean(path + ".redText")) {
            messageType.setFlag(MinigameMessageType.ERROR);
        } else {
            messageType.loadValue(config, path);
        }
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer mgPlayer, Menu previous) {
        Menu menu = new Menu(3, getDisplayname(), mgPlayer);
        menu.addItem(new MenuItemBack(previous), menu.getSize() - 9);

        menu.addItem(message.getMenuItem(Material.NAME_TAG, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_BROADCAST_MESSAGE_NAME)));
        menu.addItem(excludeExecutor.getMenuItem(Material.ENDER_PEARL, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_BROADCAST_EXCLUDING_NAME)));
        menu.addItem(messageType.getMenuItem(Material.ENDER_PEARL, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_BROADCAST_MSGTYPE_NAME)));

        menu.displayMenu(mgPlayer);
        return true;
    }
}
