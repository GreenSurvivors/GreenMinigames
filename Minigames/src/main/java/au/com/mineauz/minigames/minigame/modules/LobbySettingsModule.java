package au.com.mineauz.minigames.minigame.modules;

import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.TimeFlag;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.minigame.Minigame;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class LobbySettingsModule extends MinigameModule {
    private final BooleanFlag canMovePlayerWait = new BooleanFlag(true, "canMovePlayerWait");
    private final BooleanFlag canMoveStartWait = new BooleanFlag(true, "canMoveStartWait");
    private final BooleanFlag canInteractPlayerWait = new BooleanFlag(true, "canInteractPlayerWait");
    private final BooleanFlag canInteractStartWait = new BooleanFlag(true, "canInteractStartWait");
    private final BooleanFlag teleportOnPlayerWait = new BooleanFlag(false, "teleportOnPlayerWait");
    private final BooleanFlag teleportOnStart = new BooleanFlag(true, "teleportOnStart");
    private final TimeFlag playerWaitTime = new TimeFlag(0L, "playerWaitTime");

    public LobbySettingsModule(@NotNull Minigame mgm, @NotNull String name) {
        super(mgm, name);
    }

    public static LobbySettingsModule getMinigameModule(Minigame mgm) {
        return ((LobbySettingsModule) mgm.getModule(MgModules.LOBBY_SETTINGS.getName()));
    }

    @Override
    public boolean useSeparateConfig() {
        return false;
    }

    @Override
    public void save(@NotNull FileConfiguration config, @NotNull String path) {
        canInteractPlayerWait.saveValue(config, path);
        canInteractStartWait.saveValue(config, path);
        canMovePlayerWait.saveValue(config, path);
        canMoveStartWait.saveValue(config, path);
        teleportOnPlayerWait.saveValue(config, path);
        playerWaitTime.saveValue(config, path);
    }

    @Override
    public void load(@NotNull FileConfiguration config, @NotNull String path) {
        canInteractPlayerWait.loadValue(config, path);
        canInteractStartWait.loadValue(config, path);
        canMovePlayerWait.loadValue(config, path);
        canMoveStartWait.loadValue(config, path);
        teleportOnPlayerWait.loadValue(config, path);
        playerWaitTime.loadValue(config, path);
    }

    public boolean canMovePlayerWait() {
        return canMovePlayerWait.getFlag();
    }

    public void setCanMovePlayerWait(boolean canMovePlayerWait) {
        this.canMovePlayerWait.setFlag(canMovePlayerWait);
    }

    public Callback<Boolean> getCanMovePlayerWaitCallback() {
        return new Callback<>() {
            @Override
            public Boolean getValue() {
                return canMovePlayerWait.getFlag();
            }

            @Override
            public void setValue(Boolean value) {
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

    public Callback<Boolean> getCanMoveStartWaitCallback() {
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

    public Callback<Boolean> getCanInteractPlayerWaitCallback() {
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

    public Callback<Boolean> getCanInteractStartWaitCallback() {
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

    public Callback<Boolean> getTeleportOnStartCallback() {
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

    public Callback<Boolean> getTeleportOnPlayerWaitCallback() {
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

    public Callback<Long> getPlayerWaitTimeCallback() {
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
