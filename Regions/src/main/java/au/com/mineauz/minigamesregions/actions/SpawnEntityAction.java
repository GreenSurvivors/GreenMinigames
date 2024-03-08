package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.EnumFlag;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/*
 * todo
 * Entity settings:
 *   rotation
 *
 * Living Entity Settings:
 *   Potion effect
 *   EntityEquipment
 * Ageable:
 *   setAge
 * --> Breeadble
 *   setAgeLock
 */

public class SpawnEntityAction extends AAction {
    /**
     * Contains all entities that are problematic to spawn as is.
     * Some problems may get resolved, if their (default)settings get added in the future;
     * others probably stay problematic and shouldn't be spawn able like players
     */
    private final Set<EntityType> NOT_SPAWNABLE = Set.of( //todo enabled feature by world
            EntityType.AREA_EFFECT_CLOUD, // todo needs effect
            EntityType.BLOCK_DISPLAY, // todo needs block state/data and display settings
            EntityType.DROPPED_ITEM, // todo needs ItemMeta
            EntityType.FALLING_BLOCK, // todo needs block state/data
            EntityType.FISHING_HOOK, // needs a fishing rod; we can't guarantee this
            EntityType.GLOW_ITEM_FRAME, // hanging items need support and direction; we can't guarantee this
            EntityType.ITEM_DISPLAY, // todo needs ItemMeta and display settings
            EntityType.ITEM_FRAME, // hanging items need support and direction; we can't guarantee this
            EntityType.LEASH_HITCH, // hanging items need support also does not easy leash an entity; we can't guarantee this
            EntityType.LIGHTNING, // todo needs lightning settings
            EntityType.PAINTING, // hanging items need support and direction; we can't guarantee this
            EntityType.PLAYER, // we don't support npcs; todo maybe in the future integrate citizens support?
            EntityType.TEXT_DISPLAY, // todo needs text and display settings
            EntityType.UNKNOWN // not a spawn able entity type
    );

    private final EnumFlag<EntityType> type = new EnumFlag<>(EntityType.ZOMBIE, "type");
    //private final Map<String, ConfigSerializableBridge<?>> settings = new HashMap<>(); // todo once Paper stops relocation use ((CraftEntity)entity).getHandle().saveWithoutId(nbt); instead of an own bad implementation

