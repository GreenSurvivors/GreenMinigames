package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.TimeFlag;
import au.com.mineauz.minigames.managers.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.RegionMessageManager;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Map;

public class PulseRedstoneAction extends AAction {
    private final TimeFlag time = new TimeFlag(1L, "time"); // in seconds
    private final BooleanFlag torch = new BooleanFlag(false, "torch");

    protected PulseRedstoneAction(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_PLUSEREDSTONE_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.BLOCK;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable ComponentLike> describe() {
        return Map.of(RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_PLUSEREDSTONE_TIME_NAME),
                MinigameUtils.convertTime(Duration.ofSeconds(time.getFlag()), true),
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_PLUSEREDSTONE_TORCH_NAME),
                MinigameMessageManager.getMgMessage(torch.getFlag() ? MgCommandLangKey.COMMAND_STATE_ENABLED : MgCommandLangKey.COMMAND_STATE_DISABLED));
    }

    @Override
    public boolean useInRegions() {
        return false;
    }

    @Override
    public boolean useInNodes() {
        return true;
    }

    @Override
    public void executeRegionAction(@Nullable MinigamePlayer mgPlayer, @NotNull Region region) {
        debug(mgPlayer, region);
    }

    @Override
    public void executeNodeAction(@NotNull MinigamePlayer mgPlayer, @NotNull Node node) {
        debug(mgPlayer, node);
        BlockData bData;
        if (torch.getFlag()) {
            bData = Material.REDSTONE_TORCH.createBlockData();

            if (bData instanceof Lightable lightable) {
                lightable.setLit(true);
            }
        } else {
            bData = Material.REDSTONE_BLOCK.createBlockData();
        }
        final BlockState last = node.getLocation().getBlock().getState();
        node.getLocation().getBlock().setBlockData(bData);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Minigames.getPlugin(), () ->
                last.update(true), 20L * time.getFlag());
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        time.saveValue(config, path);
        torch.saveValue(config, path);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        time.loadValue(config, path);
        torch.loadValue(config, path);
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer mgPlayer, Menu previous) {
        Menu menu = new Menu(3, getDisplayname(), mgPlayer);
        menu.addItem(new MenuItemBack(previous), menu.getSize() - 9);
        menu.addItem(time.getMenuItem(Material.CLOCK, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_PLUSEREDSTONE_TIME_NAME), 0L, null));
        menu.addItem(torch.getMenuItem(Material.REDSTONE_BLOCK, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_PLUSEREDSTONE_TORCH_NAME)));
        menu.displayMenu(mgPlayer);
        return true;
    }

}
