package au.com.mineauz.minigames.signs;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.managers.MinigameManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgSignLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.minigame.reward.ARewardType;
import au.com.mineauz.minigames.minigame.reward.RewardGroup;
import au.com.mineauz.minigames.minigame.reward.Rewards;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.tool.MinigameTool;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RewardSign extends AMinigameSign {
    private static final @NotNull Minigames plugin = Minigames.getPlugin();

    @Override
    public @NotNull Component getName() {
        return MinigameMessageManager.getMgMessage(MgSignLangKey.TYPE_REWARD);
    }

    @Override
    public @Nullable String getCreatePermission() {
        return "minigame.sign.create.reward";
    }

    @Override
    public @Nullable String getUsePermission() {
        return "minigame.sign.use.reward";
    }

    @Override
    public boolean signCreate(final @NotNull SignChangeEvent event) {
        if (event.line(2) != null && !PlainTextComponentSerializer.plainText().serialize(event.line(2)).isEmpty()) {
            event.line(1, getName());
            return true;
        }
        MinigameMessageManager.sendMgMessage(event.getPlayer(), MinigameMessageType.ERROR, MgMiscLangKey.SIGN_REWARD_ERROR_NONAME);
        return false;
    }

    @Override
    public boolean signUse(final @NotNull Sign sign, final @NotNull MinigamePlayer mgPlayer) {
        final @NotNull Location loc = sign.getLocation();
        final Player player = mgPlayer.getPlayer();
        final MinigameManager mdata = plugin.getMinigameManager();
        if (!MinigameTool.isMinigameTool(player.getInventory().getItemInMainHand())) {
            final @NotNull String label = LegacyComponentSerializer.legacySection().serialize(sign.getSide(Side.FRONT).line(2)).toLowerCase(); // note: legacy serialize to stay backwards compatible with already paid rewards
            if (mgPlayer.isInMinigame()) {
                if (!mgPlayer.hasTempClaimedReward(label)) {
                    if (mdata.hasRewardSign(loc)) {
                        final Rewards rew = mdata.getRewardsRewardSign(loc);
                        for (final @NotNull ARewardType rewardType : rew.getReward()) {
                            rewardType.giveReward(mgPlayer);
                        }
                    }
                    mgPlayer.addTempClaimedReward(label);
                }
            } else {
                if (!mgPlayer.hasClaimedReward(label)) {
                    if (mdata.hasRewardSign(loc)) {
                        Rewards rew = mdata.getRewardsRewardSign(loc);
                        for (ARewardType r : rew.getReward()) {
                            r.giveReward(mgPlayer);
                        }

                        mgPlayer.updateInventory();
                    }
                    mgPlayer.addClaimedReward(label);
                }
            }
        } else if (player.hasPermission("minigame.tool")) {
            final Rewards rew;
            if (!mdata.hasRewardSign(loc)) {
                mdata.addRewardSign(loc);
            }
            rew = mdata.getRewardsRewardSign(loc);

            final @NotNull Menu rewardMenu = new Menu(5, getName(), mgPlayer);

            rewardMenu.setItem(new MenuItemRewardGroupAdd(MenuDisplayTypes.createType(),
                MgMenuLangKey.MENU_REWARD_GROUP_ADD_NAME, rew), 42);
            rewardMenu.setItem(new MenuItemRewardAdd(MenuDisplayTypes.createType(), MgMenuLangKey.MENU_REWARD_ITEM_ADD_NAME, rew), 43);
            final @NotNull MenuItemCustom mic = new MenuItemCustom(MenuDisplayTypes.saveType(), MgMenuLangKey.MENU_REWARD_SAVE_ALL_NAME);
            mic.setClick(() -> {
                try {
                    mdata.saveRewardSign(MinigameUtils.createBlockLocationID(loc), true);
                } catch (final @NotNull IOException e) {
                    plugin.getComponentLogger().error("Couldn't save reward sign at " + loc, e);
                }
                MinigameMessageManager.sendMgMessage(mic.getMenu().getIntendedViewer(), MinigameMessageType.INFO, MgMiscLangKey.SIGN_REWARD_SAVED);
                mic.getMenu().getIntendedViewer().getPlayer().closeInventory();
                return ItemStack.empty();
            });
            rewardMenu.setItem(mic, 44);

            final @NotNull List<@NotNull AMenuItem> menuItems = new ArrayList<>();
            for (final @NotNull ARewardType item : rew.getRewards()) {
                menuItems.add(item.getMenuItem());
            }

            final @NotNull List<@NotNull Component> des = MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_EDIT_SHIFTLEFT);
            for (final @NotNull RewardGroup group : rew.getGroups()) {
                MenuItemRewardGroup rwg = new MenuItemRewardGroup(ItemType.BUNDLE,
                    MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_REWARD_GROUP_NAME,
                        Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), group.getName())),
                    des, group, rew);
                menuItems.add(rwg);
            }
            rewardMenu.addItems(menuItems);
            rewardMenu.displayMenu();
        }
        return true;
    }

    @Override
    public void signBreak(final @NotNull Sign sign, final @NotNull MinigamePlayer mgPlayer) {
        if (plugin.getMinigameManager().hasRewardSign(sign.getLocation())) {
            try {
                plugin.getMinigameManager().removeRewardSign(sign.getLocation());
            } catch (final @NotNull IOException e) {
                plugin.getComponentLogger().error("Couldn't remove reward sign at " + sign.getLocation(), e);
            }
        }
    }
}