    protected SpawnEntityAction(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull String getName() {
        return "SPAWN_ENTITY";
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SPAWNENTITY_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.WORLD;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() { //todo
        Map<Component, Component> out = new HashMap<>(2);

        out.put(RegionMessageManager.getMessage(RegionLangKey.MENU_ENTITY_TYPE_NAME), Component.text(type.getFlag().translationKey()));

        /*
        if (type.getFlag().isAlive() && settings.containsKey("customName")) {
            out.put(RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SPAWNENTITY_NAME_NAME),
                    MiniMessage.miniMessage().deserialize((String) settings.get("customName").getObject()));
        }*/

        return out;
    }

    @Override
    public boolean useInRegions() {
        return false;
    }

    @Override
    public boolean useInNodes() {
        return true;
    }

    @Override
    public void executeRegionAction(@Nullable MinigamePlayer mgPlayer,
                                    @NotNull Region region) {
        debug(mgPlayer, region);
    }

    @Override
    public void executeNodeAction(@NotNull MinigamePlayer mgPlayer, @NotNull Node node) {
        if (!mgPlayer.isInMinigame()) {
            return;
        }
        debug(mgPlayer, node);
        node.getLocation().getWorld().spawnEntity(node.getLocation(), type.getFlag(), CreatureSpawnEvent.SpawnReason.CUSTOM, entity -> {
            /*
            if (settings.containsKey("velocity")) {
                final Vector velocity = (Vector) settings.get("velocity").getObject();
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> entity.setVelocity(velocity));
            }

            if (settings.containsKey("customName")) {
                entity.customName(MiniMessage.miniMessage().deserialize((String) settings.get("customName").getObject()));
            }

            if (settings.containsKey("customNameVisible")) {
                entity.setCustomNameVisible((Boolean) settings.get("customNameVisible").getObject());
            }

            if (settings.containsKey("visualFire")) {
                entity.setVisualFire((Boolean) settings.get("visualFire").getObject());
            }

            if (settings.containsKey("persistent")) {
                entity.setPersistent((Boolean) settings.get("persistent").getObject());
            }

            if (settings.containsKey("glowing")) {
                entity.setGlowing((Boolean) settings.get("glowing").getObject());
            }

            if (settings.containsKey("invulnerable")) {
                entity.setInvulnerable((Boolean) settings.get("invulnerable").getObject());
            }

            if (settings.containsKey("silent")) {
                entity.setSilent((Boolean) settings.get("silent").getObject());
            }

            if (settings.containsKey("hasGravity")) {
                entity.setGravity((Boolean) settings.get("hasGravity").getObject());
            }

            if (entity instanceof LivingEntity livingEntity) {
                if (settings.containsKey("canPickupItems")) {
                    livingEntity.setCanPickupItems((Boolean) settings.get("canPickupItems").getObject());
                }

                if (settings.containsKey("hasAI")) {
                    livingEntity.setAI((Boolean) settings.get("hasAI").getObject());
                }

                if (settings.containsKey("isCollidable")) {
                    livingEntity.setCollidable((Boolean) settings.get("isCollidable").getObject());
                }
            }*/

            entity.setMetadata("MinigameEntity", new FixedMetadataValue(Minigames.getPlugin(), true)); //todo use in recorder to despawn + add parameter for specific Minigame
            mgPlayer.getMinigame().getRecorderData().addEntity(entity, mgPlayer, true);
        });
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        type.saveValue(config, path);

        /* // temp
        char configSeparator = config.options().pathSeparator();
        for (Map.Entry<String, ConfigSerializableBridge<?>> entry : settings.entrySet()) {
            config.set(path + configSeparator + "settings" + configSeparator + entry.getKey(), entry.getValue().serialize());
        }*/
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        type.loadValue(config, path);
        /*
        settings.clear();
        char configSeparator = config.options().pathSeparator();
        ConfigurationSection section = config.getConfigurationSection(path + configSeparator + "settings");
        if (section != null) { // may was empty
            Set<String> keys = section.getKeys(false);

            for (String key : keys) {
                ConfigSerializableBridge<?> serializableBridge = ConfigSerializableBridge.deserialize(config.get(path + configSeparator + "settings" + configSeparator + key));

                if (serializableBridge != null) {
                    settings.put(key, serializableBridge);
                } else {
                    Minigames.getCmpnntLogger().warn("Key \"" + key + "\" of ConfigSerializableBridge in SpawnEntityAction of path \"" + path + configSeparator + "settings" + configSeparator + key + "\" failed to load!");
                }
            }
        }*/
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer mgPlayer, Menu previous) {
        Menu menu = new Menu(3, getDisplayname(), mgPlayer);
        menu.addItem(new MenuItemBack(previous), menu.getSize() - 9);
        List<EntityType> options = new ArrayList<>();
        for (EntityType type : EntityType.values()) {
            if (!NOT_SPAWNABLE.contains(type)) {
                options.add(type);
            }
        }
        menu.addItem(new MenuItemList<>(Material.SKELETON_SKULL, RegionMessageManager.getMessage(RegionLangKey.MENU_ENTITY_TYPE_NAME), new Callback<>() { //todo spawn egg?

            @Override
            public EntityType getValue() {
                return type.getFlag();
            }

            @Override
            public void setValue(EntityType value) {
                type.setFlag(value);
            }
        }, options));

        final MenuItemCustom customMenuItem = new MenuItemCustom(Material.CHEST, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SPAWNENTITY_SETTINGS_NAME));
        final Menu entitySettingsMenu = new Menu(6, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SPAWNENTITY_SETTINGS_NAME), mgPlayer);
        final MinigamePlayer fply = mgPlayer;
        customMenuItem.setClick(() -> {
            if (type.getFlag().isAlive()) {
                entitySettingsMenu.clearMenu();

                final MenuItemBack backButton = new MenuItemBack(menu);
                entitySettingsMenu.addItem(backButton, entitySettingsMenu.getSize() - 1);
                populateEntitySettings(entitySettingsMenu, mgPlayer);

                entitySettingsMenu.displayMenu(fply);
                return null;
            }
            return customMenuItem.getDisplayItem();
        });
        menu.addItem(customMenuItem);

        menu.displayMenu(mgPlayer);
        return true;
    }

