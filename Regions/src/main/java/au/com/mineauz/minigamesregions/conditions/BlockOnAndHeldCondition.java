package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.util.Map;

/**
 * This class allows a check if the first solid block under a player is equal
 * to the block that player holds in his hand.
 */
public class BlockOnAndHeldCondition extends ACondition {

    protected BlockOnAndHeldCondition(final @NotNull Key key) {
        super(key);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_BLOCKONANDHELD_NAME);
    }

    @Override
    public @NotNull IConditionCategory getCategory() {
        return RegionConditionCategories.WORLD;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of();
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
    public boolean checkRegionCondition(final MinigamePlayer mgPlayer, final @NotNull Region region) {
        return false;
    }

    @Override
    public boolean checkNodeCondition(final @Nullable MinigamePlayer mgPlayer, final @NotNull Node node) {
        return check(mgPlayer);
    }

    private boolean check(final @Nullable MinigamePlayer mgPlayer) {
        if (mgPlayer == null) {
            return false;
        }

        final @Nullable Player player = mgPlayer.getPlayer();
        if (player == null) {
            return false;
        }

        final @NotNull ItemStack heldItem = player.getInventory().getItemInMainHand();

        if (heldItem.isEmpty()) {
            return false;
        }

        final @NotNull World world = player.getWorld();

        final @NotNull Location plyLoc = player.getLocation();
        int plyY = plyLoc.getBlockY();
        //In case that the player is in the air, this searches for the first solid block below and checks if it is equal
        while (plyY >= 0) {
            plyY -= 1;
            final @NotNull Block tempBlock = world.getBlockAt(plyLoc.getBlockX(), plyY, plyLoc.getBlockZ());

            if (tempBlock.getType().asBlockType().equals(heldItem.getType().asBlockType())) {
                return true;
            } else if (!tempBlock.isEmpty()) {
                return false;
            }
        }

        return false;
    }

    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) {
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
    }

    @Override
    public boolean displayMenu(final @NotNull Menu prev) {
        final @NotNull Menu menu = new Menu(3, getDisplayName(), prev.getIntendedViewer());
        menu.setItem(new MenuItemBack(prev), menu.getSize() - 9);
        addInvertMenuItem(menu);
        menu.displayMenu();
        return true;
    }

    @Override
    public boolean playerNeeded() {
        return true;
    }
}
