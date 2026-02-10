package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.HSVLike;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * pretty advanced menu: Lets you pick a color via hsv color picker
 */
@SuppressWarnings("UnstableApiUsage") // shutup ItemTyp
public class MenuColorPicker extends Menu { // todo where can we fit another item to allow manuel input?
    //7 or greater
    protected static final int HUE_AMOUNT = 25;
    protected final @NotNull MenuItemColorHolder[] hueItems = new MenuItemColorHolder[HUE_AMOUNT];
    // called whenever the user chooses a color
    protected final @NotNull Callback<TextColor> result;
    // you can scroll through the hue items and this is the index we are in right now
    protected int scrollIndex = 0;

    public MenuColorPicker(final @NotNull Component title,
                           final @NotNull Callback<TextColor> result,
                           final @NotNull MinigamePlayer intendedViewer) {
        super(6, title, intendedViewer);

        this.result = result;

        //init hue values
        for (int i = 0; i < HUE_AMOUNT; i++) {
            TextColor textColor = TextColor.color(HSVLike.hsvLike(i * (1.0f / (HUE_AMOUNT - 1)), 1.0f, 1.0f));
            hueItems[i] = (new MenuItemColorHolder(ItemType.LEATHER_CHESTPLATE, textColor, this::updateDisplay));
        }

        //set color selector up (first 5 rows)
        for (int i = 0; i < 5 * 9; i++) {
            setItem(new MenuItemColorHolder(ItemType.LEATHER_CHESTPLATE, NamedTextColor.BLACK, resultingColor -> {
                result.setValue(resultingColor);

                intendedViewer.getPlayer().closeInventory();
                final @Nullable Menu previousPage = getPreviousPage();
                if (previousPage != null) {
                    previousPage.displayMenu();
                }
            }), i);
        }

        //set up hue scroller (last row)
        final @NotNull MenuItemCustom hueUpItem = new MenuItemCustom(MenuDisplayTypes.pageBackType(), Component.text("<-"));
        hueUpItem.setClick(() -> {
            scrollIndex = Math.floorMod(scrollIndex - 3, HUE_AMOUNT);
            setHueBar(scrollIndex);
            return hueUpItem.getDisplayItem();
        });
        final @NotNull MenuItemCustom hueDownItem = new MenuItemCustom(MenuDisplayTypes.pageNextType(), Component.text("->"));
        hueDownItem.setClick(() -> {
            scrollIndex = Math.floorMod(scrollIndex + 3, HUE_AMOUNT);
            setHueBar(scrollIndex);

            return hueDownItem.getDisplayItem();
        });

        setItem(hueUpItem, 5 * 9);
        setItem(hueDownItem, 6 * 9 - 1);
        setHueBar(0);

        updateDisplay(hueItems[0].getColor());
    }

    protected void setHueBar(final int startingFrom) {
        for (int i = 0; i < 7; i++) {
            setItem(hueItems[Math.floorMod(i + startingFrom, HUE_AMOUNT)], 5 * 9 + i);
        }
    }

    protected void updateDisplay(final @NotNull TextColor color) {
        final float hue = HSVLike.fromRGB(color.red(), color.blue(), color.green()).h();

        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 9; x++) {
                final float saturation = 1.0f - (y * (1.0f / 4));
                final float brightness = 1.0f - (x * (1.0f / 8));
                final @NotNull HSVLike targetColor = HSVLike.hsvLike(hue, saturation, brightness);

                ((MenuItemColorHolder) getMenuItem(y * 9 + x)).setColor(TextColor.color(targetColor));
            }
        }
    }
}