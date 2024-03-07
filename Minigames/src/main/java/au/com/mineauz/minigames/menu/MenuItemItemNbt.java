package au.com.mineauz.minigames.menu;

import net.kyori.adventure.text.Component;
import org.apache.commons.text.WordUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/*todo when I did this I didn't really thought about item amount.
 * While clicking with a stack on this should work, it gets tricky if the desired number exceeds the stack limit.
 * sure, one could easily work with left / right click but there is no guarantee overstacking works and clicking 300 times isn't fun.
 * for now I will add a Integer menu item besides this one. Maybe someone will have a bright idea in future
 * Amounts are not supported here.
 */
public class MenuItemItemNbt extends MenuItem {
    private final static String DESCRIPTION_TOKEN = "Nbt_description";

    /**
     * NEVER EVER confuse this item with the display item of MenuItem!
     * this one contains the pure data as it should get written to config if needed.
     * The display item has a  different displayname and a description attached to it,
     * to explain a user what the MenuItem does.
     * Accidentally setting the same instance to both of them may tint your data or break your menu item.
     * Always use {@link ItemStack#clone} and best practice use the {@link MenuItem#setDisplayItem(ItemStack)}
     * to update the display item!
     * take care.
     **/
    private final @NotNull Callback<ItemStack> itemCallback;

    public MenuItemItemNbt(@Nullable Material displayMat, @Nullable Component name, @NotNull Callback<ItemStack> c) {
        super(displayMat, name);
        itemCallback = c;
    }

    public MenuItemItemNbt(@Nullable Material displayMat, @Nullable Component name, List<Component> description, @NotNull Callback<ItemStack> c) {
        super(displayMat, name, description);
        itemCallback = c;
    }

    public MenuItemItemNbt(@NotNull ItemStack itemStack, Component name, @NotNull Callback<ItemStack> c) {
        super(itemStack.clone(), name); // clone to not overwrite lore / name

        setDescriptionPart(DESCRIPTION_TOKEN, createDescription(itemStack));
        itemCallback = c;
    }

    @Override
    public ItemStack onClickWithItem(ItemStack item) {
        // better make a copy, we don't know what happens with the item later
        itemCallback.setValue(item.clone());

        // Taken from itemCallback, since it could be able to change it,
        // and it may be different from our initial cloned item
        ItemStack workItemStack = itemCallback.getValue();

        // cloned one as display item since it gets changed to display name / description / other meta
        setDisplayItem(workItemStack.clone());
        // please note, the item used to create the components is the original - not cloned one!
        setDescriptionPart(DESCRIPTION_TOKEN, createDescription(workItemStack));

        return super.onClickWithItem(itemCallback.getValue());
    }

    @Override
    public ItemStack onShiftRightClick() {
        // note: this was done so display item and value do NOT share the same reference.
        // the display item gets changed by MenuItem!
        setDisplayItem(new ItemStack(Material.STONE));
        itemCallback.setValue(new ItemStack(Material.STONE));
        return super.onShiftRightClick();
    }

    public ItemStack getItem() {
        return itemCallback.getValue();
    }

    public void processNewName(@NotNull Component newName) {
        ItemStack oldData = itemCallback.getValue();
        setDescriptionPart(DESCRIPTION_TOKEN, createDescription(oldData.getType(), newName, oldData.lore()));
    }

    public void processNewLore(@Nullable List<@NotNull Component> newLore) {
        ItemStack oldData = itemCallback.getValue();
        setDescriptionPart(DESCRIPTION_TOKEN, createDescription(oldData.getType(), oldData.displayName(), newLore));
    }

    private List<Component> createDescription(@NotNull ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        return createDescription(itemStack.getType(), meta.displayName(), meta.lore());
    }

    private List<Component> createDescription(@NotNull Material type, @Nullable Component displayName, @Nullable List<@NotNull Component> lore) {
        List<Component> result = new ArrayList<>();
        if (displayName != null) {
            result.add(Component.text("Name: ").append(displayName));
        }
        result.add(Component.text("Material: " + WordUtils.capitalizeFully(type.name())));

        if (lore != null) {
            result.add(Component.text("lore: "));
            result.addAll(lore);
        }
        return result;
    }
}
