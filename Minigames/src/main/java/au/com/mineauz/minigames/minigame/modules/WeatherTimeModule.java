package au.com.mineauz.minigames.minigame.modules;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.EnumFlag;
import au.com.mineauz.minigames.config.TimeFlag;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuDisplayTypes;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.menu.MenuItemPage;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.WeatherType;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class WeatherTimeModule extends AMinigameModule {
    private final @NotNull TimeFlag time = new TimeFlag("customTime.value", 0L);
    private final @NotNull BooleanFlag useCustomTime = new BooleanFlag("customTime.enabled", false);
    private final @NotNull BooleanFlag useCustomWeather = new BooleanFlag("customWeather.enabled", false);
    private final @NotNull EnumFlag<WeatherType> weather = new EnumFlag<>("customWeather.type", WeatherType.CLEAR);
    private int task = -1;

    public WeatherTimeModule(final @NotNull Minigame mgm, final @NotNull Key key) {
        super(mgm, key);
    }

    public static WeatherTimeModule getMinigameModule(final @NotNull Minigame minigame) {
        return (WeatherTimeModule) minigame.getModule(MgDefaultModules.WEATHER_TIME.getKey());
    }

    @Override
    public boolean useSeparateConfig() {
        return false;
    }

    @Override
    public void save(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        time.saveValue(config);
        useCustomTime.saveValue(config);
        weather.saveValue(config);
        useCustomWeather.saveValue(config);
    }

    @Override
    public void load(final @NotNull CommentedConfigurationNode config) {
        time.loadValue(config);
        useCustomTime.loadValue(config);
        weather.loadValue(config);
        useCustomWeather.loadValue(config);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void addEditMenuOptions(final @NotNull Menu previosMenu) {
        final @NotNull Menu menu = new Menu(6, MgMenuLangKey.MENU_TIMEWEATHER_NAME, previosMenu.getIntendedViewer());

        menu.addItem(useCustomTime.getMenuItem(ItemType.CLOCK, MgMenuLangKey.MENU_TIMEWEATHER_TIME_USE_NAME));
        menu.addItem(time.getMenuItem(MenuDisplayTypes.timeType(), MgMenuLangKey.MENU_TIMEWEATHER_TIME_NAME, 0L, 24000L));
        menu.addItem(useCustomWeather.getMenuItem(ItemType.WIND_CHARGE, MgMenuLangKey.MENU_TIMEWEATHER_WEATHER_USE_NAME));
        menu.addItem(weather.getMenuItem(ItemType.WATER_BUCKET, MgMenuLangKey.MENU_TIMEWEATHER_WEATHER_NAME));
        menu.setItem(new MenuItemBack(previosMenu), menu.getSize() - 9);

        previosMenu.addItem(new MenuItemPage(MenuDisplayTypes.genericSubMenu(), MgMenuLangKey.MENU_TIMEWEATHER_NAME, menu));
    }

    public long getTime() {
        return time.getFlag();
    }

    public void setTime(final long time) {
        this.time.setFlag(time);
    }

    public boolean isUsingCustomTime() {
        return useCustomTime.getFlag();
    }

    public void setUseCustomTime(final boolean bool) {
        useCustomTime.setFlag(bool);
    }

    public void applyCustomTime(final @NotNull MinigamePlayer player) {
        if (isUsingCustomTime()) {
            player.getPlayer().setPlayerTime(time.getFlag(), false);
        }
    }

    public boolean isUsingCustomWeather() {
        return useCustomWeather.getFlag();
    }

    public void setUsingCustomWeather(final boolean bool) {
        useCustomWeather.setFlag(bool);
    }

    public WeatherType getCustomWeather() {
        return weather.getFlag();
    }

    public void setCustomWeather(final WeatherType type) {
        weather.setFlag(type);
    }

    public void applyCustomWeather(final @NotNull MinigamePlayer player) {
        if (isUsingCustomWeather()) {
            player.getPlayer().setPlayerWeather(weather.getFlag());
        }
    }

    public void startTimeLoop() {
        final @NotNull Minigame minigame = getMinigame();
        if (task == -1 && isUsingCustomTime()) {
            task = Bukkit.getScheduler().scheduleSyncRepeatingTask(Minigames.getPlugin(), () -> {
                for (final @NotNull MinigamePlayer player : minigame.getPlayers()) {
                    player.getPlayer().setPlayerTime(time.getFlag(), false);
                }
            }, 20 * 5, 20 * 5);
        }
    }

    public void stopTimeLoop() {
        if (task == -1) return;
        Bukkit.getScheduler().cancelTask(task);
        task = -1;
    }
}
