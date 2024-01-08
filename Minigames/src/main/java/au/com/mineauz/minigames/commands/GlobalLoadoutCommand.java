package au.com.mineauz.minigames.commands;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.managers.MinigameManager;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigames.menu.MenuItemDisplayLoadout;
import au.com.mineauz.minigames.menu.MenuItemLoadoutAdd;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GlobalLoadoutCommand implements ICommand {
    private final MinigameManager mdata = Minigames.getPlugin().getMinigameManager();

    @Override
    public @NotNull String getName() {
        return "globalloadout";
    }

    @Override
    public @NotNull String @Nullable [] getAliases() {
        return new String[]{"gloadout", "loadout"};
    }

    @Override
    public boolean canBeConsole() {
        return false;
    }

    @Override
    public @NotNull Component getDescription() {
        return "Opens the Loadout edit window for Global Loadouts. These loadouts may be used in any Minigame.";
    }

    @Override
    public @NotNull String @Nullable [] getParameters() {
        return null;
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "/minigame globalloadout"
        };
    }

    @Override
    public String getPermissionMessage() {
        return "You do not have permission to edit global loadouts!";
    }

    @Override
    public @Nullable String getPermission() {
        return "minigame.globalloadout";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Minigame minigame,
                             @NotNull String label, @NotNull String @Nullable [] args) {
        MinigamePlayer player = Minigames.getPlugin().getPlayerManager().getMinigamePlayer((Player) sender);
        Menu loadouts = new Menu(6, getName(), player);

        List<String> des = new ArrayList<>();
        des.add("Shift + Right Click to Delete");
        List<MenuItem> mi = new ArrayList<>();
        for (String ld : mdata.getLoadouts()) {
            Material item = Material.WHITE_STAINED_GLASS_PANE;
            if (!mdata.getLoadout(ld).getItems().isEmpty()) {
                item = mdata.getLoadout(ld).getItem((Integer) mdata.getLoadout(ld).getItems().toArray()[0]).getType();
            }
            mi.add(new MenuItemDisplayLoadout(ld, des, item, mdata.getLoadout(ld)));
        }
        loadouts.addItem(new MenuItemLoadoutAdd("Add Loadout", Material.ITEM_FRAME, mdata.getLoadoutMap()), 53);
        loadouts.addItems(mi);

        loadouts.displayMenu(player);
        return true;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(@NotNull CommandSender sender, Minigame minigame,
                                                         String alias, @NotNull String @NotNull [] args) {
        return null;
    }

}
