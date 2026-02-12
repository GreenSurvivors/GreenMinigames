package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.config.StringFlag;
import au.com.mineauz.minigames.config.TimeFlag;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ApplyEffectAction extends AAction {
    private final @NotNull StringFlag typeNameSpacedKey = new StringFlag("type", PotionEffectType.SPEED.getKey().toString());
    private final @NotNull TimeFlag dur = new TimeFlag("duration", 60L);
    private final @NotNull IntegerFlag amp = new IntegerFlag("amplifier", 1);
    private @Nullable PotionEffectType type = null;

    protected ApplyEffectAction(final @NotNull Key key) {
        super(key);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return MessageManager.getMessage(RegionLangKey.MENU_ACTION_EFFECTAPPLY_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.PLAYER;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        Component typeComp;
        if (type == null) {
            typeComp = MessageManager.getMessage(MgMenuLangKey.MENU_ERROR_UNKNOWN);
        } else {
            typeComp = Component.translatable(type.translationKey());
        }

        return Map.of(MessageManager.getMessage(RegionLangKey.MENU_ACTION_EFFECTAPPLY_EFFECT_NAME),
                typeComp.append(Component.text(" " + amp.getFlag())),

                MessageManager.getMessage(RegionLangKey.MENU_ACTION_EFFECTAPPLY_DURATION_NAME),
                dur.getFlag() == PotionEffect.INFINITE_DURATION ?
                        MessageManager.getMessage(MgMenuLangKey.MENU_NUMBER_INFINITE) :
                        Component.text(dur.getFlag()));
    }

    @Override
    public boolean useInRegions() {
        return true;
    }

    @Override
    public boolean useInNodes() {
        return true;
    }

    @Override
    public void executeRegionAction(@NotNull MinigamePlayer mgPlayer,
                                    @NotNull Region region) {
        debug(mgPlayer, region);
        execute(mgPlayer);
    }

    @Override
    public void executeNodeAction(@NotNull MinigamePlayer mgPlayer,
                                  @NotNull Node node) {
        debug(mgPlayer, node);
        execute(mgPlayer);
    }

    private void execute(@NotNull MinigamePlayer player) {
        if (type != null) {
            PotionEffect effect = new PotionEffect(type, dur.getFlag().intValue() * 20, amp.getFlag() - 1);
            player.getPlayer().addPotionEffect(effect);
        }
    }

    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        typeNameSpacedKey.saveValue(config);
        dur.saveValue(config);
        amp.saveValue(config);
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
        typeNameSpacedKey.loadValue(config);
        dur.loadValue(config);
        amp.loadValue(config);

        String temp = typeNameSpacedKey.getFlag().toLowerCase(Locale.ENGLISH);
        temp = switch (temp) { // dataFixerUpper
            case "slow" -> "slowness";
            case "fast_digging" -> "haste";
            case "slow_digging" -> "mining_fatigue";
            case "increase_damage" -> "strength";
            case "heal" -> "instant_health";
            case "harm" -> "instant_damage";
            case "jump" -> "jump_boost";
            case "confusion" -> "nausea";
            case "damage_resistance" -> "resistance";
            default -> temp;
        };

        NamespacedKey key = NamespacedKey.fromString(temp);
        if (key != null) {
            type = Registry.EFFECT.get(key);

            if (type == null) {
                Minigames.getPlugin().getComponentLogger().error("Could not find status effect from NameSpacedKey \"" + temp + "\". " +
                        "ApplyEffectAction under \"" + config.path() + "\" will fail.");
            }
        } else {
            Minigames.getPlugin().getComponentLogger().error("Could not get NameSpacedKey \"" + temp + "\". " +
                    "ApplyEffectAction under \"" + config.path() + "\" will fail.");
        }
    }

    @Override
    public boolean displayMenu(final @NotNull Menu previous) {
        final @NotNull Menu menu = new Menu(3, getDisplayname(), previous.getIntendedViewer());
        menu.setItem(new MenuItemBack(previous), menu.getSize() - 9);


        final @NotNull List<@NotNull PotionEffectType> pots = Registry.EFFECT.stream().toList();

        menu.addItem(new MenuItemList<>(MenuDisplayTypes.potionEffectType(), MessageManager.getMessage(RegionLangKey.MENU_ACTION_EFFECTAPPLY_EFFECT_NAME),
            new Callback<>() {
                @Override
                public @Nullable PotionEffectType getValue() {
                    return type;
                }

                @Override
                public void setValue(@NotNull PotionEffectType value) {
                    typeNameSpacedKey.setFlag(value.getKey().asString());
                    type = value;
                }
            }, pots));
        menu.addItem(dur.getMenuItem(MenuDisplayTypes.timeType(), MessageManager.getMessage(RegionLangKey.MENU_ACTION_EFFECTAPPLY_DURATION_NAME), 0L, 86400L));
        menu.addItem(new MenuItemInteger(ItemType.EXPERIENCE_BOTTLE, MessageManager.getMessage(RegionLangKey.MENU_ACTION_EFFECTAPPLY_LEVEL_NAME), new Callback<>() {

            @Override
            public Integer getValue() {
                return amp.getFlag();
            }

            @Override
            public void setValue(Integer value) {
                amp.setFlag(value);
            }

        }, 0, 100));
        menu.displayMenu();
        return true;
    }
}
