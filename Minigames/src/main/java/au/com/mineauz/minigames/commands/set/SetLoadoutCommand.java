package au.com.mineauz.minigames.commands.set;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.*;
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
        return MessageManager.getMessage(MgCommandLangKey.COMMAND_SET_LOADOUT_DESCRIPTION);
    }

    @Override
    public @NotNull Component getUsage() {
        return MessageManager.getMessage(MgCommandLangKey.COMMAND_SET_LOADOUT_USAGE);
    }

    @Override
    public @Nullable String getPermission() {
        return "minigame.set.loadout";
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Minigame minigame,
                             final @NotNull String @Nullable [] args) {
        if (sender instanceof final @NotNull Player player) {
            final @NotNull MinigamePlayer mgPlayer = Minigames.getPlugin().getPlayerManager().getMinigamePlayer(player);
            final @NotNull Menu loadoutMenu = new Menu(6, Component.text(getName()), mgPlayer);
            final @NotNull List<@NotNull AMenuItem> menuItems = new ArrayList<>();
            final @Nullable LoadoutModule loadoutModule = LoadoutModule.getMinigameModule(minigame);

            if (loadoutModule != null) {
                @NotNull ItemType displayType;
                for (final @NotNull PlayerLoadout loadout : loadoutModule.getLoadouts()) {
                    displayType = MenuDisplayTypes.unknownType();
                    if (!loadout.getItemSlots().isEmpty()) {
                        displayType = loadout.getItem((Integer) loadout.getItemSlots().toArray()[0]).getType().asItemType();
                    }

                    MenuItemDisplayLoadout mil = new MenuItemDisplayLoadout(displayType, loadout.getDisplayName(),
                            MessageManager.getMessageList(MgMenuLangKey.MENU_DELETE_SHIFTRIGHTCLICK), loadout, minigame);

                    mil.setAllowDelete(loadout.isDeletable());
                    menuItems.add(mil);
                }
                loadoutMenu.setItem(new MenuItemLoadoutAdd(MenuDisplayTypes.createType(), MgMenuLangKey.MENU_LOADOUT_ADD_NAME,
                        loadoutModule.getLoadoutMap(), minigame), 53);
                loadoutMenu.addItems(menuItems);

                loadoutMenu.displayMenu();
            } else {
                MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTGAMEMECHANIC,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                        Placeholder.unparsed(MinigamePlaceHolderKey.TYPE.getKey(), MgDefaultModules.LOADOUT.getKey().value()));
            }
        } else {
            MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_SENDERNOTAPLAYER);
        }

        return true;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(final @NotNull CommandSender sender, final @NotNull Minigame minigame,
                                                         final @NotNull String @NotNull [] args) {
        return null;
    }
}
