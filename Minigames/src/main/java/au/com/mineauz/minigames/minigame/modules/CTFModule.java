package au.com.mineauz.minigames.minigame.modules;

import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.Flag;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemPage;
import au.com.mineauz.minigames.menu.MenuUtility;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.CTFFlag;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CTFModule extends MinigameModule {
    // config
    private final BooleanFlag useFlagAsCapturePoint = new BooleanFlag(true, "useFlagAsCapturePoint");
    private final BooleanFlag bringFlagBackManual = new BooleanFlag(false, "bringFlagBackManual");
    private final BooleanFlag carryFlagAsItem = new BooleanFlag(false, "carryFlagAsItem");
    // runtime
    private final Map<MinigamePlayer, CTFFlag> flagCarriers = new HashMap<>();
    private final Map<String, CTFFlag> droppedFlag = new HashMap<>();

    public CTFModule(final @NotNull Minigame mgm) {
        super(mgm);
    }

    public static CTFModule getMinigameModule(final @NotNull Minigame mgm) {
        return (CTFModule) mgm.getModule("CTF");
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

    @Override
    public String getName() {
        return "CTF";
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

    public void removeFlagCarrier(final @NotNull MinigamePlayer ply) {
        flagCarriers.remove(ply);
    }

    public CTFFlag getCarriedFlag(final @NotNull MinigamePlayer ply) {
        return flagCarriers.get(ply);
    }

    public void resetFlags() {
        for (MinigamePlayer ply : flagCarriers.keySet()) {
            getCarriedFlag(ply).respawnFlag();
            getCarriedFlag(ply).stopCarrierParticleEffect();
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
    public Map<String, Flag<?>> getConfigFlags() {
        Map<String, Flag<?>> flags = new HashMap<>();
        flags.put(useFlagAsCapturePoint.getName(), useFlagAsCapturePoint);
        flags.put(bringFlagBackManual.getName(), bringFlagBackManual);
        flags.put(carryFlagAsItem.getName(), carryFlagAsItem);
        return flags;
    }

    @Override
    public boolean useSeparateConfig() {
        return false;
    }

    @Override
    public void save(FileConfiguration config) {

    }

    @Override
    public void load(FileConfiguration config) {

    }

    @Override
    public void addEditMenuOptions(Menu menu) {

    }

    @Override
    public boolean displayMechanicSettings(final @NotNull Menu previous) {
        final @NotNull Menu menu = new Menu(6, "CTF Settings", previous.getViewer());
        menu.addItem(new MenuItemPage("Back", MenuUtility.getBackMaterial(), previous), menu.getSize() - 9);

        menu.addItem(useFlagAsCapturePoint.getMenuItem("CTF Flag is Capture Point", Material.BLACK_BANNER,
                List.of("Use a teams Flag as a capture point")));
        menu.addItem(bringFlagBackManual.getMenuItem("Bring Flag Back Manually", Material.ENDER_EYE,
                List.of("If enabled, the flag can be brought", "back to the base manually")));
        menu.addItem(carryFlagAsItem.getMenuItem("Carry CTF Flag as Item", Material.OAK_SIGN,
            List.of("Every player carrying a CTF Flag", " will get an item to place down", "(Please make sure the player is allowed to place blocks!)")));
        menu.displayMenu(previous.getViewer());
        return true;
    }
}

