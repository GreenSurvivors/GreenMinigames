package au.com.mineauz.minigames;

import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MultiplayerBets {
    private final @NotNull Map<@NotNull MinigamePlayer, @NotNull ItemStack> itemBet = new HashMap<>();
    private final Map<MinigamePlayer, Double> moneyBet = new HashMap<>();
    private double greatestMoneyBet = 0;
    private @NotNull ItemStack greatestItemBet = new ItemStack(Material.AIR);

    public MultiplayerBets() {
    }

    public void addBet(@NotNull MinigamePlayer player, @NotNull ItemStack item) {
        itemBet.put(player, item);
        greatestItemBet = item.clone();
    }

    public void addBet(@NotNull MinigamePlayer player, @NotNull Double money) {
        greatestMoneyBet = money;
        moneyBet.put(player, money);
    }

    public boolean hasAlreadyBet(@NotNull MinigamePlayer player) {
        return itemBet.containsKey(player) || moneyBet.containsKey(player);
    }

    public boolean isHighestBetter(@Nullable Double money, @Nullable ItemStack item) {
        if (money != null) {
            return greatestMoneyBet == 0 || money >= greatestMoneyBet;
        } else if (item != null) {
            return itemBet.isEmpty() || betValueItem(item) >= betValueItem(getHighestItemBet());
        }

        return true;
    }

    /**
     * returns a set of all bettet items, where all amounts of similar items where added together
     */
    public @NotNull Set<@NotNull ItemStack> claimItemBets() {
        HashSet<ItemStack> resultItems = new HashSet<>();

        for (ItemStack itemToAdd : itemBet.values()) {
            boolean inResult = false;

            Iterator<ItemStack> it = resultItems.iterator();
            ItemStack alreadyIn = null;

            //count together item amounts, if nbt (except amount) matches
            while (it.hasNext() && !inResult) {
                alreadyIn = it.next();

                if (itemToAdd.isSimilar(alreadyIn)) {
                    alreadyIn.setAmount(alreadyIn.getAmount() + itemToAdd.getAmount());

                    it.remove();
                    inResult = true;
                }
            }

            // readd item or add new one
            if (inResult) {
                resultItems.add(alreadyIn);
            } else {
                resultItems.add(itemToAdd);
            }
        }

        return resultItems;
    }

    public @NotNull Double claimMoneyBets() {
        Double money = 0d;
        for (Double mon : moneyBet.values()) {
            money += mon;
        }
        return money;
    }

    public int betValueMaterial(@NotNull Material material) {
        return switch (material) {
            case DIAMOND -> 3;
            case GOLD_INGOT -> 2;
            case IRON_INGOT -> 1;
            default -> 0;
        };
    }

    public int betValueItem(@NotNull ItemStack item) {
        return betValueMaterial(item.getType()) * item.getAmount();
    }

    public @Nullable ItemStack getPlayersItemBet(MinigamePlayer player) {
        if (itemBet.containsKey(player)) {
            return itemBet.get(player);
        }
        return null;
    }

    public @Nullable Double getPlayersMoneyBet(MinigamePlayer player) {
        if (moneyBet.containsKey(player)) {
            return moneyBet.get(player);
        }
        return null;
    }

    public void removePlayersBet(MinigamePlayer player) {
        itemBet.remove(player);
        moneyBet.remove(player);
    }

    public boolean hasItemBets() {
        return !itemBet.isEmpty();
    }

    public boolean hasMoneyBets() {
        return !moneyBet.isEmpty();
    }

    public double getHighestMoneyBet() {
        return greatestMoneyBet;
    }

    public ItemStack getHighestItemBet() {
        return greatestItemBet;
    }
}
