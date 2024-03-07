package au.com.mineauz.minigames.minigame.modules;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.PlayerLoadout;
import au.com.mineauz.minigames.config.LoadoutFlag;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemCustom;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LoadoutModule extends MinigameModule {
    private static final Map<Class<? extends LoadoutAddon>, LoadoutAddon<?>> addons = new HashMap<>();
    private final Map<String, PlayerLoadout> extraLoadouts = new HashMap<>();
    //private final LoadoutSetFlag loadoutsFlag = new LoadoutSetFlag(extraLoadouts, "loadouts");

    public LoadoutModule(@NotNull Minigame mgm, @NotNull String name) {
        super(mgm, name);
        PlayerLoadout defaultLoadout = new PlayerLoadout("default");
        defaultLoadout.setDeletable(false);
        extraLoadouts.put("default", defaultLoadout);
    }

    public static @Nullable LoadoutModule getMinigameModule(@NotNull Minigame mgm) {
        return ((LoadoutModule) mgm.getModule(MgModules.LOADOUT.getName()));
    }

    /**
     * Registers a loadout addon. This addon will be available for all loadouts on all games.
     *
     * @param plugin The plugin registering the addon
     * @param addon  The addon to register
     */
    public static void registerAddon(Plugin plugin, LoadoutAddon<?> addon) {
        addons.put(addon.getClass(), addon);
    }

    /**
     * Unregisters a previously registered addon
     *
     * @param addon The addon to unregister
     */
    public static void unregisterAddon(Class<? extends LoadoutAddon<?>> addon) {
        addons.remove(addon);
    }

    /**
     * Retrieves a registered addon
     *
     * @param addonClass The addon class to get the addon for
     * @return The addon or null
     */
    @SuppressWarnings("unchecked")
    public static <T extends LoadoutAddon<?>> T getAddon(Class<T> addonClass) {
        return (T) addons.get(addonClass);
    }

    public static void addAddonMenuItems(Menu menu, PlayerLoadout loadout) {
        for (LoadoutAddon<?> addon : addons.values()) {
            addon.addMenuOptions(menu, loadout);
        }
    }

    @Override
    public boolean useSeparateConfig() {
        return false;
    }

    @Override
    public void save(@NotNull FileConfiguration config, @NotNull String path) {
        LoadoutFlag loadoutFlag;
        for (Map.Entry<String, PlayerLoadout> loadoutEntry : extraLoadouts.entrySet()) {
            loadoutFlag = new LoadoutFlag(loadoutEntry.getValue(), loadoutEntry.getKey());
            loadoutFlag.saveValue(config, path + ".loadouts");
        }
    }

    @Override
    public void load(@NotNull FileConfiguration config, @NotNull String path) {
        final ConfigurationSection configSection = config.getConfigurationSection(path + ".loadouts");
        if (configSection != null) {
            LoadoutFlag loadoutFlag;

            for (String loadout : configSection.getKeys(false)) {
                loadoutFlag = new LoadoutFlag(new PlayerLoadout(loadout), loadout);
                if (loadout.equals("default")) {
                    loadoutFlag.getFlag().setDeletable(false);
                }
                loadoutFlag.loadValue(config, path + "." + getName().toLowerCase());
                extraLoadouts.put(loadoutFlag.getName(), loadoutFlag.getFlag());
            }
        }

        if (config.contains(path + ".loadout")) {
            Minigames.getPlugin().getLogger().warning(config.getCurrentPath() + " contains unsupported configurations: " + path + ".loadout");
        }
        if (config.contains(path + ".extraloadouts")) {
            Minigames.getPlugin().getLogger().warning(config.getCurrentPath() + " contains unsupported configurations: " + path + ".extraloadouts");
        }
    }

    public void addLoadout(String name) {
        extraLoadouts.put(name, new PlayerLoadout(name));
    }

    public void deleteLoadout(String name) {
        extraLoadouts.remove(name);
    }

    public Set<String> getLoadoutNames() {
        return extraLoadouts.keySet();
    }

    public Set<PlayerLoadout> getLoadouts() {
        return new HashSet<>(extraLoadouts.values());
    }

    public Map<String, PlayerLoadout> getLoadoutMap() {
        return extraLoadouts;
    }

    public @Nullable PlayerLoadout getLoadout(@NotNull String name) {
        if (extraLoadouts.containsKey(name)) {
            return extraLoadouts.get(name);
        } else {
            for (String loadout : extraLoadouts.keySet()) {
                if (loadout.equalsIgnoreCase(name)) {
                    return extraLoadouts.get(loadout);
                }
            }
        }
        return null;
    }

    public boolean hasLoadouts() {
        return !extraLoadouts.isEmpty();
    }

    public boolean hasLoadout(String name) {
        if (!name.equalsIgnoreCase("default")) {
            if (extraLoadouts.containsKey(name)) {
                return extraLoadouts.containsKey(name);
            } else {
                for (String loadout : extraLoadouts.keySet()) {
                    if (loadout.equalsIgnoreCase(name)) {
                        return true;
                    }
                }
                return false;
            }
        } else {
            return true;
        }
    }

    public void displaySelectionMenu(final MinigamePlayer mgPlayer, final boolean equip) {
        Menu m = new Menu(6, MgMenuLangKey.MENU_LOADOUT_SELECT_NAME, mgPlayer);

        for (final PlayerLoadout loadout : extraLoadouts.values()) {
            if (loadout.isDisplayedInMenu()) {
                if (!loadout.getUsePermissions() || mgPlayer.getPlayer().hasPermission("minigame.loadout." + loadout.getName().toLowerCase())) {
                    if (!mgPlayer.getMinigame().isTeamGame() || loadout.getTeamColor() == null ||
                            mgPlayer.getTeam().getColor() == loadout.getTeamColor()) {
                        MenuItemCustom c = new MenuItemCustom(Material.GLASS, loadout.getDisplayName());
                        if (!loadout.getItemSlots().isEmpty()) {
                            ItemStack item = loadout.getItem(new ArrayList<>(loadout.getItemSlots()).get(0));
                            c.setDisplayItem(item);
                        }
                        c.setClick(() -> {
                            mgPlayer.setLoadout(loadout);
                            mgPlayer.getPlayer().closeInventory();
                            if (!equip) {
                                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_LOADOUT_NEXTRESPAWN,
                                        Placeholder.component(MinigamePlaceHolderKey.LOADOUT.getKey(), loadout.getDisplayName()));
                            } else {
                                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_LOADOUT_EQUIPPED,
                                        Placeholder.component(MinigamePlaceHolderKey.LOADOUT.getKey(), loadout.getDisplayName()));
                                loadout.equipLoadout(mgPlayer);
                            }
                            return null;
                        });
                        m.addItem(c);
                    }
                }
            }
        }
        m.displayMenu(mgPlayer);
    }

    @Override
    public void addEditMenuOptions(@NotNull Menu menu) {
        // TODO Move loadout menu stuff here

    }

    @Override
    public boolean displayMechanicSettings(@NotNull Menu previous) {
        return false;
    }

    /**
     * Represents a custom loadout element.
     * This can be used to add things like disguises
     * or commands.
     *
     * @param <T> The value type for this loadout addon.arg1
     */
    public interface LoadoutAddon<T> {
        String getName();

        void addMenuOptions(Menu menu, PlayerLoadout loadout);

        void save(ConfigurationSection section, T value);

        T load(ConfigurationSection section);

        void applyLoadout(MinigamePlayer player, T value);

        void clearLoadout(MinigamePlayer player, T value);
    }
}
