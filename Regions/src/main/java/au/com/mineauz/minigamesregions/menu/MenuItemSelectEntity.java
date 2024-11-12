package au.com.mineauz.minigamesregions.menu;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigames.menu.consumer.EntityConsumer;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Main;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

public class MenuItemSelectEntity extends MenuItem implements EntityConsumer {
    private final static String DESCRIPTION_TOKEN = "Entity_description";
    private final @NotNull Callback<EntitySnapshot> entitySnapshotCallback;

    public MenuItemSelectEntity(@Nullable Material displayMat, @Nullable Component name, @NotNull Callback<EntitySnapshot> c) {
        super(displayMat, name);
        entitySnapshotCallback = c;
    }

    public MenuItemSelectEntity(@Nullable Material displayMat, @Nullable Component name,
                                @Nullable List<@NotNull Component> description, @NotNull Callback<EntitySnapshot> c) {
        super(displayMat, name, description);
        entitySnapshotCallback = c;
    }

    @Override
    public ItemStack onClickWithItem(@NotNull ItemStack item) {
        if (item.getItemMeta() instanceof SpawnEggMeta spawnEggMeta) {
            final @Nullable EntitySnapshot snapshot = spawnEggMeta.getSpawnedEntity();

            if (snapshot != null) {
                if (snapshot.getEntityType().isSpawnable()) {
                    entitySnapshotCallback.setValue(snapshot);
                    update();
                } else {
                    Main.getPlugin().getComponentLogger().warn("MenuItemSelectEntity was clicked with an spawnegg containing a non-spawnable entity: {}", snapshot.getEntityType().getKey());
                }
            } else {
                final @Nullable EntityType entityType = Registry.ENTITY_TYPE.get(item.getType().getKey());

                if (entityType != null && entityType.isSpawnable()) {
                    entitySnapshotCallback.setValue(Bukkit.getWorlds().getFirst().createEntity(new Location(Bukkit.getWorlds().getFirst(), 0, 0, 0), entityType.getEntityClass()).createSnapshot());
                    update();
                } else {
                    Main.getPlugin().getComponentLogger().warn("MenuItemSelectEntity was clicked with an spawnegg, but I couldn't find any spawnable entity: {}, coming from {}", entityType == null ? null : entityType.getKey().asString(), item.getType().getKey().asString());
                }
            }
        }

        return super.onClickWithItem(item);
    }

    @Override
    public @Nullable ItemStack onDoubleClick() {
        MinigamePlayer mgPlayer = getContainer().getViewer();
        mgPlayer.setNoClose(true);
        mgPlayer.getPlayer().closeInventory();
        final int reopenSeconds = 10;

        MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO,
            RegionMessageManager.getMessage(RegionLangKey.MENU_SELECT_ENTITY_CLICK_ENTITY,
                Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), getName()),
                Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(Duration.ofSeconds(reopenSeconds)))));
        mgPlayer.setManualEntry(this);
        getContainer().startReopenTimer(reopenSeconds);


        return super.onDoubleClick();
    }

    @Override
    public void acceptEntity(@NotNull Entity entity) {
        entitySnapshotCallback.setValue(entity.createSnapshot());
        update();

        getContainer().cancelReopenTimer();
        getContainer().displayMenu(getContainer().getViewer());
    }

    public void update() {
        Material displayType;

        // fist try spawn egg
        displayType = Registry.MATERIAL.get(NamespacedKey.minecraft(entitySnapshotCallback.getValue().getEntityType().getKey().getKey() + "_spawn_egg"));

        if (displayType == null) {
            // did not work. Maybe there is an item with the same key, like in case of boats or arrows?
            displayType = Registry.MATERIAL.get(entitySnapshotCallback.getValue().getEntityType().getKey());

            if (displayType == null) {
                displayType = Material.SPAWNER;
            }
        }

        ItemStack newDisplayItem = getDisplayItem().withType(displayType);

        if (newDisplayItem instanceof SpawnEggMeta spawnEggMeta) {
            spawnEggMeta.setSpawnedEntity(entitySnapshotCallback.getValue());

            newDisplayItem.setItemMeta(spawnEggMeta);
        }

        setDisplayItem(newDisplayItem);

        setDescriptionPart(DESCRIPTION_TOKEN, List.of(
            RegionMessageManager.getMessage(RegionLangKey.MENU_ENTITY_TYPE_NAME,
                Placeholder.component(MinigamePlaceHolderKey.ENTITY.getKey(),
                    Component.translatable(entitySnapshotCallback.getValue().getEntityType().translationKey())))));
    }
}
