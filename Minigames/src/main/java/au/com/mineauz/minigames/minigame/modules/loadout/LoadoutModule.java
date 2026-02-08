package au.com.mineauz.minigames.minigame.modules.loadout;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.LoadoutFlag;
import au.com.mineauz.minigames.config.MinigameSave;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.AMinigameModule;
import au.com.mineauz.minigames.minigame.modules.MgDefaultModules;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public class LoadoutModule extends AMinigameModule {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?[0-9]+");
    private static final @NotNull Map<@NotNull Key, @NotNull ILoadoutAddonFactory> registeredAddons = new HashMap<>();
    private static final @NotNull Map<String, @NotNull PlayerLoadout> globalLoadouts = new HashMap<>();

    private final @NotNull Map<@NotNull String, @NotNull PlayerLoadout> loadouts = new HashMap<>();

    public LoadoutModule(final @NotNull Minigame mgm, final @NotNull Key key) {
        super(mgm, key);
        final @NotNull PlayerLoadout defaultLoadout = new PlayerLoadout("default");
        registeredAddons.values().forEach(defaultLoadout::registerAddon);
        defaultLoadout.setDeletable(false);
        loadouts.put("default", defaultLoadout);
    }

    public static @Nullable LoadoutModule getMinigameModule(@NotNull Minigame mgm) {
        return ((LoadoutModule) mgm.getModule(MgDefaultModules.LOADOUT.getKey()));
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

    public static @NotNull Collection<@NotNull ILoadoutAddonFactory> getAllAddonFactories() {
        return registeredAddons.values();
    }

    @Override
    public boolean useSeparateConfig() {
        return false;
    }

    @Override
    public void save(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        LoadoutFlag loadoutFlag;
        for (Map.Entry<String, PlayerLoadout> loadoutEntry : loadouts.entrySet()) {
            loadoutFlag = new LoadoutFlag(loadoutEntry.getKey(), loadoutEntry.getValue());
            loadoutFlag.saveValue(config.node("loadouts"));
        }
    }

    @Override
    public void load(final @NotNull CommentedConfigurationNode config) throws ConfigurateException {
        final @NotNull CommentedConfigurationNode loadOutsNode = config.node("loadouts");
        if (!loadOutsNode.virtual() && !loadOutsNode.isNull()) {
            LoadoutFlag loadoutFlag;

            for (final @NotNull CommentedConfigurationNode loadoutNode : loadOutsNode.childrenList()) {
                final @NotNull String loadoutName = loadoutNode.key().toString();
                loadoutFlag = new LoadoutFlag(loadoutName, new PlayerLoadout(loadoutName));
                if (loadoutName.equals("default")) {
                    loadoutFlag.getFlag().setDeletable(false);
                }
                loadoutFlag.loadValue(loadoutNode);
                loadouts.put(loadoutFlag.getName(), loadoutFlag.getFlag());
            }
        }

        if (config.hasChild("loadout")) {
            Minigames.getPlugin().getLogger().warning(config.path() + " contains unsupported configurations: \"loadout\"");
        }
        if (config.hasChild("extraloadouts")) {
            Minigames.getPlugin().getLogger().warning(config.path() + " contains unsupported configurations: \"extraloadouts\"");
        }
    }

    public static void setupGlobalLoadOuts(final @NotNull Minigames plugin) {
        final @NotNull MinigameSave globalLoadoutsSSave = MinigameSave.forGlobalData(Path.of("globalLoadouts"));
        final @NotNull ConfigurationNode configRoot;
        try {
            configRoot = globalLoadoutsSSave.getConfigRoot();
        } catch (final @NotNull ConfigurateException e) {
            plugin.getComponentLogger().error("Couldn't load global loadouts!", e);
            return;
        }

        globalLoadouts.clear();

        for (final @NotNull ConfigurationNode loadoutNode : configRoot.childrenList()) {
            final @NotNull String loadoutName = loadoutNode.key().toString();
            final @NotNull PlayerLoadout loadout = new PlayerLoadout(loadoutName);

            for (final @NotNull ConfigurationNode loadoutEntryNode : loadoutNode.childrenList()) {
                final @NotNull String loadoutEntryKey = loadoutEntryNode.key().toString();
                if (NUMBER_PATTERN.matcher(loadoutEntryKey).matches()) {
                    if (loadoutEntryNode.isMap()) {
                        // datafixerupper
                        try {
                            loadout.addItem(
                                ItemStack.deserialize((Map<String, Object>) loadoutEntryNode.get(TypeFactory.parameterizedClass(Map.class, String.class, Object.class))),
                                Integer.parseInt(loadoutEntryKey));
                        } catch (final @NotNull SerializationException e) {
                            plugin.getComponentLogger().error("Couldn't load global loadout " + loadoutEntryNode.path() + " ignoring.", e);
                        }
                    } else {
                        try {
                            loadout.addItem(
                                ItemStack.deserializeBytes(loadoutEntryNode.get(TypeToken.get(byte[].class))),
                                Integer.parseInt(loadoutEntryKey));
                        } catch (final @NotNull SerializationException e) {
                            plugin.getComponentLogger().error("Couldn't load global loadout " + loadoutEntryNode.path() + " ignoring.", e);
                        }
                    }
                } else if (loadoutEntryKey.equals("potions")) {
                    for (final @NotNull ConfigurationNode potionEntryNode : loadoutNode.childrenList()) {
                        final @Nullable PotionEffectType type = Registry.EFFECT.get(NamespacedKey.fromString(potionEntryNode.key().toString().toLowerCase(Locale.ROOT))); // todo what if null
                        final @NotNull PotionEffect effect = new PotionEffect(type,
                            potionEntryNode.node("dur").getInt(),
                            potionEntryNode.node("amp").getInt());

                        loadout.addPotionEffect(effect);
                    }
                } else if (loadoutEntryKey.equals("usepermissions")) {
                    loadout.setUsePermissions(loadoutEntryNode.getBoolean(false));
                }
            }

            addGlobalLoadout(loadout);
        }
    }

    public static void saveGlobalLoadouts() throws IOException {
        final @NotNull MinigameSave globalLoadouts = MinigameSave.forGlobalData(Path.of("globalLoadouts"));
        final @NotNull ConfigurationNode rootNode = globalLoadouts.getConfigRoot();
        if (LoadoutModule.hasGlobalLoadouts()) {
            for (final PlayerLoadout loadout : LoadoutModule.getGlobalLoadouts()) {
                final @NotNull ConfigurationNode loadoutNode = rootNode.node(loadout.getName());

                for (final int slot : loadout.getItemSlots()) {
                    loadoutNode.node(slot).set(loadout.getItem(slot).serializeAsBytes()); // todo datafixerupper
                }

                loadoutNode.removeChild("potions");

                for (final PotionEffect eff : loadout.getAllPotionEffects()) {
                    final @NotNull ConfigurationNode effectNode = loadoutNode.node("potions", eff.getType().getKey().getKey());
                    effectNode.node("amp").set(eff.getAmplifier());
                    effectNode.node("dur").set(eff.getDuration());
                }
                if (loadout.usesPermissions()) {
                    loadoutNode.node("usepermissions").set(true);
                } else {
                    loadoutNode.removeChild("usepermissions");
                }
            }
        } else {
            rootNode.set(null);
        }
        globalLoadouts.saveConfig();
    }

    public static void addGlobalLoadout(final @NotNull PlayerLoadout loadout) {
        globalLoadouts.put(loadout.getName(), loadout);
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
        final @NotNull Menu menu = new Menu(6, MgMenuLangKey.MENU_LOADOUT_SELECT_NAME, mgPlayer);

        for (final @NotNull PlayerLoadout loadout : loadouts.values()) {
            if (loadout.isDisplayedInMenu()) {
                if (!loadout.usesPermissions() || mgPlayer.getPlayer().hasPermission("minigame.loadout." + loadout.getName().toLowerCase())) {
                    if (mgPlayer.isInMinigame() && !mgPlayer.getMinigame().isTeamGame() || loadout.getTeamColor() == null ||
                        mgPlayer.getTeam().getColor() == loadout.getTeamColor()) {

                        final @NotNull MenuItemCustom loadoutItem = new MenuItemCustom(ItemType.GLASS, loadout.getDisplayName());
                        if (!loadout.getItemSlots().isEmpty()) {
                            ItemStack item = loadout.getItem(new ArrayList<>(loadout.getItemSlots()).getFirst());
                            loadoutItem.setDisplayItem(item);
                        }
                        loadoutItem.setClick(() -> {
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
                            return ItemStack.empty();
                        });
                        menu.addItem(loadoutItem);
                    }
                }
            }
        }
        menu.displayMenu();
    }

    @Override
    public void addEditMenuOptions(final @NotNull Menu superMenu) {
        final Menu loadouts = new Menu(6, getMinigame().getDisplayName(), superMenu.getIntendedViewer());
        final @NotNull List<@NotNull MenuItem> loadoutMenuItems = new ArrayList<>();

        for (final @NotNull PlayerLoadout playerLoadout : getLoadouts()) {
            @NotNull ItemType itemType = ItemType.GLASS_PANE;

            if (!playerLoadout.getItemSlots().isEmpty()) {
                itemType = playerLoadout.getItem((Integer) playerLoadout.getItemSlots().toArray()[0]).getType().asItemType();
            }
            if (playerLoadout.isDeletable()) {
                loadoutMenuItems.add(new MenuItemDisplayLoadout(itemType, playerLoadout.getDisplayName(),
                    MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_DELETE_SHIFTRIGHTCLICK), playerLoadout, getMinigame()));
            } else {
                loadoutMenuItems.add(new MenuItemDisplayLoadout(itemType, playerLoadout.getDisplayName(), playerLoadout, getMinigame()));
            }
        }

        loadouts.addItem(new MenuItemLoadoutAdd(MenuUtility.createType(), MgMenuLangKey.MENU_LOADOUT_ADD_NAME,
            getLoadoutMap(), getMinigame()), 53);
        loadouts.addItem(new MenuItemBack(superMenu), loadouts.getSize() - 9);
        loadouts.addItems(loadoutMenuItems);

        superMenu.addItem(new MenuItemPage(ItemType.CHEST, MgMenuLangKey.MENU_MINIGAME_LOADOUTS_NAME, loadouts));
    }
}
