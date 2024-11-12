package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.recorder.EntityData;
import au.com.mineauz.minigamesregions.Main;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.config.EntitySnapshotFlag;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.entity.CraftEntitySnapshot;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SpawnEntityAction extends AAction {
    private static final @NotNull NamespacedKey MINIGAME_ENTITY_KEY = new NamespacedKey(Main.getPlugin(), "minigame");
    private final @NotNull EntitySnapshotFlag entitySnapshotFlag = new EntitySnapshotFlag(getDefaultSnapshot(), "entity");

    private static @NotNull EntitySnapshot getDefaultSnapshot() {
        return Bukkit.getWorlds().getFirst().createEntity(new Location(Bukkit.getWorlds().getFirst(), 0, 0, 0), Zombie.class).createSnapshot();
    }

    protected SpawnEntityAction(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull String getName() {
        return "SPAWN_ENTITY";
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SPAWNENTITY_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.WORLD;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        Map<Component, Component> out = new LinkedHashMap<>(2);
        out.put(RegionMessageManager.getMessage(RegionLangKey.MENU_ENTITY_TYPE_NAME), Component.translatable(entitySnapshotFlag.getFlagOrDefault().getEntityType().translationKey()));

        if (entitySnapshotFlag.getFlagOrDefault().getEntityType().isAlive()) {
            String customName = ((CraftEntitySnapshot) entitySnapshotFlag.getFlagOrDefault()).getData().getString("CustomName");

            if (!customName.isBlank()) {
                out.put(RegionMessageManager.getMessage(RegionLangKey.MENU_ENTITY_CUSTOMNAME_NAME),
                    PaperAdventure.asAdventure(net.minecraft.network.chat.Component.Serializer.fromJson(customName, MinecraftServer.getServer().registryAccess())));
            }
        }

        return out;
    }

    @Override
    public boolean useInRegions() {
        return false;
    }

    @Override
    public boolean useInNodes() {
        return true;
    }

    @Override
    public void executeRegionAction(@Nullable MinigamePlayer mgPlayer,
                                    @NotNull Region region) {
        debug(mgPlayer, region);
    }

    @Override
    public void executeNodeAction(@NotNull MinigamePlayer mgPlayer, @NotNull Node node) {
        if (!mgPlayer.isInMinigame()) {
            return;
        }
        debug(mgPlayer, node);

        Entity entity = entitySnapshotFlag.getFlagOrDefault().createEntity(node.getLocation());
        entity.getPersistentDataContainer().set(MINIGAME_ENTITY_KEY, PersistentDataType.STRING, node.getMinigame().getName()); //todo use in recorder to despawn + add parameter for specific Minigame

        mgPlayer.getMinigame().getRecorderData().addEntity(entity, mgPlayer, EntityData.ChangeType.CREATED);
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        entitySnapshotFlag.saveValue(config, path);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        entitySnapshotFlag.loadValue(config, path);
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer mgPlayer, @NotNull Menu previous) {
        Menu menu = new Menu(3, getDisplayname(), mgPlayer);
        menu.addItem(new MenuItemBack(previous), menu.getSize() - 9);

        final MenuItem entitySelector = entitySnapshotFlag.getMenuItem(Material.SPAWNER, RegionMessageManager.getMessage(RegionLangKey.MENU_ENTITY_SELECT_NAME));
        entitySelector.update();

        final EntityType[] entityTypes = EntityType.values();
        List<EntityType> options = new ArrayList<>(entityTypes.length);
        for (EntityType type : entityTypes) {
            if (type.isSpawnable()) {
                options.add(type);
            }
        }
        menu.addItem(new MenuItemList<>(Material.SKELETON_SKULL, RegionMessageManager.getMessage(RegionLangKey.MENU_ENTITY_TYPE_NAME), new Callback<>() {
            @Override
            public EntityType getValue() {
                return entitySnapshotFlag.getFlagOrDefault().getEntityType();
            }

            @Override
            public void setValue(EntityType value) {
                final @NotNull CompoundTag nbt = ((CraftEntitySnapshot) entitySnapshotFlag.getFlagOrDefault()).getData();

                net.minecraft.world.entity.EntityType<?> entitytypes = CraftEntityType.bukkitToMinecraft(value);
                ResourceLocation minecraftkey = net.minecraft.world.entity.EntityType.getKey(entitytypes);
                String StringID = entitytypes.canSerialize() && minecraftkey != null ? minecraftkey.toString() : null;

                if (StringID != null) {
                    nbt.putString("id", StringID);

                    entitySnapshotFlag.setFlag(CraftEntitySnapshot.create(nbt));
                    entitySelector.update();
                }
            }
        }, options));

        menu.displayMenu(mgPlayer);
        return true;
    }

    public static @NotNull NamespacedKey getMINIGAME_ENTITY_KEY() {
        return MINIGAME_ENTITY_KEY;
    }
}
