package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.config.StringFlag;
import au.com.mineauz.minigames.config.TimeFlag;
import au.com.mineauz.minigames.managers.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.RegionMessageManager;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class ApplyEffectAction extends AAction {
    private final StringFlag typeNameSpacedKey = new StringFlag(PotionEffectType.SPEED.getKey().toString(), "type");
    private @Nullable PotionEffectType type = null;
    private final TimeFlag dur = new TimeFlag(60L, "duration");
    private final IntegerFlag amp = new IntegerFlag(1, "amplifier");

    protected ApplyEffectAction(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_EFFECTAPPLY_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.PLAYER;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable ComponentLike> describe() {
        Component typeComp;
        if (type == null) {
            typeComp = MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_ERROR_UNKNOWN);
        } else {
            typeComp = Component.translatable(type.translationKey());
        }

        return Map.of(RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_EFFECTAPPLY_EFFECT_NAME),
                typeComp.append(Component.text(" " + amp.getFlag())),

                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_EFFECTAPPLY_DURATION_NAME),
                dur.getFlag() == PotionEffect.INFINITE_DURATION ?
                        MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_NUMBER_INFINITE) :
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
    public void executeRegionAction(@Nullable MinigamePlayer mgPlayer,
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

    private void execute(MinigamePlayer player) {
        if (type != null) {
            PotionEffect effect = new PotionEffect(type, dur.getFlag().intValue() * 20, amp.getFlag() - 1);
            player.getPlayer().addPotionEffect(effect);
        }
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        typeNameSpacedKey.saveValue(config, path);
        dur.saveValue(config, path);
        amp.saveValue(config, path);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        typeNameSpacedKey.loadValue(config, path);
        dur.loadValue(config, path);
        amp.loadValue(config, path);

        NamespacedKey key = NamespacedKey.fromString(typeNameSpacedKey.getFlag().toLowerCase(java.util.Locale.ENGLISH));
        if (key != null) {
            type = Registry.EFFECT.get(key);

            if (type == null) {
                Minigames.getCmpnntLogger().error("Could not find status effect from name spaced key \"" + typeNameSpacedKey.getFlag() + "\". " +
                        "ApplyEffectAction under \"" + path + "\" will fail.");
            }
        } else {
            Minigames.getCmpnntLogger().error("Could not get name spaced key \"" + typeNameSpacedKey.getFlag() + "\". " +
                    "ApplyEffectAction under \"" + path + "\" will fail.");
        }
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer mgPlayer, Menu previous) {
        Menu m = new Menu(3, getDisplayname(), mgPlayer);
        m.addItem(new MenuItemBack(previous), m.getSize() - 9);


        List<PotionEffectType> pots = Registry.EFFECT.stream().toList();

        m.addItem(new MenuItemList<>(Material.POTION, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_EFFECTAPPLY_EFFECT_NAME), new Callback<>() {
            @Override
            public PotionEffectType getValue() {
                return type;
            }

            @Override
            public void setValue(PotionEffectType value) {
                typeNameSpacedKey.setFlag(value.getKey().asString());
                type = value;
            }
        }, pots));
        m.addItem(dur.getMenuItem(Material.CLOCK, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_EFFECTAPPLY_DURATION_NAME), 0L, 86400L));
        m.addItem(new MenuItemInteger(Material.EXPERIENCE_BOTTLE, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_EFFECTAPPLY_LEVEL_NAME), new Callback<>() {

            @Override
            public Integer getValue() {
                return amp.getFlag();
            }

            @Override
            public void setValue(Integer value) {
                amp.setFlag(value);
            }

        }, 0, 100));
        m.displayMenu(mgPlayer);
        return true;
    }
}
