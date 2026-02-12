package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.FloatFlag;
import au.com.mineauz.minigames.config.StringFlag;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;
import java.util.Map;

public class PlaySoundAction extends AAction {
    private final @NotNull StringFlag soundKey = new StringFlag("sound", Registry.SOUNDS.getKey(Sound.ENTITY_PLAYER_LEVELUP).asString());
    private final @NotNull BooleanFlag privatePlayBack = new BooleanFlag("private", true);
    private final @NotNull FloatFlag volume = new FloatFlag("volume", 1f);
    private final @NotNull FloatFlag pitch = new FloatFlag("pitch", 1f);

    protected PlaySoundAction(final @NotNull Key key) {
        super(key);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return MessageManager.getMessage(RegionLangKey.MENU_ACTION_PLAYSOUND_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.WORLD;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(
                MessageManager.getMessage(MgMenuLangKey.MENU_PLAYSOUND_SOUND_NAME), Component.text(Registry.SOUNDS.getKey(getSound()).asString()),
                MessageManager.getMessage(MgMenuLangKey.MENU_PLAYSOUND_VOLUME_NAME), Component.text(volume.getFlag()),
                MessageManager.getMessage(MgMenuLangKey.MENU_PLAYSOUND_PITCH_NAME), Component.text(pitch.getFlag()),
                MessageManager.getMessage(MgMenuLangKey.MENU_PLAYSOUND_PRIVATEPLAYBACK_NAME),
                MessageManager.getMessage(privatePlayBack.getFlag() ? MgCommandLangKey.COMMAND_STATE_ENABLED : MgCommandLangKey.COMMAND_STATE_DISABLED));
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
    public void executeRegionAction(final @Nullable MinigamePlayer mgPlayer, final @NotNull Region region) {
        debug(mgPlayer, region);
        if (mgPlayer != null) {
            execute(mgPlayer, mgPlayer.getLocation());
        }
    }

    @Override
    public void executeNodeAction(final @NotNull MinigamePlayer mgPlayer, final @NotNull Node node) {
        debug(mgPlayer, node);
        if (node.getSafeLocation().getWorld() == null) {
            return;
        }

        execute(mgPlayer, node.getSafeLocation().toLocation());
    }

    private void execute(final @NotNull MinigamePlayer mgPlayer, final @NotNull Location loc) {
        if (!mgPlayer.isInMinigame()) return;
        if (privatePlayBack.getFlag()) {
            mgPlayer.getPlayer().playSound(
                    loc,
                    getSound(),
                    volume.getFlag(),
                    pitch.getFlag());
        } else {
            mgPlayer.getPlayer().getWorld().playSound(
                    loc,
                    getSound(),
                    volume.getFlag(),
                    pitch.getFlag());
        }
    }

    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        soundKey.saveValue(config);
        privatePlayBack.saveValue(config);
        volume.saveValue(config);
        pitch.saveValue(config);
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
        soundKey.loadValue(config);

        try {
            @SuppressWarnings({"UnstableApiUsage", "removal"})
            final @NotNull Sound legacySound = Sound.valueOf(soundKey.getFlag().toUpperCase());

            soundKey.setFlag(Registry.SOUNDS.getKey(legacySound).asString());
        } catch (IllegalArgumentException ignored) {
        }

        privatePlayBack.loadValue(config);
        volume.loadValue(config);
        pitch.loadValue(config);
    }

    @Override
    public boolean displayMenu(final @NotNull Menu previous) {
        final @NotNull Menu menu = new Menu(3, MgMenuLangKey.MENU_PLAYSOUND_MENU_NAME, previous.getIntendedViewer());

        menu.setItem(new MenuItemBack(previous), menu.getSize() - 9);
        List<Sound> sounds = Registry.SOUNDS.stream().toList();
        menu.addItem(new MenuItemList<>(ItemType.NOTE_BLOCK, MgMenuLangKey.MENU_PLAYSOUND_SOUND_NAME, new Callback<>() {

            @Override
            public @NotNull Sound getValue() {
                final Sound sound = getSound();              //ensure config doesn't contain old values; replace if they do.

                if (!Registry.SOUNDS.getKey(sound).asString().equals(soundKey.getFlag())) {
                    soundKey.setFlag(sound.toString());
                }
                return sound;
            }

            @Override
            public void setValue(final @NotNull Sound value) {
                soundKey.setFlag(Registry.SOUNDS.getKey(value).asString());
            }
        }, sounds));

        menu.addItem(privatePlayBack.getMenuItem(ItemType.ENDER_PEARL, MgMenuLangKey.MENU_PLAYSOUND_PRIVATEPLAYBACK_NAME));
        menu.addItem(new MenuItemDecimal(ItemType.JUKEBOX, MgMenuLangKey.MENU_PLAYSOUND_VOLUME_NAME, new Callback<>() {

            @Override
            public @NotNull Double getValue() {
                return volume.getFlag().doubleValue();
            }

            @Override
            public void setValue(final @NotNull Double value) {
                volume.setFlag(value.floatValue());
            }
        }, 0.1, 1d, 0.5, null));

        menu.addItem(new MenuItemDecimal(ItemType.ENDER_EYE, MgMenuLangKey.MENU_PLAYSOUND_PITCH_NAME, new Callback<>() {

            @Override
            public @NotNull Double getValue() {
                return pitch.getFlag().doubleValue();
            }

            @Override
            public void setValue(final @NotNull Double value) {
                pitch.setFlag(value.floatValue());
            }


        }, 0.05, 0.1, 0d, 2d));
        menu.displayMenu();
        return true;
    }

    private @NotNull Sound getSound() {
        Sound result = Registry.SOUNDS.get(NamespacedKey.fromString(soundKey.getFlag()));

        if (result == null) {
            Minigames.getPlugin().getComponentLogger().warn("Bad Sound Config in Minigame Config : " + soundKey.getFlag());
            result = Sound.ENTITY_PLAYER_BURP;
        }
        return result;
    }
}
