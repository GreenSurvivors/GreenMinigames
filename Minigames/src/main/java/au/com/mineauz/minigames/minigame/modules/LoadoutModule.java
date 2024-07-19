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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LoadoutModule extends MinigameModule {
    private static final Map<String, LoadoutAddonFactory> registeredAddons = new HashMap<>();
    private final Map<String, PlayerLoadout> loadouts = new HashMap<>();

    public LoadoutModule(@NotNull Minigame mgm, @NotNull String name) {
        super(mgm, name);
        PlayerLoadout defaultLoadout = new PlayerLoadout("default");
        defaultLoadout.setDeletable(false);
        loadouts.put("default", defaultLoadout);
    }

    public static @Nullable LoadoutModule getMinigameModule(@NotNull Minigame mgm) {
        return ((LoadoutModule) mgm.getModule(MgModules.LOADOUT.getName()));
    }

    /**
     * Registers a loadout addon. This addon will be available for all loadouts on all games.
     *
     * @param addonFactory  The addon to register
     */
    public @Nullable LoadoutAddonFactory registerAddon(@NotNull LoadoutAddonFactory addonFactory) {
        LoadoutAddonFactory replacedFactory = registeredAddons.put(addonFactory.getAddonName(), addonFactory);

        for (PlayerLoadout loadout : loadouts.values()) {
            if (replacedFactory != null) {
                loadout.unregisterAddon(replacedFactory.getAddonName());
            }

            loadout.registerAddon(addonFactory);
        }

        return replacedFactory;
    }

    /**
     * Unregisters a previously registered addon
     *
     * @param addonName The addon to unregister
     */
    public void unregisterAddon(@NotNull String addonName) {
        registeredAddons.remove(addonName);

        for (PlayerLoadout loadout : loadouts.values()) {
            loadout.unregisterAddon(addonName);
        }
    }

    /**
     * Retrieves a registered addon
     *
     * @param addonName The addon name to get the addon for
     * @return The addon or null
     */
    public static LoadoutAddonFactory getAddonFactory(@NotNull String addonName) {
        return registeredAddons.get(addonName);
    }

    @Override
    public boolean useSeparateConfig() {
        return false;
    }

    @Override
    public void save(@NotNull FileConfiguration config, @NotNull String path) {
        char configSeparator = config.options().pathSeparator();
        LoadoutFlag loadoutFlag;
        for (Map.Entry<String, PlayerLoadout> loadoutEntry : loadouts.entrySet()) {
            loadoutFlag = new LoadoutFlag(loadoutEntry.getValue(), loadoutEntry.getKey());
            loadoutFlag.saveValue(config, path + configSeparator + "loadouts");
        }
    }

    @Override
    public void load(@NotNull FileConfiguration config, @NotNull String path) {
        char configSeparator = config.options().pathSeparator();
        final ConfigurationSection configSection = config.getConfigurationSection(path + configSeparator + "loadouts");
        if (configSection != null) {
            LoadoutFlag loadoutFlag;

            for (String loadout : configSection.getKeys(false)) {
                loadoutFlag = new LoadoutFlag(new PlayerLoadout(loadout), loadout);
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

    public void addLoadout(String name) {
        loadouts.put(name, new PlayerLoadout(name));
    }

    public void deleteLoadout(String name) {
        loadouts.remove(name);
    }

    public Set<String> getLoadoutNames() {
        return loadouts.keySet();
    }

    public Set<PlayerLoadout> getLoadouts() {
        return new HashSet<>(loadouts.values());
    }

    public Map<String, PlayerLoadout> getLoadoutMap() {
        return loadouts;
    }

    public @Nullable PlayerLoadout getLoadout(@NotNull String name) {
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

    public boolean hasLoadout(String name) {
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

    public void displaySelectionMenu(final MinigamePlayer mgPlayer, final boolean equip) {
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
