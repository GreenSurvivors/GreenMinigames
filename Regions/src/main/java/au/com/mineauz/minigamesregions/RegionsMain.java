package au.com.mineauz.minigamesregions;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.commands.set.SetCommand;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.tool.ToolModes;
import au.com.mineauz.minigamesregions.commands.SetNodeCommand;
import au.com.mineauz.minigamesregions.commands.SetRegionCommand;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import au.com.mineauz.minigamesregions.tool.ExecutorHolderEditToolMode;
import au.com.mineauz.minigamesregions.tool.NodeToolMode;
import au.com.mineauz.minigamesregions.tool.RegionToolMode;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class RegionsMain extends JavaPlugin {
    private static Minigames minigames;
    private static RegionsMain plugin;
    private RegionDisplayManager display;

    public static Minigames getMinigames() {
        return minigames;
    }

    @Deprecated
    public static RegionsMain getPlugin() {
        return plugin;
    }

    @Override
    public void onDisable() {
        if (plugin == null) {
            return;
        }
        for (Minigame mg : minigames.getMinigameManager().getAllMinigames().values()) {
            mg.saveMinigame();
        }
        minigames.getMinigameManager().removeModule(RegionModule.getFactory().getKey());

        ToolModes.removeToolMode("REGION");
        ToolModes.removeToolMode("NODE");
        ToolModes.removeToolMode("REGION_AND_NODE_EDITOR");

        display.shutdown();

        getLogger().info("Minigames Regions disabled");
    }

    @Override
    public void onEnable() {
        try {
            plugin = this;
            Plugin mgPlugin = getServer().getPluginManager().getPlugin("Minigames");
            if (mgPlugin != null && mgPlugin.isEnabled()) {
                minigames = (Minigames) mgPlugin;
            } else {
                getLogger().severe("Minigames plugin not found! You must have the plugin to use Regions!");
                plugin = null;
                minigames = null;
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            display = new RegionDisplayManager(minigames);

            minigames.getMinigameManager().addModule(RegionModule.getFactory());

            SetCommand.registerSetCommand(new SetNodeCommand());
            SetCommand.registerSetCommand(new SetRegionCommand());

            getServer().getPluginManager().registerEvents(new RegionEvents(), this);

            ToolModes.addToolMode(new RegionToolMode());
            ToolModes.addToolMode(new NodeToolMode());
            ToolModes.addToolMode(new ExecutorHolderEditToolMode());
            RegionMessageManager.register();
            getLogger().info("Minigames Regions successfully enabled!");
        } catch (Exception e) {
            plugin = null;
            minigames = null;
            getComponentLogger().error("Failed to enable Minigames Regions " + getPluginMeta().getVersion() + ": ", e);
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    public RegionDisplayManager getDisplayManager() {
        return display;
    }
}
