package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class MenuItemBlockData extends MenuItem {
    private Callback<BlockData> data;

    public MenuItemBlockData(Component name, Material displayItem) {
        super(name, displayItem);
        data.setValue(displayItem.createBlockData());
        setDescription(createDescription(data.getValue()));
    }

    public MenuItemBlockData(Component name, Material displayItem, Callback<BlockData> callback) {
        super(name, displayItem);
        this.data = callback;
        setDescription(createDescription(data.getValue()));
    }

    @Override
    public void update() {
        setDescription(createDescription(this.data.getValue()));
    }

    /**
     * minecraft:chest[facing=north,type=single,waterlogged=false]{Items:[{Slot:0b,id:"minecraft:grass_block",Count:1b}],Lock:""}
     */
    private List<Component> createDescription(BlockData data) {
        List<Component> result = new ArrayList<>();
        result.add("Material: " + data.getMaterial().name());
        String dataString = data.getAsString();
        int firstbracket = StringUtils.indexOf(dataString, "[", 0);
        String minecraftname = StringUtils.left(dataString, firstbracket - 1);
        result.add("Minecraft Name: " + minecraftname);
        int secondbracket = StringUtils.indexOf(dataString, "]", 0);
        String meta = StringUtils.mid(dataString, firstbracket + 1, secondbracket - 1);
        String[] vals = StringUtils.split(meta, ",");
        for (String val : vals) {
            result.add(ChatColor.GOLD + val.replace("=", ": " + ChatColor.GREEN) +
                    ChatColor.RESET);
        }
        if (secondbracket < dataString.length()) {
            result.add(ChatColor.GOLD + "Extra:" + ChatColor.GREEN + " " + StringUtils.mid(dataString,
                    secondbracket + 1, dataString.length()) + ChatColor.RESET);
        }
        return result;
    }

    @Override
    public ItemStack onClickWithItem(@Nullable ItemStack item) {
        if (item != null && item.getType().isBlock()) {
            this.data.setValue(item.getType().createBlockData());
        } else {
            MinigameMessageManager.sendMgMessage(getContainer().getViewer(), MinigameMessageType.ERROR, MgMenuLangKey.MENU_BLOCKDATA_ERROR_INVALID,
                    Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), item != null ? Component.translatable(item.getType().translationKey()) : Component.text("?")));
        }
        return getItem();
    }

    @Override
    public void checkValidEntry(String entry) {
        String err;
        try {
            BlockData d = Bukkit.createBlockData(entry);
            data.setValue(d);
            setDescription(createDescription(data.getValue()));
            getContainer().cancelReopenTimer();
            getContainer().displayMenu(getContainer().getViewer());
        } catch (IllegalArgumentException e) {
            getContainer().cancelReopenTimer();
            getContainer().displayMenu(getContainer().getViewer());
            MinigameMessageManager.sendMessage(getContainer().getViewer(), MinigameMessageType.ERROR, Component.text(e.getLocalizedMessage()));
        }
    }

    @Override
    public ItemStack onDoubleClick() {
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
}
