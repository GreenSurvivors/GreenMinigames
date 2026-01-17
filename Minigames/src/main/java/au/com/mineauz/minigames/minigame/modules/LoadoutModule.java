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
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LoadoutModule extends MinigameModule {
    private static final @NotNull Map<@NotNull Key, @NotNull ILoadoutAddonFactory> registeredAddons = new HashMap<>();
    private static final @NotNull Map<String, @NotNull PlayerLoadout> globalLoadouts = new HashMap<>();

    private final @NotNull Map<@NotNull String, @NotNull PlayerLoadout> loadouts = new HashMap<>();

    public LoadoutModule(@NotNull Minigame mgm, @NotNull String name) {
        super(mgm, name);
        PlayerLoadout defaultLoadout = new PlayerLoadout("default");
        registeredAddons.values().forEach(defaultLoadout::registerAddon);
        defaultLoadout.setDeletable(false);
        loadouts.put("default", defaultLoadout);
    }

    public static @Nullable LoadoutModule getMinigameModule(@NotNull Minigame mgm) {
        return ((LoadoutModule) mgm.getModule(MgModules.LOADOUT.getName()));
    }

    /**
     * Registers a loadout addonFactory. This addonFactory will be creating a new addon for all newly created loadouts on all games.
     *
     * @param addonFactory  The factory producing our addon to register
     */
    public static @Nullable ILoadoutAddonFactory registerAddon(final @NotNull ILoadoutAddonFactory addonFactory) {
        final @Nullable ILoadoutAddonFactory replacedFactory = registeredAddons.put(addonFactory.getKey(), addonFactory);
        globalLoadouts.values().forEach( gl -> gl.registerAddon(addonFactory));

        for (final @NotNull Minigame minigame : Minigames.getPlugin().getMinigameManager().getAllMinigames().values()) {
            final @Nullable LoadoutModule module = getMinigameModule(minigame);

            if (module != null) {
                for (final @NotNull PlayerLoadout loadout : module.loadouts.values()) {
                    loadout.registerAddon(addonFactory);
                }
            }
        }

        return replacedFactory;
    }

    /**
     * Unregisters a previously registered addon
     *
     * @param loadoutAddonKey The addon to unregister
     */
    public static boolean unregisterAddon(final @NotNull Key loadoutAddonKey) {
        final ILoadoutAddonFactory removed = registeredAddons.remove(loadoutAddonKey);
        globalLoadouts.values().forEach( gl -> gl.unregisterAddon(loadoutAddonKey));

        for (final @NotNull Minigame minigame : Minigames.getPlugin().getMinigameManager().getAllMinigames().values()) {
            final @Nullable LoadoutModule module = getMinigameModule(minigame);

            if (module != null) {
                for (final @NotNull PlayerLoadout loadout : getMinigameModule(minigame).loadouts.values()) {
                    loadout.unregisterAddon(loadoutAddonKey);
                }
            }
        }

        return removed != null;
    }

    /**
     * Retrieves a registered addon
     *
     * @param addonKey The addon name to get the addon for
     * @return The addon or null
     */
    public static @Nullable ILoadoutAddonFactory getAddonFactory(final @NotNull Key addonKey) {
        return registeredAddons.get(addonKey);
    }

    public static @NotNull Collection<@NotNull ILoadoutAddonFactory> getAllAddonFactories(){
        return registeredAddons.values();
    }

    @Override
    public boolean useSeparateConfig() {
        return false;
    }

    @Override
    public void save(final @NotNull FileConfiguration config, final @NotNull String path) {
        char configSeparator = config.options().pathSeparator();
        LoadoutFlag loadoutFlag;
        for (Map.Entry<String, PlayerLoadout> loadoutEntry : loadouts.entrySet()) {
            loadoutFlag = new LoadoutFlag(loadoutEntry.getKey(), loadoutEntry.getValue());
            loadoutFlag.saveValue(config, path + configSeparator + "loadouts");
        }
    }

    @Override
    public void load(final @NotNull FileConfiguration config, final @NotNull String path) {
        char configSeparator = config.options().pathSeparator();
        final ConfigurationSection configSection = config.getConfigurationSection(path + configSeparator + "loadouts");
        if (configSection != null) {
            LoadoutFlag loadoutFlag;

            for (String loadout : configSection.getKeys(false)) {
                loadoutFlag = new LoadoutFlag(loadout, new PlayerLoadout(loadout));
                if (loadout.equals("default")) {
                    loadoutFlag.getFlag().setDeletable(false);
                }
                loadoutFlag.loadValue(config, path + configSeparator + getName().toLowerCase());
                loadouts.put(loadoutFlag.getName(), loadoutFlag.getFlag());
            }
        }

        if (config.contains(path + configSeparator + configSeparator + "loadout")) {
            Minigames.getPlugin().getLogger().warning(config.getCurrentPath() + " contains unsupported configurations: " + path + configSeparator + "loadout");
        }
        if (config.contains(path + configSeparator + "extraloadouts")) {
            Minigames.getPlugin().getLogger().warning(config.getCurrentPath() + " contains unsupported configurations: " + path + configSeparator + "extraloadouts");
        }
    }

    public static void addGlobalLoadout(final @NotNull String name) {
        globalLoadouts.put(name, new PlayerLoadout(name));
    }

    public static void deleteGlobalLoadout(final @NotNull String name) {
        globalLoadouts.remove(name);
    }

    public static @NotNull List<@NotNull PlayerLoadout> getGlobalLoadouts() {
        return new ArrayList<>(globalLoadouts.values());
    }

    public static @NotNull Map<@NotNull String, @NotNull PlayerLoadout> getGlobalLoadoutMap() {
        return globalLoadouts;
    }

    public static @Nullable PlayerLoadout getGlobalLoadout(final @NotNull String name) {
        return globalLoadouts.get(name);
    }

    public static boolean hasGlobalLoadouts() {
        return !globalLoadouts.isEmpty();
    }

    public static boolean hasGlobalLoadout(final @NotNull String name) {
        return globalLoadouts.containsKey(name);
    }

    public void addLoadout(@NotNull String name) {
        loadouts.put(name, new PlayerLoadout(name));
    }

    public void deleteLoadout(String name) {
        loadouts.remove(name);
    }

    public @NotNull Set<@NotNull String> getLoadoutNames() {
        return loadouts.keySet();
    }

    public @NotNull Set<@NotNull PlayerLoadout> getLoadouts() {
        return new HashSet<>(loadouts.values());
    }

    public @NotNull Map<@NotNull String, @NotNull PlayerLoadout> getLoadoutMap() {
        return loadouts;
    }

    public @Nullable PlayerLoadout getLoadout(final @NotNull String name) {
        if (loadouts.containsKey(name)) {
            return loadouts.get(name);
        } else {
            for (String loadout : loadouts.keySet()) {
                if (loadout.equalsIgnoreCase(name)) {
                    return loadouts.get(loadout);
                }
            }
        }
        return null;
    }

    public boolean hasLoadouts() {
        return !loadouts.isEmpty();
    }

    public boolean hasLoadout(final @NotNull String name) {
        if (!name.equalsIgnoreCase("default")) {
            if (loadouts.containsKey(name)) {
                return loadouts.containsKey(name);
            } else {
                for (String loadout : loadouts.keySet()) {
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

    public void displaySelectionMenu(final @NotNull MinigamePlayer mgPlayer, final boolean equip) {
        Menu m = new Menu(6, MgMenuLangKey.MENU_LOADOUT_SELECT_NAME, mgPlayer);

        for (final PlayerLoadout loadout : loadouts.values()) {
            if (loadout.isDisplayedInMenu()) {
                if (!loadout.getUsePermissions() || mgPlayer.getPlayer().hasPermission("minigame.loadout." + loadout.getName().toLowerCase())) {
                    if (mgPlayer.isInMinigame() && !mgPlayer.getMinigame().isTeamGame() || loadout.getTeamColor() == null ||
                            mgPlayer.getTeam().getColor() == loadout.getTeamColor()) {
                        MenuItemCustom c = new MenuItemCustom(Material.GLASS, loadout.getDisplayName());
                        if (!loadout.getItemSlots().isEmpty()) {
                            ItemStack item = loadout.getItem(new ArrayList<>(loadout.getItemSlots()).getFirst());
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
}
