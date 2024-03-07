package au.com.mineauz.minigames.minigame.modules;

import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.minigame.Minigame;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class CTFModule extends MinigameModule {
    private final BooleanFlag useFlagAsCapturePoint = new BooleanFlag(true, "useFlagAsCapturePoint");
    private final BooleanFlag bringFlagBackManual = new BooleanFlag(false, "bringFlagBackManual");

    public CTFModule(@NotNull Minigame mgm, @NotNull String name) {
        super(mgm, name);
    }

    public static CTFModule getMinigameModule(Minigame mgm) {
        return ((CTFModule) mgm.getModule(MgModules.INFECTION.getName()));
    }

    public Boolean getUseFlagAsCapturePoint() {
        return useFlagAsCapturePoint.getFlag();
    }

    public void setUseFlagAsCapturePoint(boolean useFlagAsCapturePoint) {
        this.useFlagAsCapturePoint.setFlag(useFlagAsCapturePoint);
    }

    public Boolean getBringFlagBackManual() {
        return bringFlagBackManual.getFlag();
    }

    public void setBringFlagBackManual(boolean bringFlagBackManual) {
        this.bringFlagBackManual.setFlag(bringFlagBackManual);
    }

    @Override
    public boolean useSeparateConfig() {
        return false;
    }

    @Override
    public void save(@NotNull FileConfiguration config, @NotNull String path) {
        useFlagAsCapturePoint.saveValue(config, path);
        bringFlagBackManual.saveValue(config, path);
    }

    @Override
    public void load(@NotNull FileConfiguration config, @NotNull String path) {
        useFlagAsCapturePoint.loadValue(config, path);
        bringFlagBackManual.loadValue(config, path);
    }

    @Override
    public void addEditMenuOptions(@NotNull Menu menu) {
    }

    @Override
    public boolean displayMechanicSettings(@NotNull Menu previous) {
        Menu menu = new Menu(6, MgMenuLangKey.MENU_CTF_NAME, previous.getViewer());
        menu.addItem(new MenuItemBack(previous), menu.getSize() - 9);

        menu.addItem(useFlagAsCapturePoint.getMenuItem(Material.BLACK_BANNER, MgMenuLangKey.MENU_CTF_CAPTUREPOINT_NAME,
                MgMenuLangKey.MENU_CTF_CAPTUREPOINT_DESCRIPTION));
        menu.addItem(bringFlagBackManual.getMenuItem(Material.ENDER_EYE, MgMenuLangKey.MENU_CTF_FLAGBACKMANUALLY_NAME,
                MgMenuLangKey.MENU_CTF_FLAGBACKMANUALLY_DESCRIPTION));
        menu.displayMenu(previous.getViewer());
        return true;
    }
}

