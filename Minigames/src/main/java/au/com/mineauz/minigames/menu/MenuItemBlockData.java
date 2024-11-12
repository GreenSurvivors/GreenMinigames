package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.consumer.BlockDataConsumer;
import au.com.mineauz.minigames.menu.consumer.StringConsumer;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class MenuItemBlockData extends MenuItem implements BlockDataConsumer, StringConsumer {
    private static final @NotNull String DESCRIPTION_TOKEN = "BlockData_description";
    private final @NotNull Callback<BlockData> dataCallback;

    public MenuItemBlockData(@NotNull Material displayMat, @Nullable Component name,
                             @NotNull Callback<BlockData> callback) {
        super(displayMat, name);
        this.dataCallback = callback;
        setDescriptionPart(DESCRIPTION_TOKEN, createDescription(dataCallback.getValue()));
    }

    @Override
    public void update() {
        setDescriptionPart(DESCRIPTION_TOKEN, createDescription(this.dataCallback.getValue()));
    }

    /**
     * minecraft:chest[facing=north,type=single,waterlogged=false]{Items:[{Slot:0b,id:"minecraft:grass_block",Count:1b}],Lock:""}
     */
    private @NotNull List<@NotNull Component> createDescription(@NotNull BlockData data) {
        List<Component> result = new ArrayList<>();
        result.add(MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_MATERIAL_DESCRIOPTION,
                Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), Component.translatable(data.getMaterial().translationKey()))));
        String dataString = data.getAsString();

        int firstBracket = dataString.indexOf('[');
        int secondBracket = dataString.indexOf(']');

        if (secondBracket > 0) {
            String meta = dataString.substring(firstBracket + 1, secondBracket);
            // using StringUtils should be faster than String#slit, since it's optimized for one char scenarios;
            // May break down, if Mojang ever returns UTF16 Strings
            for (String val : StringUtils.split(meta, ",")) {
                String[] pair = StringUtils.split(meta, "=", 2);
                if (pair.length == 2) {
                    result.add(Component.text(pair[0], NamedTextColor.GOLD).
                            append(Component.text(" : ")).
                            append(Component.text(pair[1], NamedTextColor.DARK_GREEN)));
                } else {
                    result.add(Component.text(val, NamedTextColor.GOLD));
                }
            }

            int extraStart = dataString.indexOf('{', secondBracket);
            if (extraStart > 0) {
                result.add(MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_BLOCKDATA_DESCRIOPTION_EXTRA,
                        Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), dataString.substring(extraStart))));
            }
        }
        return result;
    }

    @Override
    public @NotNull ItemStack onClickWithItem(@Nullable ItemStack item) {
        if (item != null && item.getType().isBlock()) {
            this.dataCallback.setValue(item.getType().createBlockData());

            // update the display item
            ItemStack stackUpdate = getDisplayItem();
            setDisplayItem(stackUpdate.withType(item.getType()));
        } else {
            MinigameMessageManager.sendMgMessage(getContainer().getViewer(), MinigameMessageType.ERROR, MgMenuLangKey.MENU_BLOCKDATA_ERROR_INVALID,
                    Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), item != null ? Component.translatable(item.getType().translationKey()) : Component.text("?")));
        }
        return getDisplayItem();
    }

    @Override
    public @Nullable ItemStack onDoubleClick() {
        MinigamePlayer mgPlayer = getContainer().getViewer();
        mgPlayer.setNoClose(true);
        mgPlayer.getPlayer().closeInventory();
        final int reopenSeconds = 10;
        MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_BLOCKDATA_CLICKBLOCK,
                Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), getName()),
                Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(Duration.ofSeconds(reopenSeconds))));
        mgPlayer.setManualEntry(this);
        getContainer().startReopenTimer(reopenSeconds);
        return null;
    }

    @Override
    public void acceptString(@NotNull String string) {
        try {
            acceptBlockData(Bukkit.createBlockData(string));
        } catch (IllegalArgumentException e) {
            MinigameMessageManager.sendMessage(getContainer().getViewer(), MinigameMessageType.ERROR, Component.text(e.getLocalizedMessage()));
        }
    }

    @Override
    public void acceptBlockData(@NotNull BlockData blockData) {
        dataCallback.setValue(blockData);
        setDescriptionPart(DESCRIPTION_TOKEN, createDescription(dataCallback.getValue()));

        // update the display item
        if (blockData.getMaterial().isItem()) {
            ItemStack stackUpdate = getDisplayItem();
            setDisplayItem(stackUpdate.withType(blockData.getMaterial()));
        }

        getContainer().cancelReopenTimer();
        getContainer().displayMenu(getContainer().getViewer());
    }
}
