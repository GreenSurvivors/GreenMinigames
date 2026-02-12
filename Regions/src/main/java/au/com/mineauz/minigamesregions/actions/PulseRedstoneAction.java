package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.TimeFlag;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuDisplayTypes;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.time.Duration;
import java.util.Map;

public class PulseRedstoneAction extends AAction {
    private final @NotNull TimeFlag time = new TimeFlag("time", 1L); // in seconds
    private final @NotNull BooleanFlag torch = new BooleanFlag("torch", false);

    protected PulseRedstoneAction(final @NotNull Key key) {
        super(key);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return MessageManager.getMessage(RegionLangKey.MENU_ACTION_PLUSEREDSTONE_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.BLOCK;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(MessageManager.getMessage(RegionLangKey.MENU_ACTION_PLUSEREDSTONE_TIME_NAME),
                MinigameUtils.convertTime(Duration.ofSeconds(time.getFlag()), true),
                MessageManager.getMessage(RegionLangKey.MENU_ACTION_PLUSEREDSTONE_TORCH_NAME),
                MessageManager.getMessage(torch.getFlag() ? MgCommandLangKey.COMMAND_STATE_ENABLED : MgCommandLangKey.COMMAND_STATE_DISABLED));
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
    public void executeRegionAction(final @Nullable MinigamePlayer mgPlayer, final @NotNull Region region) {
        debug(mgPlayer, region);
    }

    @Override
    public void executeNodeAction(final @NotNull MinigamePlayer mgPlayer, final @NotNull Node node) {
        debug(mgPlayer, node);

        if (node.getSafeLocation().getWorld() == null) {
            return;
        }

        final @NotNull BlockData bData;
        if (torch.getFlag()) {
            bData = Material.REDSTONE_TORCH.createBlockData();

            if (bData instanceof Lightable lightable) {
                lightable.setLit(true);
            }
        } else {
            bData = Material.REDSTONE_BLOCK.createBlockData();
        }
        final BlockState last = node.getSafeLocation().getBlockAt().getState(true);
        node.getSafeLocation().getBlockAt().setBlockData(bData);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Minigames.getPlugin(), () ->
            last.update(true), 20L * time.getFlag());
    }

    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        time.saveValue(config);
        torch.saveValue(config);
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
        time.loadValue(config);
        torch.loadValue(config);
    }

    @Override
    public boolean displayMenu(final @NotNull Menu previous) {
        final @NotNull Menu menu = new Menu(3, getDisplayname(), previous.getIntendedViewer());
        menu.setItem(new MenuItemBack(previous), menu.getSize() - 9);
        menu.addItem(time.getMenuItem(MenuDisplayTypes.timeType(), MessageManager.getMessage(RegionLangKey.MENU_ACTION_PLUSEREDSTONE_TIME_NAME), 0L, null));
        menu.addItem(torch.getMenuItem(ItemType.REDSTONE_BLOCK, MessageManager.getMessage(RegionLangKey.MENU_ACTION_PLUSEREDSTONE_TORCH_NAME)));
        menu.displayMenu();
        return true;
    }
}
