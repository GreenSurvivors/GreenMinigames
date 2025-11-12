package au.com.mineauz.minigames.minigame.modules;

import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.CTFFlag;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CTFModule extends MinigameModule {
    // config
    private final BooleanFlag useFlagAsCapturePoint = new BooleanFlag("useFlagAsCapturePoint", true);
    private final BooleanFlag bringFlagBackManual = new BooleanFlag("bringFlagBackManual", false);
    private final BooleanFlag carryFlagAsItem = new BooleanFlag("carryFlagAsItem", false);
    // runtime
    private final Map<MinigamePlayer, CTFFlag> flagCarriers = new HashMap<>();
    private final Map<String, CTFFlag> droppedFlag = new HashMap<>();

    public CTFModule(final @NotNull Minigame mgm, final @NotNull String name) {
        super(mgm, name);
    }

    public static CTFModule getMinigameModule(final @NotNull Minigame mgm) {
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

    public void setBringFlagBackManual(final boolean bringFlagBackManual) {
        this.bringFlagBackManual.setFlag(bringFlagBackManual);
    }

    public Boolean shouldCarryFlagAsItem() {
        return carryFlagAsItem.getFlag();
    }

    public void setCarryFlagAsItem (final boolean carryFlagAsItem) {
        this.carryFlagAsItem.setFlag(carryFlagAsItem);
    }

    public boolean isFlagCarrier(final @NotNull MinigamePlayer mgPlayer) {
        return flagCarriers.containsKey(mgPlayer);
    }

    public void addFlagCarrier(final @NotNull MinigamePlayer mgPlayer, final @NotNull CTFFlag flag) {
        flagCarriers.put(mgPlayer, flag);

        if (shouldCarryFlagAsItem()) {
            mgPlayer.getPlayer().getInventory().addItem(flag.getAsItem());
        }
    }

    public void removeFlagCarrier(final @NotNull MinigamePlayer mgPlayer) {
        final @Nullable CTFFlag flag = flagCarriers.remove(mgPlayer);

        if (shouldCarryFlagAsItem() && flag != null) {
            final PlayerInventory inventory = mgPlayer.getPlayer().getInventory();
            final ItemStack[] items = inventory.getStorageContents();

            for (int i = 0; i < items.length; i++) {
                if (flag.isFlag(items[i])){
                    inventory.setItem(i, ItemStack.empty());
                }
            }
        }
    }

    public CTFFlag getCarriedFlag(final @NotNull MinigamePlayer ply) {
        return flagCarriers.get(ply);
    }

    public void resetFlags() {
        for (CTFFlag ctfFlag : flagCarriers.values()) {
            ctfFlag.respawnFlag();
            ctfFlag.stopCarrierParticleEffect();
        }
        flagCarriers.clear();
        for (String id : droppedFlag.keySet()) {
            if (!getDroppedFlag(id).isAtHome()) {
                getDroppedFlag(id).stopTimer();
                getDroppedFlag(id).respawnFlag();
            }
        }
        droppedFlag.clear();
    }

    public boolean hasDroppedFlag(final @NotNull String id) {
        return droppedFlag.containsKey(id);
    }

    public void addDroppedFlag(final @NotNull String id, final @NotNull CTFFlag flag) {
        droppedFlag.put(id, flag);
    }

    public void removeDroppedFlag(final @NotNull String id) {
        droppedFlag.remove(id);
    }

    public @Nullable CTFFlag getDroppedFlag(final @NotNull String id) {
        return droppedFlag.get(id);
    }

    public @NotNull Collection<@NotNull CTFFlag> getAllDroppedFlags() {
        return droppedFlag.values();
    }

    @Override
    public boolean useSeparateConfig() {
        return false;
    }

    @Override
    public void save(@NotNull FileConfiguration config, @NotNull String path) {
        useFlagAsCapturePoint.saveValue(config, path);
        bringFlagBackManual.saveValue(config, path);
        carryFlagAsItem.saveValue(config, path);
    }

    @Override
    public void load(@NotNull FileConfiguration config, @NotNull String path) {
        useFlagAsCapturePoint.loadValue(config, path);
        bringFlagBackManual.loadValue(config, path);
        carryFlagAsItem.loadValue(config, path);
    }

    @Override
    public void addEditMenuOptions(@NotNull Menu menu) {
    }

    @Override
    public boolean displayMechanicSettings(final @NotNull Menu previous) {
        Menu menu = new Menu(6, MgMenuLangKey.MENU_CTF_NAME, previous.getViewer());
        menu.addItem(new MenuItemBack(previous), menu.getSize() - 9);

        menu.addItem(useFlagAsCapturePoint.getMenuItem(Material.BLACK_BANNER, MgMenuLangKey.MENU_CTF_CAPTUREPOINT_NAME,
                MgMenuLangKey.MENU_CTF_CAPTUREPOINT_DESCRIPTION));
        menu.addItem(bringFlagBackManual.getMenuItem(Material.ENDER_EYE, MgMenuLangKey.MENU_CTF_FLAGBACKMANUALLY_NAME,
                MgMenuLangKey.MENU_CTF_FLAGBACKMANUALLY_DESCRIPTION));
        menu.addItem(carryFlagAsItem.getMenuItem(Material.OAK_SIGN, MgMenuLangKey.MENU_CTF_CARRYFLAGASITEM_NAME,
            MgMenuLangKey.MENU_CTF_CARRYFLAGASITEM_DESCRIPTION));
        menu.displayMenu(previous.getViewer());
        return true;
    }
}

