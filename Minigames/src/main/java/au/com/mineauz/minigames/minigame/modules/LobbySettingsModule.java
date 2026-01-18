package au.com.mineauz.minigames.minigame.modules;

import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.TimeFlag;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.minigame.Minigame;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class LobbySettingsModule extends MinigameModule {
    private final BooleanFlag canMovePlayerWait = new BooleanFlag("canMovePlayerWait", true);
    private final BooleanFlag canMoveStartWait = new BooleanFlag("canMoveStartWait", true);
    private final BooleanFlag canInteractPlayerWait = new BooleanFlag("canInteractPlayerWait", true);
    private final BooleanFlag canInteractStartWait = new BooleanFlag("canInteractStartWait", true);
    private final BooleanFlag teleportOnPlayerWait = new BooleanFlag("teleportOnPlayerWait", false);
    private final BooleanFlag teleportOnStart = new BooleanFlag("teleportOnStart", true);
    private final TimeFlag playerWaitTime = new TimeFlag("playerWaitTime", 0L);

    public LobbySettingsModule(final @NotNull Minigame mgm, final @NotNull Key key) {
        super(mgm, key);
    }

    public static LobbySettingsModule getMinigameModule(Minigame mgm) {
        return ((LobbySettingsModule) mgm.getModule(MgModules.LOBBY_SETTINGS.getKey()));
    }

    @Override
    public boolean useSeparateConfig() {
        return false;
    }

    @Override
    public void save(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        canInteractPlayerWait.saveValue(config);
        canInteractStartWait.saveValue(config);
        canMovePlayerWait.saveValue(config);
        canMoveStartWait.saveValue(config);
        teleportOnPlayerWait.saveValue(config);
        playerWaitTime.saveValue(config);
    }

    @Override
    public void load(final @NotNull CommentedConfigurationNode config) {
        canInteractPlayerWait.loadValue(config);
        canInteractStartWait.loadValue(config);
        canMovePlayerWait.loadValue(config);
        canMoveStartWait.loadValue(config);
        teleportOnPlayerWait.loadValue(config);
        playerWaitTime.loadValue(config);
    }

    public boolean canMovePlayerWait() {
        return canMovePlayerWait.getFlag();
    }

    public void setCanMovePlayerWait(boolean canMovePlayerWait) {
        this.canMovePlayerWait.setFlag(canMovePlayerWait);
    }

    public @NotNull Callback<@NotNull Boolean> getCanMovePlayerWaitCallback() {
        return new Callback<>() {
            @Override
            public @NotNull Boolean getValue() {
                return canMovePlayerWait.getFlag();
            }

            @Override
            public void setValue(@NotNull Boolean value) {
                canMovePlayerWait.setFlag(value);
            }
        };
    }

    public boolean canMoveStartWait() {
        return canMoveStartWait.getFlag();
    }

    public void setCanMoveStartWait(boolean canMoveStartWait) {
        this.canMoveStartWait.setFlag(canMoveStartWait);
    }

    public @NotNull Callback<Boolean> getCanMoveStartWaitCallback() {
        return new Callback<>() {
            @Override
            public Boolean getValue() {
                return canMoveStartWait.getFlag();
            }

            @Override
            public void setValue(Boolean value) {
                canMoveStartWait.setFlag(value);
            }
        };
    }

    public boolean canInteractPlayerWait() {
        return canInteractPlayerWait.getFlag();
    }

    public void setCanInteractPlayerWait(boolean canInteractPlayerWait) {
        this.canInteractPlayerWait.setFlag(canInteractPlayerWait);
    }

    public @NotNull Callback<Boolean> getCanInteractPlayerWaitCallback() {
        return new Callback<>() {
            @Override
            public Boolean getValue() {
                return canInteractPlayerWait.getFlag();
            }

            @Override
            public void setValue(Boolean value) {
                canInteractPlayerWait.setFlag(value);
            }
        };
    }

    public boolean canInteractStartWait() {
        return canInteractStartWait.getFlag();
    }

    public void setCanInteractStartWait(boolean canInteractStartWait) {
        this.canInteractStartWait.setFlag(canInteractStartWait);
    }

    public @NotNull Callback<Boolean> getCanInteractStartWaitCallback() {
        return new Callback<>() {
            @Override
            public Boolean getValue() {
                return canInteractStartWait.getFlag();
            }

            @Override
            public void setValue(Boolean value) {
                canInteractStartWait.setFlag(value);
            }
        };
    }

    public boolean isTeleportOnStart() {
        return teleportOnStart.getFlag();
    }

    public void setTeleportOnStart(boolean teleportOnStart) {
        this.teleportOnStart.setFlag(teleportOnStart);
    }

    public @NotNull Callback<Boolean> getTeleportOnStartCallback() {
        return new Callback<>() {
            @Override
            public Boolean getValue() {
                return teleportOnStart.getFlag();
            }

            @Override
            public void setValue(Boolean value) {
                teleportOnStart.setFlag(value);
            }
        };
    }

    public boolean isTeleportOnPlayerWait() {
        return teleportOnPlayerWait.getFlag();
    }

    public void setTeleportOnPlayerWait(boolean teleportOnPlayerWait) {
        this.teleportOnPlayerWait.setFlag(teleportOnPlayerWait);
    }

    public @NotNull Callback<Boolean> getTeleportOnPlayerWaitCallback() {
        return new Callback<>() {
            @Override
            public Boolean getValue() {
                return teleportOnPlayerWait.getFlag();
            }

            @Override
            public void setValue(Boolean value) {
                teleportOnPlayerWait.setFlag(value);
            }
        };
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
    public void setPlayerWaitTime(long time) {
        playerWaitTime.setFlag(time);
    }

    public @NotNull Callback<Long> getPlayerWaitTimeCallback() {
        return new Callback<>() {
            @Override
            public Long getValue() {
                return playerWaitTime.getFlag();
            }

            @Override
            public void setValue(Long value) {
                playerWaitTime.setFlag(value);
            }
        };
    }

    @Override
    public void addEditMenuOptions(@NotNull Menu menu) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean displayMechanicSettings(@NotNull Menu previous) {
        return false;
    }
}
