package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.PlayerLoadout;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.StringFlag;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.minigame.modules.LoadoutModule;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.RegionMessageManager;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class EquipLoadoutAction extends AAction {
    private final StringFlag loadout = new StringFlag("default", "loadout");
    private final BooleanFlag equipOnTrigger = new BooleanFlag(false, "equipOnTrigger");

    protected EquipLoadoutAction(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_EQUIPLOADOUT_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.MINIGAME;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_LOADOUT_NAME), Component.text(loadout.getFlag()));
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
    public void executeNodeAction(@NotNull MinigamePlayer mgPlayer, @NotNull Node node) {
        debug(mgPlayer, node);
        if (!mgPlayer.isInMinigame()) return;
        LoadoutModule lmod = LoadoutModule.getMinigameModule(mgPlayer.getMinigame());
        if (lmod != null && lmod.hasLoadout(loadout.getFlag())) {
            PlayerLoadout pLoadOut = lmod.getLoadout(loadout.getFlag());
            mgPlayer.setLoadout(pLoadOut);
            if (equipOnTrigger.getFlag()) {
                pLoadOut.equipLoadout(mgPlayer);
            }
        }
    }

    @Override
    public void executeRegionAction(@Nullable MinigamePlayer mgPlayer, @NotNull Region region) {
        debug(mgPlayer, region);
        if (mgPlayer == null || !mgPlayer.isInMinigame()) return;
        LoadoutModule lmod = LoadoutModule.getMinigameModule(mgPlayer.getMinigame());
        if (lmod != null && lmod.hasLoadout(loadout.getFlag())) {
            PlayerLoadout pLoadOut = lmod.getLoadout(loadout.getFlag());
            mgPlayer.setLoadout(pLoadOut);
            if (equipOnTrigger.getFlag()) {
                pLoadOut.equipLoadout(mgPlayer);
            }
        }
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        loadout.saveValue(config, path);
        equipOnTrigger.saveValue(config, path);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        loadout.loadValue(config, path);
        equipOnTrigger.loadValue(config, path);
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer mgPlayer, Menu previous) {
        Menu m = new Menu(3, getDisplayname(), mgPlayer);
        m.addItem(new MenuItemBack(previous), m.getSize() - 9);
        m.addItem(new MenuItemString(Material.DIAMOND_SWORD, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_LOADOUT_NAME), new Callback<>() {

            @Override
            public String getValue() {
                return loadout.getFlag();
            }

            @Override
            public void setValue(String value) {
                loadout.setFlag(value);
            }
        }));

        m.addItem(new MenuItemBoolean(Material.PAPER, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_LOADOUT_ONTRIGGER_NAME),
                RegionMessageManager.getMessageList(RegionLangKey.MENU_ACTION_LOADOUT_ONTRIGGER_DESCRIPTION), new Callback<>() {
            @Override
            public Boolean getValue() {
                return equipOnTrigger.getFlag();
            }

            @Override
            public void setValue(Boolean value) {
                equipOnTrigger.setFlag(value);
            }
        }));
        m.displayMenu(mgPlayer);
        return true;
    }
}
