package au.com.mineauz.minigames.commands;

import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.minigame.modules.loadout.LoadoutModule;
import au.com.mineauz.minigames.minigame.modules.loadout.PlayerLoadout;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;

public class GlobalLoadoutCommand extends ACommand {

    @Override
    public @NotNull String getName() {
        return "globalloadout";
    }

    @Override
    public @NotNull String @Nullable [] getAliases() {
        return new String[]{"gloadout"};
    }

    @Override
    public boolean canBeConsole() {
        return false;
    }

    @Override
    public @NotNull Component getDescription() {
        return MessageManager.getMessage(MgCommandLangKey.COMMAND_GLOBALLOADOUT_DESCRIPTION);
    }

    @Override
    public @NotNull Component getUsage() {
        return MessageManager.getMessage(MgCommandLangKey.COMMAND_GLOBALLOADOUT_USAGE);
    }

    @Override
    public @Nullable String getPermission() {
        return "minigame.globalloadout";
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender,
                             final @NotNull String @NotNull [] args) {
        if (sender instanceof final @NotNull Player player) {
            final @NotNull MinigamePlayer mgPlayer = PLUGIN.getPlayerManager().getMinigamePlayer(player);
            final @NotNull Menu globalLoadoutMenu = new Menu(6, MgMenuLangKey.MENU_GLOBALLOADOUT_NAME, mgPlayer);
            final @NotNull List<@NotNull AMenuItem> menuItems = new ArrayList<>();

            for (final @NotNull PlayerLoadout globalLoadout : LoadoutModule.getGlobalLoadouts()) {
                @UnknownNullability ItemType displayType = MenuDisplayTypes.unknownType();
                if (!globalLoadout.getItemSlots().isEmpty()) {
                    displayType = globalLoadout.getItem((Integer) globalLoadout.getItemSlots().toArray()[0]).getType().asItemType();
                }
                menuItems.add(new MenuItemDisplayLoadout(displayType, globalLoadout.getDisplayName(),
                    MessageManager.getMessageList(MgMenuLangKey.MENU_DELETE_SHIFTRIGHTCLICK), globalLoadout));
            }
            globalLoadoutMenu.setItem(new MenuItemLoadoutAdd(MenuDisplayTypes.createType(), MgMenuLangKey.MENU_LOADOUT_ADD_NAME,
                LoadoutModule.getGlobalLoadoutMap()), 53);
            globalLoadoutMenu.addItems(menuItems);

            globalLoadoutMenu.displayMenu();
        } else {
            MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_SENDERNOTAPLAYER);
        }

        return true;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(@NotNull CommandSender sender, @NotNull String @Nullable [] args) {
        return null;
    }
}
