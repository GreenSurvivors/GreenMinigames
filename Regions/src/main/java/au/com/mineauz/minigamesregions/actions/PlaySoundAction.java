package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.FloatFlag;
import au.com.mineauz.minigames.config.StringFlag;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class PlaySoundAction extends AAction {
    private final @NotNull StringFlag soundKey = new StringFlag("sound", Registry.SOUNDS.getKey(Sound.ENTITY_PLAYER_LEVELUP).asString());
    private final @NotNull BooleanFlag privatePlayBack = new BooleanFlag("private", true);
    private final @NotNull FloatFlag volume = new FloatFlag("volume", 1f);
    private final @NotNull FloatFlag pitch = new FloatFlag("pitch", 1f);

    protected PlaySoundAction(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_PLAYSOUND_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.WORLD;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(
                MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_PLAYSOUND_SOUND_NAME), Component.text(Registry.SOUNDS.getKey(getSound()).asString()),
                MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_PLAYSOUND_VOLUME_NAME), Component.text(volume.getFlag()),
                MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_PLAYSOUND_PITCH_NAME), Component.text(pitch.getFlag()),
                MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_PLAYSOUND_PRIVATEPLAYBACK_NAME),
                MinigameMessageManager.getMgMessage(privatePlayBack.getFlag() ? MgCommandLangKey.COMMAND_STATE_ENABLED : MgCommandLangKey.COMMAND_STATE_DISABLED));
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
    public void executeRegionAction(@Nullable MinigamePlayer mgPlayer, @NotNull Region region) {
        debug(mgPlayer, region);
        if (mgPlayer != null) {
            execute(mgPlayer, mgPlayer.getLocation());
        }
    }

    @Override
    public void executeNodeAction(@NotNull MinigamePlayer mgPlayer, @NotNull Node node) {
        debug(mgPlayer, node);
        execute(mgPlayer, node.getLocation());
    }

    private void execute(@NotNull MinigamePlayer player, @NotNull Location loc) {
        if (!player.isInMinigame()) return;
        if (privatePlayBack.getFlag()) {
            player.getPlayer().playSound(
                    loc,
                    getSound(),
                    volume.getFlag(),
                    pitch.getFlag());
        } else {
            player.getPlayer().getWorld().playSound(
                    loc,
                    getSound(),
                    volume.getFlag(),
                    pitch.getFlag());
        }
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        soundKey.saveValue(config, path);
        privatePlayBack.saveValue(config, path);
        volume.saveValue(config, path);
        pitch.saveValue(config, path);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        soundKey.loadValue(config, path);

        try {
            @SuppressWarnings({"UnstableApiUsage", "removal"})
            final @NotNull Sound legacySound = Sound.valueOf(soundKey.getFlag().toUpperCase());

            soundKey.setFlag(Registry.SOUNDS.getKey(legacySound).asString());
        } catch (IllegalArgumentException ignored) {
        }

        privatePlayBack.loadValue(config, path);
        volume.loadValue(config, path);
        pitch.loadValue(config, path);
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer mgPlayer, @NotNull Menu previous) {
        Menu m = new Menu(3, MgMenuLangKey.MENU_PLAYSOUND_MENU_NAME, mgPlayer);

        m.addItem(new MenuItemBack(previous), m.getSize() - 9);
        List<Sound> sounds = Registry.SOUNDS.stream().toList();
        m.addItem(new MenuItemList<>(Material.NOTE_BLOCK, MgMenuLangKey.MENU_PLAYSOUND_SOUND_NAME, new Callback<>() {

            @Override
            public @NotNull Sound getValue() {
                final Sound sound = getSound();              //ensure config doesn't contain old values; replace if they do.

                if (!Registry.SOUNDS.getKey(sound).asString().equals(soundKey.getFlag())) {
                    soundKey.setFlag(sound.toString());
                }
                return sound;
            }

            @Override
            public void setValue(@NotNull Sound value) {
                soundKey.setFlag(Registry.SOUNDS.getKey(value).asString());
            }
        }, sounds));

        m.addItem(privatePlayBack.getMenuItem(Material.ENDER_PEARL, MgMenuLangKey.MENU_PLAYSOUND_PRIVATEPLAYBACK_NAME));
        m.addItem(new MenuItemDecimal(Material.JUKEBOX, MgMenuLangKey.MENU_PLAYSOUND_VOLUME_NAME, new Callback<>() {

            @Override
            public @NotNull Double getValue() {
                return volume.getFlag().doubleValue();
            }

            @Override
            public void setValue(@NotNull Double value) {
                volume.setFlag(value.floatValue());
            }
        }, 0.1, 1d, 0.5, null));

        m.addItem(new MenuItemDecimal(Material.ENDER_EYE, MgMenuLangKey.MENU_PLAYSOUND_PITCH_NAME, new Callback<>() {

            @Override
            public @NotNull Double getValue() {
                return pitch.getFlag().doubleValue();
            }

            @Override
            public void setValue(@NotNull Double value) {
                pitch.setFlag(value.floatValue());
            }


        }, 0.05, 0.1, 0d, 2d));
        m.displayMenu(mgPlayer);
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
