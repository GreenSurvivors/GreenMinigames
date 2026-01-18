package au.com.mineauz.minigames;

import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.minigame.TeamColor;
import au.com.mineauz.minigames.minigame.modules.ALoadoutAddon;
import au.com.mineauz.minigames.minigame.modules.ILoadoutAddonFactory;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.*;
import java.util.regex.Pattern;

public class PlayerLoadout {
    private static final @NotNull Pattern NUMBER = Pattern.compile("[+-]?[0-9]+");

    private final @NotNull Map<@NotNull Key, @NotNull ALoadoutAddon> addons = new HashMap<>();
    private final @NotNull Map<@NotNull Integer, @NotNull ItemStack> itemSlots = new HashMap<>();
    private final @NotNull List<@NotNull PotionEffect> effects = new ArrayList<>();
    private final @NotNull String loadoutName;
    private boolean usePermission = false;
    private boolean fallDamage = true;
    private boolean hunger = false;
    private int level = -1;
    private boolean deletable = true;
    private @Nullable Component displayname = null;
    private boolean lockInventory = false;
    private boolean lockArmour = false;
    private boolean allowOffHand = true;
    private @Nullable TeamColor team;
    private boolean displayInMenu = true;

    public PlayerLoadout(@NotNull String name) {
        loadoutName = name;
        team = TeamColor.matchColor(name);
    }

    public @NotNull Callback<Component> getDisplayNameCallback() {
        return new Callback<>() {

            @Override
            public Component getValue() {
                return displayname;
            }

            @Override
            public void setValue(Component value) {
                displayname = value;
            }
        };
    }

    public @NotNull Component getDisplayName() {
        return Objects.requireNonNullElseGet(displayname, () -> Component.text(loadoutName));
    }

    public void setDisplayName(@NotNull Component name) {
        displayname = name;
    }

    public boolean usesPermissions() {
        return usePermission;
    }

    public void setUsePermissions(boolean bool) {
        usePermission = bool;
    }

    public @NotNull Callback<Boolean> getUsePermissionsCallback() {
        return new Callback<>() {

            @Override
            public @NotNull Boolean getValue() {
                return usePermission;
            }

            @Override
            public void setValue(@NotNull Boolean value) {
                usePermission = value;
            }
        };
    }

    public @NotNull String getName() {
        return loadoutName;
    }

    public void addItem(@NotNull ItemStack item, int slot) {
        itemSlots.put(slot, item);
    }

    public void addPotionEffect(@NotNull PotionEffect effect) {
        for (PotionEffect pot : effects) {
            if (effect.getType().getKey().equals(pot.getType().getKey())) {
                effects.remove(pot);
                break;
            }
        }
        effects.add(effect);
    }

    public void removePotionEffect(@NotNull PotionEffect effect) {
        if (effects.contains(effect)) {
            effects.remove(effect);
        } else {
            for (PotionEffect pot : effects) {
                if (pot.getType().getKey().equals(effect.getType().getKey())) {
                    effects.remove(pot);
                    break;
                }
            }
        }
    }

    public @NotNull List<@NotNull PotionEffect> getAllPotionEffects() {
        return effects;
    }

