package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.config.TimeFlag;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.RegionMessageManager;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MinigameTimerCondition extends ACondition {
    private final TimeFlag minTime = new TimeFlag(5L, "minTime");
    private final TimeFlag maxTime = new TimeFlag(10L, "maxTime");

    protected MinigameTimerCondition(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_MINIGAMETIMER_NAME);
    }

    @Override
    public String getCategory() {
        return "Minigame ConditionRegistry";
    }

    @Override
    public void describe(Map<String, Object> out) {
        out.put("Time", MinigameUtils.convertTime(minTime.getFlag(), true) + " - " + MinigameUtils.convertTime(maxTime.getFlag(), true));
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
    public boolean checkRegionCondition(MinigamePlayer player, Region region) {
        return check(player.getMinigame());
    }

    @Override
    public boolean checkNodeCondition(MinigamePlayer player, Node node) {
        return check(player.getMinigame());
    }

    private boolean check(Minigame mg) {
        long timeLeft = mg.getMinigameTimer().getTimeLeft();
        long min = minTime.getFlag();
        long max = maxTime.getFlag();
        debug(mg);
        return timeLeft >= min && timeLeft <= max;
    }

    @Override
    public void saveArguments(FileConfiguration config, String path) {
        minTime.saveValue(path, config);
        maxTime.saveValue(path, config);
        saveInvert(config, path);
    }

    @Override
    public void loadArguments(FileConfiguration config, String path) {
        minTime.loadValue(path, config);
        maxTime.loadValue(path, config);
        loadInvert(config, path);
    }

    @Override
    public boolean displayMenu(MinigamePlayer player, Menu prev) {
        Menu m = new Menu(3, "Minigame Timer", player);

        m.addItem(minTime.getMenuItem("Min Time", Material.CLOCK, 0L, null));
        m.addItem(maxTime.getMenuItem("Max Time", Material.CLOCK, 0L, null));

        m.addItem(new MenuItemBack(prev), m.getSize() - 9);
        addInvertMenuItem(m);
        m.displayMenu(player);
        return true;
    }

    @Override
    public boolean onPlayerApplicable() {
        return true;
    }
}
