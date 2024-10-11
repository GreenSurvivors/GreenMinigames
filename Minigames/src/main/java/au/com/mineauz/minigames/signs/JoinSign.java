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
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

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
    public String getCreatePermission() {
        return "minigame.sign.create.join";
    }

    @Override
    public String getUsePermission() {
        return "minigame.sign.use.join";
    }

    @Override
    public boolean signCreate(@NotNull SignChangeEvent event) {
        final Sign sign = (Sign) event.getBlock().getState();
        final Minigame minigame = getMinigame(sign, event.line(2));

        if (minigame != null) {
            event.line(1, getName());

            event.line(2, minigame.getDisplayName());
            setPersistentMinigame(sign, minigame);

            if (Minigames.getPlugin().hasEconomy()) { // todo
                if (!event.getLine(3).isEmpty() && !event.getLine(3).matches("\\$?[0-9]+(.[0-9]{2})?")) {
                    MinigameMessageManager.sendMgMessage(event.getPlayer(), MinigameMessageType.ERROR, MgMiscLangKey.SIGN_JOIN_ERROR_INVALIDMONEY);
                    return false;
                } else if (event.getLine(3).matches("[0-9]+(?:.[0-9]{2})?")) {
                    event.setLine(3, "$" + event.getLine(3));
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
        if (plugin.getConfig().getBoolean("requireEmptyInventory")) {
            fullInv = true;
            for (ItemStack item : mgPlayer.getPlayer().getInventory().getContents()) {
                if (item != null) {
                    MinigameMessageManager.debugMessage("Found: " + item);
                    invOk = false;
                    break;
                }
            }

            for (ItemStack item : mgPlayer.getPlayer().getInventory().getArmorContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    MinigameMessageManager.debugMessage("Found armor: " + item);
                    invOk = false;
                    break;
                }
            }
        } else {
            fullInv = false;
            invOk = mgPlayer.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR;
        }
        if (invOk) {
            Minigame mgm = getMinigame(sign);
            if (mgm != null && (!mgm.getUsePermissions() ||
                    mgPlayer.getPlayer().hasPermission("minigame.join." + mgm.getName().toLowerCase()))) {
                if (mgm.isEnabled()) {
                    if (!sign.getLine(3).isEmpty() && Minigames.getPlugin().hasEconomy()) {
                        double amount = Double.parseDouble(sign.getLine(3).replace("$", ""));
                        if (Minigames.getPlugin().getEconomy().getBalance(mgPlayer.getPlayer().getPlayer()) >= amount) {
                            Minigames.getPlugin().getEconomy().withdrawPlayer(mgPlayer.getPlayer().getPlayer(), amount);
                        } else {
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
        } else if (!MinigameTool.isMinigameTool(mgPlayer.getPlayer().getInventory().getItemInMainHand())) {
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
