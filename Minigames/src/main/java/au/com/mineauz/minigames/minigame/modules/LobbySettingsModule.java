package au.com.mineauz.minigames.minigame.modules;

import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.TimeFlag;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.minigame.Minigame;
import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;

public class LobbySettingsModule extends AMinigameModule {
    private final @NotNull BooleanFlag canMoveOnPlayerWait = new BooleanFlag("canMovePlayerWait", true);
    private final @NotNull BooleanFlag canMoveOnStartWait = new BooleanFlag("canMoveStartWait", true);
    private final @NotNull BooleanFlag canInteractPlayerWait = new BooleanFlag("canInteractPlayerWait", true);
    private final @NotNull BooleanFlag canInteractStartWait = new BooleanFlag("canInteractStartWait", true);
    private final @NotNull BooleanFlag teleportOnPlayerWait = new BooleanFlag("teleportOnPlayerWait", false);
    private final @NotNull BooleanFlag teleportOnStart = new BooleanFlag("teleportOnStart", true);
    private final @NotNull TimeFlag playerWaitTime = new TimeFlag("playerWaitTime", 0L);

    public LobbySettingsModule(final @NotNull Minigame mgm, final @NotNull Key key) {
        super(mgm, key);
    }

    public static LobbySettingsModule getMinigameModule(final @NotNull Minigame mgm) {
        return ((LobbySettingsModule) mgm.getModule(MgDefaultModules.LOBBY_SETTINGS.getKey()));
    }

    @Override
    public boolean useSeparateConfig() {
        return false;
    }

    @Override
    public void save(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        canInteractPlayerWait.saveValue(config);
        canInteractStartWait.saveValue(config);
        canMoveOnPlayerWait.saveValue(config);
        canMoveOnStartWait.saveValue(config);
        teleportOnPlayerWait.saveValue(config);
        playerWaitTime.saveValue(config);
    }

    @Override
    public void load(final @NotNull CommentedConfigurationNode config) {
        canInteractPlayerWait.loadValue(config);
        canInteractStartWait.loadValue(config);
        canMoveOnPlayerWait.loadValue(config);
        canMoveOnStartWait.loadValue(config);
        teleportOnPlayerWait.loadValue(config);
        playerWaitTime.loadValue(config);
    }

    public boolean canMoveOnPlayerWait() {
        return canMoveOnPlayerWait.getFlag();
    }

    public void setCanMoveOnPlayerWait(final boolean canMoveOnPlayerWait) {
        this.canMoveOnPlayerWait.setFlag(canMoveOnPlayerWait);
    }

    public boolean canMoveOnStartWait() {
        return canMoveOnStartWait.getFlag();
    }

    public void setCanMoveOnStartWait(final boolean canMoveOnStartWait) {
        this.canMoveOnStartWait.setFlag(canMoveOnStartWait);
    }

    public boolean canInteractPlayerWait() {
        return canInteractPlayerWait.getFlag();
    }

    public void setCanInteractPlayerWait(final boolean canInteractPlayerWait) {
        this.canInteractPlayerWait.setFlag(canInteractPlayerWait);
    }

    public boolean canInteractStartWait() {
        return canInteractStartWait.getFlag();
    }

    public void setCanInteractStartWait(final boolean canInteractStartWait) {
        this.canInteractStartWait.setFlag(canInteractStartWait);
    }

    public boolean isTeleportOnStart() {
        return teleportOnStart.getFlag();
    }

    public void setTeleportOnStart(final boolean teleportOnStart) {
        this.teleportOnStart.setFlag(teleportOnStart);
    }

    public boolean isTeleportOnPlayerWait() {
        return teleportOnPlayerWait.getFlag();
    }

    public void setTeleportOnPlayerWait(final boolean teleportOnPlayerWait) {
        this.teleportOnPlayerWait.setFlag(teleportOnPlayerWait);
    }

    /**
     * in seconds
     */
    public long getPlayerWaitTime() {
        return playerWaitTime.getFlag();
    }

    /**
     * in seconds
     */
    public void setPlayerWaitTime(final long time) {
        playerWaitTime.setFlag(time);
    }

    @Override
    public void addEditMenuOptions(final @NotNull Menu superMenu) {
        if (getMinigame().getType() == MinigameType.MULTIPLAYER) {
            final @NotNull Menu lobbyMenu = new Menu(6, getMinigame().getDisplayName(), superMenu.getIntendedViewer());

            final @NotNull List<@NotNull AMenuItem> itemsLobby = new ArrayList<>(4);

            itemsLobby.add(canInteractPlayerWait.getMenuItem(ItemType.STONE_BUTTON, MgMenuLangKey.MENU_LOBBY_WAIT_PLAYER_INTERACT_NAME));
            itemsLobby.add(canInteractStartWait.getMenuItem(ItemType.STONE_BUTTON, MgMenuLangKey.MENU_LOBBY_WAIT_START_INTERACT_NAME));
            itemsLobby.add(canMoveOnPlayerWait.getMenuItem(ItemType.ICE, MgMenuLangKey.MENU_LOBBY_WAIT_PLAYER_MOVE_NAME));
            itemsLobby.add(canMoveOnStartWait.getMenuItem(ItemType.ICE, MgMenuLangKey.MENU_LOBBY_WAIT_START_MOVE_NAME));
            itemsLobby.add(teleportOnPlayerWait.getMenuItem(ItemType.ENDER_PEARL, MgMenuLangKey.MENU_LOBBY_WAIT_PLAYER_TELEPORT_NAME));
            itemsLobby.add(teleportOnStart.getMenuItem(ItemType.ENDER_PEARL, MgMenuLangKey.MENU_LOBBY_WAIT_START_TELEPORT_NAME,
                MgMenuLangKey.MENU_LOBBY_WAIT_START_TELEPORT_DESCRIPTION));
            itemsLobby.add(playerWaitTime.getMenuItem(MenuDisplayTypes.timeType(), MgMenuLangKey.MENU_LOBBY_WAIT_PLAYER_TIME_NAME,
                MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_LOBBY_WAIT_PLAYER_TIME_DESCRIPTION),
                0L, Long.MAX_VALUE));
            lobbyMenu.addItems(itemsLobby);
            lobbyMenu.setItem(new MenuItemBack(superMenu), lobbyMenu.getSize() - 9);

            final @NotNull MenuItemPage lobbySettingsMenuItemPage = new MenuItemPage(ItemType.OAK_DOOR, MgMenuLangKey.MENU_MINIGAME_LOBBY_SETTINGS_NAME, lobbyMenu);

            if (getMinigame().getType() == MinigameType.MULTIPLAYER) {
                superMenu.setItem(lobbySettingsMenuItemPage, 15);
            }
        }
    }
}
