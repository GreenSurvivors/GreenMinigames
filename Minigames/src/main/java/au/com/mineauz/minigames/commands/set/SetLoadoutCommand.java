package au.com.mineauz.minigames.commands.set;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigames.menu.MenuItemDisplayLoadout;
import au.com.mineauz.minigames.menu.MenuItemLoadoutAdd;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.MgDefaultModules;
import au.com.mineauz.minigames.minigame.modules.loadout.LoadoutModule;
import au.com.mineauz.minigames.minigame.modules.loadout.PlayerLoadout;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SetLoadoutCommand extends ASetCommand {

    @Override
    public @NotNull String getName() {
        return "loadout";
    }

    @Override
    public boolean canBeConsole() {
        return false;
    }

    @Override
    public @NotNull Component getDescription() {
        return MinigameMessageManager.getMgMessage(MgCommandLangKey.COMMAND_SET_LOADOUT_DESCRIPTION);
    }

    @Override
    public @NotNull Component getUsage() {
        return MinigameMessageManager.getMgMessage(MgCommandLangKey.COMMAND_SET_LOADOUT_USAGE);
    }

    @Override
    public @Nullable String getPermission() {
        return "minigame.set.loadout";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Minigame minigame,
                             @NotNull String @Nullable [] args) {

        if (sender instanceof Player player) {
            MinigamePlayer mgPlayer = Minigames.getPlugin().getPlayerManager().getMinigamePlayer(player);
            Menu loadoutMenu = new Menu(6, Component.text(getName()), mgPlayer);
            List<MenuItem> mi = new ArrayList<>();
            LoadoutModule mod = LoadoutModule.getMinigameModule(minigame);

            if (mod != null) {
                ItemType displayType;
                for (final @NotNull PlayerLoadout loadout : mod.getLoadouts()) {
                    displayType = ItemType.WHITE_STAINED_GLASS_PANE;
                    if (!loadout.getItemSlots().isEmpty()) {
                        displayType = loadout.getItem((Integer) loadout.getItemSlots().toArray()[0]).getType().asItemType();
                    }

                    MenuItemDisplayLoadout mil = new MenuItemDisplayLoadout(displayType, loadout.getDisplayName(),
                            MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_DELETE_SHIFTRIGHTCLICK), loadout, minigame);

                    mil.setAllowDelete(loadout.isDeletable());
                    mi.add(mil);
                }
                loadoutMenu.addItem(new MenuItemLoadoutAdd(ItemType.ITEM_FRAME, MgMenuLangKey.MENU_LOADOUT_ADD_NAME,
                        mod.getLoadoutMap(), minigame), 53);
                loadoutMenu.addItems(mi);

                loadoutMenu.displayMenu(mgPlayer);
            } else {
                MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTGAMEMECHANIC,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                        Placeholder.unparsed(MinigamePlaceHolderKey.TYPE.getKey(), MgDefaultModules.LOADOUT.getKey().value()));
            }
        } else {
            MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_SENDERNOTAPLAYER);
        }

        return true;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(@NotNull CommandSender sender, @NotNull Minigame minigame,
                                                         @NotNull String @NotNull [] args) {
        return null;
    }
}