    public void equipLoadout(@NotNull MinigamePlayer mgPlayer) {
        mgPlayer.getPlayer().getInventory().clear();
        mgPlayer.getPlayer().getInventory().setHelmet(null);
        mgPlayer.getPlayer().getInventory().setChestplate(null);
        mgPlayer.getPlayer().getInventory().setLeggings(null);
        mgPlayer.getPlayer().getInventory().setBoots(null);
        for (PotionEffect potion : mgPlayer.getPlayer().getActivePotionEffects()) {
            mgPlayer.getPlayer().removePotionEffect(potion.getType());
        }
        if (!itemSlots.isEmpty()) {
            Player player = mgPlayer.getPlayer();

            for (Map.Entry<Integer, ItemStack> slotItem : itemSlots.entrySet()) {
                if (slotItem.getKey() >= 0 && slotItem.getKey() < 100) {
                    player.getInventory().setItem(slotItem.getKey(), slotItem.getValue());
                } else {
                    switch (slotItem.getKey()) {
                        case 100 -> player.getInventory().setBoots(slotItem.getValue());
                        case 101 -> player.getInventory().setLeggings(slotItem.getValue());
                        case 102 -> player.getInventory().setChestplate(slotItem.getValue());
                        case 103 -> player.getInventory().setHelmet(slotItem.getValue());
                        case -106 -> player.getInventory().setItemInOffHand(slotItem.getValue());
                    }
                }
            }
            mgPlayer.updateInventory();
        }

        final MinigamePlayer fplayer = mgPlayer;
        Bukkit.getScheduler().runTask(Minigames.getPlugin(), () -> fplayer.getPlayer().addPotionEffects(effects));

        for (final @NotNull ALoadoutAddon addon : addons.values()) {
            addon.applyLoadout(mgPlayer);
        }

        if (level != -1) {
            mgPlayer.getPlayer().setLevel(level);
        }
    }

    public void removeLoadout(@NotNull MinigamePlayer player) {
        for (final @NotNull ALoadoutAddon addon : addons.values()) {
            addon.clearLoadout(player);
        }
    }

    public @NotNull Set<@NotNull Integer> getItemSlots() {
        return itemSlots.keySet();
    }

    public ItemStack getItem(int slot) {
        return itemSlots.get(slot);
    }

    public void clearLoadout() {
        itemSlots.clear();
    }

    public boolean hasFallDamage() {
        return fallDamage;
    }

    public void setHasFallDamage(boolean bool) {
        fallDamage = bool;
    }

    public @NotNull Callback<@NotNull Boolean> getFallDamageCallback() {
        return new Callback<>() {

            @Override
            public @NotNull Boolean getValue() {
                return fallDamage;
            }

            @Override
            public void setValue(@NotNull Boolean value) {
                fallDamage = value;
            }
        };
    }

    public boolean hasHunger() {
        return hunger;
    }

    public void setHasHunger(boolean bool) {
        hunger = bool;
    }

