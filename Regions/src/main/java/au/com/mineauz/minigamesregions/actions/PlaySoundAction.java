package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.FloatFlag;
import au.com.mineauz.minigames.config.StringFlag;
import au.com.mineauz.minigames.managers.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.RegionMessageManager;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PlaySoundAction extends AAction {
    private final StringFlag soundName = new StringFlag(Sound.ENTITY_PLAYER_LEVELUP.name(), "sound");
    private final BooleanFlag privatePlayBack = new BooleanFlag(true, "private");
    private final FloatFlag volume = new FloatFlag(1f, "volume");
    private final FloatFlag pitch = new FloatFlag(1f, "pitch");

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
    public @NotNull Map<@NotNull Component, @Nullable ComponentLike> describe() {
        return Map.of(
                MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_PLAYSOUND_SOUND_NAME), Component.text(getSound().name()),
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
        soundName.saveValue(config, path);
        privatePlayBack.saveValue(config, path);
        volume.saveValue(config, path);
        pitch.saveValue(config, path);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        soundName.loadValue(config, path);
        privatePlayBack.loadValue(config, path);
        volume.loadValue(config, path);
        pitch.loadValue(config, path);
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer mgPlayer, Menu previous) {
        Menu m = new Menu(3, MgMenuLangKey.MENU_PLAYSOUND_MENU_NAME, mgPlayer);

        m.addItem(new MenuItemBack(previous), m.getSize() - 9);
        List<Sound> sounds = Arrays.asList(Sound.values());
        m.addItem(new MenuItemList<>(Material.NOTE_BLOCK, MgMenuLangKey.MENU_PLAYSOUND_SOUND_NAME, new Callback<>() {

            @Override
            public Sound getValue() {
                Sound s = getSound();              //ENSURE CONFIG doesn't contain old enums replace if they do.
                if (!s.name().equals(soundName.getFlag())) {
                    soundName.setFlag(s.toString());
                }
                return s;
            }

            @Override
            public void setValue(Sound value) {
                soundName.setFlag(value.toString().toUpperCase().replace(" ", "_"));
            }
        }, sounds));

        m.addItem(privatePlayBack.getMenuItem(Material.ENDER_PEARL, MgMenuLangKey.MENU_PLAYSOUND_PRIVATEPLAYBACK_NAME));
        m.addItem(new MenuItemDecimal(Material.JUKEBOX, MgMenuLangKey.MENU_PLAYSOUND_VOLUME_NAME, new Callback<>() {

            @Override
            public Double getValue() {
                return volume.getFlag().doubleValue();
            }

            @Override
            public void setValue(Double value) {
                volume.setFlag(value.floatValue());
            }
        }, 0.1, 1d, 0.5, null));

        m.addItem(new MenuItemDecimal(Material.ENDER_EYE, MgMenuLangKey.MENU_PLAYSOUND_PITCH_NAME, new Callback<>() {

            @Override
            public Double getValue() {
                return pitch.getFlag().doubleValue();
            }

            @Override
            public void setValue(Double value) {
                pitch.setFlag(value.floatValue());
            }


        }, 0.05, 0.1, 0d, 2d));
        m.displayMenu(mgPlayer);
        return true;
    }

    private Sound getSound() {
        Sound result;
        try {
            result = Sound.valueOf(soundName.getFlag());
        } catch (IllegalArgumentException e) {
            Minigames.getPlugin().getComponentLogger().warn("Bad Sound Config in Minigame Config : " + soundName.getFlag());
            result = Sound.ENTITY_PLAYER_BURP;
        }
        return result;
    }
}