    private void populateEntitySettings(@NotNull Menu entitySettingsMenu, @NotNull MinigamePlayer mgPlayer) {
        /*
        entitySettingsMenu.addItem(new MenuItemComponent(Material.NAME_TAG,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SPAWNENTITY_NAME_NAME), new Callback<>() {
            @Override
            public Component getValue() {
                ConfigSerializableBridge<?> value = settings.get("customName");
                if (value == null) {
                    return Component.empty();
                } else {
                    return MiniMessage.miniMessage().deserialize((String) value.getObject());
                }
            }

            @Override
            public void setValue(Component value) {
                settings.put("customName", new ConfigSerializableBridge<>(MiniMessage.miniMessage().serialize(value)));
            }
        }));

        entitySettingsMenu.addItem(new MenuItemBoolean(Material.SPYGLASS,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SPAWNENTITY_NAMEVISIBLE_NAME), new Callback<>() {
            @Override
            public Boolean getValue() {
                ConfigSerializableBridge<?> value = settings.get("customNameVisible");
                if (value == null) {
                    return false;
                } else {
                    return (Boolean) value.getObject();
                }
            }

            @Override
            public void setValue(Boolean value) {
                settings.put("customNameVisible", new ConfigSerializableBridge<>(value));
            }
        }));

        Menu velocityMenu = new Menu(3, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SPAWNENTITY_VELOCITY_NAME), mgPlayer);
        final MenuItemBack backButton = new MenuItemBack(entitySettingsMenu);
        velocityMenu.addItem(backButton, velocityMenu.getSize() - 1);

        velocityMenu.addItem(new MenuItemDecimal(Material.ARROW,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SPAWNENTITY_VELOCITY_X_NAME), new Callback<>() {
            @Override
            public Double getValue() {
                ConfigSerializableBridge<?> value = settings.get("velocity");

                if (value == null) {
                    return 0D;
                } else {
                    return ((Vector) value.getObject()).getX();
                }
            }

            @Override
            public void setValue(Double value) {
                Vector vector;

                ConfigSerializableBridge<?> savedVelocity = settings.get("velocity");
                if (savedVelocity == null) {
                    vector = new Vector(value, 0, 0);
                } else {
                    vector = (Vector) savedVelocity.getObject();
                    vector.setX(value);
                }

                settings.put("velocity", new ConfigSerializableBridge<>(vector));
            }


        }, 0.5, 1, null, null));
        velocityMenu.addItem(new MenuItemDecimal(Material.ARROW,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SPAWNENTITY_VELOCITY_Y_NAME), new Callback<>() {
            @Override
            public Double getValue() {
                ConfigSerializableBridge<?> value = settings.get("velocity");

                if (value == null) {
                    return 0D;
                } else {
                    return ((Vector) value.getObject()).getY();
                }
            }

            @Override
            public void setValue(Double value) {
                Vector vector;

                ConfigSerializableBridge<?> savedVelocity = settings.get("velocity");
                if (savedVelocity == null) {
                    vector = new Vector(0, value, 0);
                } else {
                    vector = (Vector) savedVelocity.getObject();
                    vector.setY(value);
                }

                settings.put("velocity", new ConfigSerializableBridge<>(vector));
            }


        }, 0.5, 1, null, null));
        velocityMenu.addItem(new MenuItemDecimal(Material.ARROW,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SPAWNENTITY_VELOCITY_Z_NAME), new Callback<>() {

            @Override
            public Double getValue() {
                ConfigSerializableBridge<?> value = settings.get("velocity");

                if (value == null) {
                    return 0D;
                } else {
                    return ((Vector) value.getObject()).getZ();
                }
            }

            @Override
            public void setValue(Double value) {
                Vector vector;

                ConfigSerializableBridge<?> savedVelocity = settings.get("velocity");
                if (savedVelocity == null) {
                    vector = new Vector(0, 0, value);
                } else {
                    vector = (Vector) savedVelocity.getObject();
                    vector.setZ(value);
                }

                settings.put("velocity", new ConfigSerializableBridge<>(vector));
            }
        }, 0.5, 1, null, null));
        entitySettingsMenu.addItem(new MenuItemPage(Material.FIREWORK_ROCKET,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SPAWNENTITY_VELOCITY_NAME), velocityMenu));

        entitySettingsMenu.addItem(new MenuItemBoolean(Material.CAMPFIRE,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SPAWNENTITY_VISUALFIRE_NAME), new Callback<>() {
            @Override
            public Boolean getValue() {
                ConfigSerializableBridge<?> value = settings.get("visualFire");
                if (value == null) {
                    return false;
                } else {
                    return (Boolean) value.getObject();
                }
            }

            @Override
            public void setValue(Boolean value) {
                settings.put("visualFire", new ConfigSerializableBridge<>(value));
            }
        }));

        entitySettingsMenu.addItem(new MenuItemBoolean(Material.SLIME_BALL,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SPAWNENTITY_PERSISTENT_NAME), new Callback<>() {
            @Override
            public Boolean getValue() {
                ConfigSerializableBridge<?> value = settings.get("persistent");
                if (value == null) {
                    return false;
                } else {
                    return (Boolean) value.getObject();
                }
            }

            @Override
            public void setValue(Boolean value) {
                settings.put("persistent", new ConfigSerializableBridge<>(value));
            }
        }));

        entitySettingsMenu.addItem(new MenuItemBoolean(Material.GLOWSTONE_DUST,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SPAWNENTITY_GLOWING_NAME), new Callback<>() {
            @Override
            public Boolean getValue() {
                ConfigSerializableBridge<?> value = settings.get("glowing");
                if (value == null) {
                    return false;
                } else {
                    return (Boolean) value.getObject();
                }
            }

            @Override
            public void setValue(Boolean value) {
                settings.put("glowing", new ConfigSerializableBridge<>(value));
            }
        }));

        entitySettingsMenu.addItem(new MenuItemBoolean(Material.SHIELD,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SPAWNENTITY_INVULNERABLE_NAME), new Callback<>() {
            @Override
            public Boolean getValue() {
                ConfigSerializableBridge<?> value = settings.get("invulnerable");
                if (value == null) {
                    return false;
                } else {
                    return (Boolean) value.getObject();
                }
            }

            @Override
            public void setValue(Boolean value) {
                settings.put("invulnerable", new ConfigSerializableBridge<>(value));
            }
        }));

        entitySettingsMenu.addItem(new MenuItemBoolean(Material.SCULK_SENSOR,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SPAWNENTITY_SILENT_NAME), new Callback<>() {
            @Override
            public Boolean getValue() {
                ConfigSerializableBridge<?> value = settings.get("silent");
                if (value == null) {
                    return false;
                } else {
                    return (Boolean) value.getObject();
                }
            }

            @Override
            public void setValue(Boolean value) {
                settings.put("silent", new ConfigSerializableBridge<>(value));
            }
        }));

        // don't overflow to next page
        entitySettingsMenu.addItem(new MenuItemNewLine());

        entitySettingsMenu.addItem(new MenuItemBoolean(Material.ELYTRA,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SPAWNENTITY_GRAVITY_NAME), new Callback<>() {
            @Override
            public Boolean getValue() {
                ConfigSerializableBridge<?> value = settings.get("hasGravity");
                if (value == null) {
                    return true;
                } else {
                    return (Boolean) value.getObject();
                }
            }

            @Override
            public void setValue(Boolean value) {
                settings.put("hasGravity", new ConfigSerializableBridge<>(value));
            }
        }));

        if (type.getFlag().isAlive()) {
            entitySettingsMenu.addItem(new MenuItemNewLine());
            populateLivingEntitySettings(entitySettingsMenu, mgPlayer);
        }*/
    }