    public @NotNull Callback<@NotNull Boolean> getHungerCallback() {
        return new Callback<>() {

            @Override
            public @NotNull Boolean getValue() {
                return hunger;
            }

            @Override
            public void setValue(@NotNull Boolean value) {
                hunger = value;
            }
        };
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public @NotNull Callback<@NotNull Integer> getLevelCallback() {
        return new Callback<>() {

            @Override
            public @NotNull Integer getValue() {
                return level;
            }

            @Override
            public void setValue(@NotNull @Range(from = 0, to = Integer.MAX_VALUE) Integer value) {
                if (level >= -1) {
                    level = value;
                }
            }
        };
    }

    public boolean isDeletable() {
        return deletable;
    }

    public void setDeletable(boolean value) {
        deletable = value;
    }

    public boolean isInventoryLocked() {
        return lockInventory;
    }

    public void setInventoryLocked(boolean locked) {
        lockInventory = locked;
    }

    public @NotNull Callback<@NotNull Boolean> getInventoryLockedCallback() {
        return new Callback<>() {

            @Override
            public @NotNull Boolean getValue() {
                return isInventoryLocked();
            }

            @Override
            public void setValue(@NotNull Boolean value) {
                setInventoryLocked(value);
            }
        };
    }

    public boolean isArmourLocked() {
        return lockArmour;
    }

    public void setArmourLocked(boolean locked) {
        lockArmour = locked;
    }

    public @NotNull Callback<@NotNull Boolean> getArmourLockedCallback() {
        return new Callback<>() {

            @Override
            public @NotNull Boolean getValue() {
                return isArmourLocked();
            }

            @Override
            public void setValue(@NotNull Boolean value) {
                setArmourLocked(value);
            }
        };
    }

    public boolean allowOffHand() {
        return allowOffHand;
    }

    public @NotNull Callback<@NotNull Boolean> getAllowOffHandCallback() {
        return new Callback<>() {
            @Override
            public @NotNull Boolean getValue() {
                return allowOffHand;
            }

            @Override
            public void setValue(@NotNull Boolean value) {
                allowOffHand = value;
            }
        };
    }

    public void setAllowOffHand(boolean allow) {
        allowOffHand = allow;
    }

    public @Nullable TeamColor getTeamColor() {
        return team;
    }

    public void setTeamColor(@Nullable TeamColor color) {
        team = color;
    }

    public @NotNull Callback<TeamColor> getTeamColorCallback() {
        return new Callback<>() {

            @Override
            public TeamColor getValue() {
                if (getTeamColor() == null) {
                    return TeamColor.NONE;
                }
                return getTeamColor();
            }

            @Override
            public void setValue(TeamColor value) {
                setTeamColor(value);
            }
        };
    }

    public boolean isDisplayedInMenu() {
        return displayInMenu;
    }

    public @NotNull Callback<@NotNull Boolean> getDisplayInMenuCallback() {
        return new Callback<>() {

            @Override
            public @NotNull Boolean getValue() {
                return isDisplayedInMenu();
            }

            @Override
            public void setValue(@NotNull Boolean value) {
                setDisplayInMenu(value);
            }
        };
    }

    public void setDisplayInMenu(boolean bool) {
        displayInMenu = bool;
    }

    /**
     * registers an addon in this loadout
     * @param addonFactory The addonFactory
     */
    public void registerAddon(@NotNull ILoadoutAddonFactory addonFactory) {
        addons.put(addonFactory.getKey(), addonFactory.makeNewLoadoutAddon(this));
    }

    /**
     * unregisters an addon in this loadout
     * @param key The addons key
     */
    public void unregisterAddon(@NotNull Key key) {
        addons.remove(key);
    }

    public void addAddonMenuItems(@NotNull Menu menu) {
        for (final @NotNull ALoadoutAddon addon : addons.values()) {
            addon.addMenuOptions(menu);
        }
    }

    public void save(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        for (Integer slot : getItemSlots()) {
            config.node("items", slot).set(getItem(slot).serializeAsBytes());
        }

        final @NotNull CommentedConfigurationNode potionNode = config.node("potions");
        for (final @NotNull PotionEffect eff : getAllPotionEffects()) {
            final @NotNull ConfigurationNode effectNode = potionNode.node(eff.getType().getKey().asMinimalString());
            effectNode.node("amp").raw(eff.getAmplifier());
            effectNode.node("dur").raw(eff.getDuration());
        }

        if (usesPermissions()) {
            config.node("usepermissions").set(Boolean.TRUE);
        }

        if (!hasFallDamage()) {
            config.node("falldamage").set(Boolean.FALSE);
        }

        if (hasHunger()) {
            config.node("hunger").set(Boolean.TRUE);
        }

        String displayName = MiniMessage.miniMessage().serialize(getDisplayName());
        if (!loadoutName.equalsIgnoreCase(displayName)) {
            config.node("displayName").set(displayName);
        }

        if (isArmourLocked()) {
            config.node("armourLocked").set(Boolean.TRUE);
        }

        if (isInventoryLocked()) {
            config.node("inventoryLocked").set(Boolean.TRUE);
        }

        if (getTeamColor() != null) {
            config.node("team").set(getTeamColor().name());
        }

        if (!isDisplayedInMenu()) {
            config.node("displayInMenu").set(Boolean.FALSE);
        }

        if (!allowOffHand()) {
            config.node("allowOffhand").set(Boolean.FALSE);
        }

        final @NotNull CommentedConfigurationNode addonsNode = config.node("addons"); // todo this breaks backwards compability
        for (final @NotNull ALoadoutAddon addon : addons.values()) {
            addon.save(addonsNode.node(addon.getKey().asMinimalString()));
        }
    }

    public void load(final @NotNull CommentedConfigurationNode config) throws ConfigurateException {
        final @NotNull CommentedConfigurationNode itemNode = config.node("items");
        if (!itemNode.virtual() && !itemNode.isNull()) {
            for (final @NotNull Map.Entry<@NotNull Object, CommentedConfigurationNode> entry : itemNode.childrenMap().entrySet()) {
                final @NotNull String key = entry.toString();

                if (NUMBER.matcher(key).matches()) {

                    final ItemStack item;
                    if (entry.getValue().isMap()) {
                        // datafixerupper
                        item = ItemStack.deserialize((Map<String, Object>) entry.getValue().get(TypeFactory.parameterizedClass(Map.class, String.class, Object.class)));
                    } else {
                        item = ItemStack.deserializeBytes(entry.getValue().get(TypeToken.get(byte[].class)));
                    }

                    addItem(item, Integer.parseInt(key));
                }
            }
        }

        final @NotNull CommentedConfigurationNode potionsNode = config.node("potions");
        if (potionsNode.isMap()) {
            for (final @NotNull Map.Entry<@NotNull Object, CommentedConfigurationNode> entry : potionsNode.childrenMap().entrySet()) {
                final @NotNull CommentedConfigurationNode effectNode = entry.getValue();

                @NotNull String effectKeyStr = entry.getKey().toString().toLowerCase(Locale.ENGLISH);
                effectKeyStr = switch (effectKeyStr) { // dataFixerUpper
                    case "slow" -> "slowness";
                    case "fast_digging" -> "haste";
                    case "slow_digging" -> "mining_fatigue";
                    case "increase_damage" -> "strength";
                    case "heal" -> "instant_health";
                    case "harm" -> "instant_damage";
                    case "jump" -> "jump_boost";
                    case "confusion" -> "nausea";
                    case "damage_resistance" -> "resistance";
                    default -> effectKeyStr;
                };

                final @Nullable NamespacedKey key = NamespacedKey.fromString(effectKeyStr);
                if (key != null) {
                    final @Nullable PotionEffectType effectType = Registry.EFFECT.get(key);

                    if (effectType != null) {
                        PotionEffect effect = new PotionEffect(effectType,
                            effectNode.node("dur").getInt(),
                            effectNode.node("amp").getInt()
                        );

                        addPotionEffect(effect);

                        continue;
                    }
                }

                Minigames.getPlugin().getComponentLogger().error("Could not find status effect from NameSpacedKey \"" + effectKeyStr + "\". " +
                    "Loadout effect under \"" + potionsNode.path() + "\" will fail.");
            }
        }

        if (config.hasChild("usepermissions")) {
            setUsePermissions(config.node("usepermissions").getBoolean());
        }

        if (config.hasChild("falldamage")) {
            setHasFallDamage(config.node("falldamage").getBoolean(true));
        }

        if (config.hasChild("hunger")) {
            setHasHunger(config.node("hunger").getBoolean());
        }

        String rawDisplayName = config.node("displayName").getString();
        if (rawDisplayName != null) {
            setDisplayName(MiniMessage.miniMessage().deserialize(rawDisplayName));
        }

        if (config.hasChild("inventoryLocked")) {
            setInventoryLocked(config.node("inventoryLocked").getBoolean());
        }

        if (config.hasChild("armourLocked")) {
            setArmourLocked(config.node("armourLocked").getBoolean());
        }

        String rawTeamColor = config.node("team").getString();
        if (rawTeamColor != null) {
            setTeamColor(TeamColor.matchColor(rawTeamColor));
        }

        if (config.hasChild("displayInMenu")) {
            setDisplayInMenu(config.node("displayInMenu").getBoolean(true));
        }

        if (config.hasChild("allowOffhand")) {
            setAllowOffHand(config.node("allowOffhand").getBoolean(true));
        }

        final CommentedConfigurationNode addonsNode = config.node("addons");
        if (!addonsNode.virtual()) {
            for (final @NotNull ALoadoutAddon addon : addons.values()) {
                addon.load(addonsNode);
            }
        }
    }
}
