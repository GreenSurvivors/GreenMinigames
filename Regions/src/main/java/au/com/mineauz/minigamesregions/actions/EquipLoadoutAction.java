package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.PlayerLoadout;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.StringFlag;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.minigame.modules.LoadoutModule;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EquipLoadoutAction extends AbstractAction {

    private final StringFlag loadout = new StringFlag("default", "loadout");
    private final BooleanFlag equipOnTrigger = new BooleanFlag(false, "equipOnTrigger");

    @Override
    public @NotNull String getName() {
        return "EQUIP_LOADOUT";
    }

    @Override
    public @NotNull String getCategory() {
        return "Minigame Actions";
    }

    @Override
    public void describe(@NotNull Map<@NotNull String, @NotNull Object> out) {
        out.put("Loadout", loadout.getFlag());
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
    public void executeNodeAction(MinigamePlayer player,
                                  @NotNull Node node) {
        debug(player, node);
        if (player == null || !player.isInMinigame()) return;
        LoadoutModule lmod = LoadoutModule.getMinigameModule(player.getMinigame());
        if (lmod.hasLoadout(loadout.getFlag())) {
            PlayerLoadout pLoadOut = lmod.getLoadout(loadout.getFlag());
            player.setLoadout(pLoadOut);
            pLoadOut.equiptLoadout(player);
        }
    }

    @Override
    public void executeRegionAction(MinigamePlayer player, @NotNull Region region) {
        debug(player, region);
        if (player == null || !player.isInMinigame()) return;
        LoadoutModule lmod = LoadoutModule.getMinigameModule(player.getMinigame());
        if (lmod.hasLoadout(loadout.getFlag())) {
            PlayerLoadout pLoadOut = lmod.getLoadout(loadout.getFlag());
            player.setLoadout(pLoadOut);
            pLoadOut.equiptLoadout(player);
        }
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        loadout.saveValue(path, config);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        loadout.loadValue(path, config);
    }

    @Override
    public boolean displayMenu(MinigamePlayer player, Menu previous) {
        Menu m = new Menu(3, "Equip Loadout", player);
        m.addItem(new MenuItemPage("Back", MenuUtility.getBackMaterial(), previous), m.getSize() - 9);
        m.addItem(new MenuItemString("Loadout Name", Material.DIAMOND_SWORD, new Callback<>() {

            @Override
            public String getValue() {
                return loadout.getFlag();
            }

            @Override
            public void setValue(String value) {
                loadout.setFlag(value);
            }


        }));
        List<String> equipDesc = new ArrayList<>();
        equipDesc.add("This will force the loadout to equip as soon as the Action is triggered...");
        m.addItem(new MenuItemBoolean("Equip on Trigger", equipDesc, Material.PAPER, new Callback<>() {
            @Override
            public Boolean getValue() {
                return equipOnTrigger.getFlag();
            }

            @Override
            public void setValue(Boolean value) {
                equipOnTrigger.setFlag(value);
            }


        }));
        m.displayMenu(player);
        return true;
    }

}