    private void populateLivingEntitySettings(@NotNull Menu entitySettingsMenu, @NotNull MinigamePlayer mgPlayer) {
        /*
        entitySettingsMenu.addItem(new MenuItemNewLine());

        entitySettingsMenu.addItem(new MenuItemBoolean(Material.HOPPER,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SPAWNENTITY_PICKUPITEMS_NAME), new Callback<>() {
            @Override
            public Boolean getValue() {
                ConfigSerializableBridge<?> value = settings.get("canPickupItems");
                if (value == null) {
                    return false;
                } else {
                    return (Boolean) value.getObject();
                }
            }

            @Override
            public void setValue(Boolean value) {
                settings.put("canPickupItems", new ConfigSerializableBridge<>(value));
            }
        }));

        entitySettingsMenu.addItem(new MenuItemBoolean(Material.LIGHT,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SPAWNENTITY_AI_NAME), new Callback<>() {
            @Override
            public Boolean getValue() {
                ConfigSerializableBridge<?> value = settings.get("hasAI");
                if (value == null) {
                    return false;
                } else {
                    return (Boolean) value.getObject();
                }
            }

            @Override
            public void setValue(Boolean value) {
                settings.put("hasAI", new ConfigSerializableBridge<>(value));
            }
        }));

        entitySettingsMenu.addItem(new MenuItemBoolean(Material.GLASS,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SPAWNENTITY_COLLIDABLE_NAME), new Callback<>() {
            @Override
            public Boolean getValue() {
                ConfigSerializableBridge<?> value = settings.get("isCollidable");
                if (value == null) {
                    return false;
                } else {
                    return (Boolean) value.getObject();
                }
            }

            @Override
            public void setValue(Boolean value) {
                settings.put("isCollidable", new ConfigSerializableBridge<>(value));
            }
        }));*/
    }
}
