package au.com.mineauz.minigames.signs;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgSignLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.tool.MinigameTool;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.UUID;

public class JoinSign extends AMinigameSign {
    private static final HashSet<UUID> shownWarning = new HashSet<>();
    private static final Minigames plugin = Minigames.getPlugin();

    @Override
    public @NotNull Component getName() {
        return MinigameMessageManager.getMgMessage(MgSignLangKey.TYPE_JOIN);
    }

    @Override
    public @Nullable String getCreatePermission() {
        return "minigame.sign.create.join";
    }

    @Override
    public @Nullable String getUsePermission() {
        return "minigame.sign.use.join";
    }

    public boolean signCreate(@NotNull SignChangeEvent event) {
        final Sign sign = (Sign) event.getBlock().getState();
        final Minigame minigame = getMinigame(sign, event.line(2));

        if (minigame != null) {
            event.line(1, getName());

            event.line(2, minigame.getDisplayName());
            setPersistentMinigame(sign, minigame);

            if (Minigames.getPlugin().hasEconomy()) {
                if (event.line(3) != null) {
                    final @NotNull String line3 = PlainTextComponentSerializer.plainText().serialize(event.line(3));

                    if (!line3.isEmpty()) {
                        final Double amount = getMoneyBetFromLine(event.line(3), true);

                        if (amount == null) {
                            MinigameMessageManager.sendMgMessage(event.getPlayer(), MinigameMessageType.ERROR, MgMiscLangKey.SIGN_JOIN_ERROR_INVALIDMONEY);
                            return false;
                        } else {
                            setMoneyBet(event, amount);
                        }
                    }
                }
            } else if (plugin.getConfig().getBoolean("warnings")) {
                event.line(3, Component.empty());
                if (!shownWarning.contains(event.getPlayer().getUniqueId())) {
                    MinigameMessageManager.sendMgMessage(event.getPlayer(), MinigameMessageType.WARNING, MgMiscLangKey.MINIGAME_WARNING_NOVAULT);

                    shownWarning.add(event.getPlayer().getUniqueId());
                }
            }
            return true;
        }
        MinigameMessageManager.sendMgMessage(event.getPlayer(), MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOMINIGAME,
            Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), event.line(2)));
        return false;
    }

    @Override
    public boolean signUse(@NotNull Sign sign, @NotNull MinigamePlayer mgPlayer) {
        if (mgPlayer.isInMinigame()) {
            return false;
        }

        boolean invOk = true;
        boolean fullInv;
        final Player player = mgPlayer.getPlayer();
        final PlayerInventory inventory = player.getInventory();
        if (plugin.getConfig().getBoolean("requireEmptyInventory")) {
            fullInv = true;
            for (ItemStack item : inventory.getContents()) {
                if (item != null) {
                    MinigameMessageManager.debugMessage("Found: " + item);
                    invOk = false;
                    break;
                }
            }

            for (ItemStack item : inventory.getArmorContents()) {
                if (item != null && !item.isEmpty()) {
                    MinigameMessageManager.debugMessage("Found armor: " + item);
                    invOk = false;
                    break;
                }
            }
        } else {
            fullInv = false;
            invOk = inventory.getItemInMainHand().isEmpty();
        }
        if (invOk) {
            final @NotNull Minigame mgm = getMinigame(sign);
            if (mgm != null && (!mgm.getUsePermissions() ||
                player.hasPermission("minigame.join." + mgm.getName().toLowerCase()))) {
                if (mgm.isEnabled()) {
                    final @Nullable Double moneyBet = getMoneyBet(sign);

                    if (moneyBet != null && Minigames.getPlugin().hasEconomy()) {
                        if (!Minigames.getPlugin().getEconomy().withdrawPlayer(player, moneyBet).transactionSuccess()) {
                            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_JOIN_ERROR_NOTENOUGH_MONEY);
                            return false;
                        }
                    }
                    plugin.getPlayerManager().joinMinigame(mgm, mgPlayer, false, 0.0);
                    return true;
                } else if (!mgm.isEnabled()) {
                    MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOTENABLED);
                }
            } else if (mgm == null) {
                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOMINIGAME,
                    Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), sign.getSide(Side.FRONT).line(2)));
            } else if (mgm.getUsePermissions()) {
                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOPERMISSION);
            }
        } else if (!MinigameTool.isMinigameTool(inventory.getItemInMainHand())) {
            if (fullInv) {
                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.SIGN_ERROR_FULLINV);
            } else {
                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.SIGN_ERROR_EMPTYHAND);
            }
        }

        return false;
    }

    @Override
    public void signBreak(@NotNull Sign sign, @NotNull MinigamePlayer mgPlayer) {
    }
}
