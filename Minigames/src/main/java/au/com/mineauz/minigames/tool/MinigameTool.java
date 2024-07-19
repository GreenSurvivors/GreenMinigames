package au.com.mineauz.minigames.tool;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.TeamColor;
import au.com.mineauz.minigames.minigame.modules.TeamsModule;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MinigameTool {
    private final Map<String, String> customSetting = new HashMap<>();
    private final @NotNull ItemStack tool;
    private @Nullable Minigame minigame = null;
    private @Nullable ToolMode mode = null;
    private @Nullable TeamColor teamColor = null;

    public MinigameTool(@NotNull ItemStack tool) {
        this.tool = tool;
        ItemMeta meta = tool.getItemMeta();
        List<Component> lore = meta.lore();
        PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();

        if (lore != null && lore.size() >= 3) {
            Pattern minigamePattern = Pattern.compile(
                    MinigameMessageManager.getStrippedMgMessage(MgMiscLangKey.TOOL_SELECTED_MINIGAME_DESCRIPTION,
                            Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), "(.*)")),
                    Pattern.CASE_INSENSITIVE);
            Matcher mgMatcher = minigamePattern.matcher(plainSerializer.serialize(lore.getFirst()));

            if (mgMatcher.matches()) {
                minigame = Minigames.getPlugin().getMinigameManager().getMinigame(mgMatcher.group(1));
            }

            Pattern modePattern = Pattern.compile(
                    MinigameMessageManager.getStrippedMgMessage(MgMiscLangKey.TOOL_SELECTED_MODE_DESCRIPTION,
                            Placeholder.unparsed(MinigamePlaceHolderKey.TYPE.getKey(), "(.*)")),
                    Pattern.CASE_INSENSITIVE);
            Matcher modeMatcher = modePattern.matcher(plainSerializer.serialize(lore.get(1)));

            if (modeMatcher.matches()) {
                mode = ToolModes.getToolMode(modeMatcher.group(1).replace(" ", "_"));
            }

            Pattern teamPattern = Pattern.compile(
                    MinigameMessageManager.getStrippedMgMessage(MgMiscLangKey.TOOL_SELECTED_TEAM_DESCRIPTION,
                            Placeholder.unparsed(MinigamePlaceHolderKey.TEAM.getKey(), "(.*)")),
                    Pattern.CASE_INSENSITIVE);
            Matcher teamMatcher = teamPattern.matcher(plainSerializer.serialize(lore.get(2)));

            if (teamMatcher.matches()) {
                teamColor = TeamColor.matchColor(teamMatcher.group(1));
            }
        } else {
            meta.displayName(MinigameMessageManager.getMgMessage(MgMiscLangKey.TOOL_NAME));
            lore = new ArrayList<>();
            lore.add(MinigameMessageManager.getMgMessage(MgMiscLangKey.TOOL_SELECTED_MINIGAME_DESCRIPTION,
                    Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(),
                            MinigameMessageManager.getMgMessage(MgMiscLangKey.QUANTIFIER_NONE))));
            lore.add(MinigameMessageManager.getMgMessage(MgMiscLangKey.TOOL_SELECTED_MODE_DESCRIPTION,
                    Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(),
                            MinigameMessageManager.getMgMessage(MgMiscLangKey.QUANTIFIER_NONE))));
            lore.add(MinigameMessageManager.getMgMessage(MgMiscLangKey.TOOL_SELECTED_TEAM_DESCRIPTION,
                    Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(),
                            MinigameMessageManager.getMgMessage(MgMiscLangKey.QUANTIFIER_NONE))));
            meta.lore(lore);
            tool.setItemMeta(meta);
        }
    }

    /**
     * Gives the defined player a Minigame tool.
     *
     * @param player - The player to give the tool to.
     * @return The Minigame Tool
     */
    public static MinigameTool giveMinigameTool(MinigamePlayer player) {
        Material toolMat = Material.matchMaterial(Minigames.getPlugin().getConfig().getString("tool", ""));
        if (toolMat == null) {
            toolMat = Material.BLAZE_ROD;
            MinigameMessageManager.sendMgMessage(player, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NODEFAULTTOOL);
        }

        ItemStack tool = new ItemStack(toolMat);
        MinigameTool mgTool = new MinigameTool(tool);

        player.getPlayer().getInventory().addItem(mgTool.getTool());

        return mgTool;
    }

    /**
     * Checks if a player has a Minigame tool.
     *
     * @param player The player to check
     * @return false if the player doesn't have one.
     */
    public static boolean hasMinigameTool(MinigamePlayer player) {
        for (ItemStack item : player.getPlayer().getInventory().getContents()) {
            if (isMinigameTool(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a specific item is a Minigame tool
     *
     * @param item The item to check
     * @return false if the item was not a Minigame tool
     */
    public static boolean isMinigameTool(@Nullable ItemStack item) {
        return item != null && item.getItemMeta() != null && item.getItemMeta().displayName() != null &&// todo check something else to be sure
                MinigameMessageManager.getMgMessage(MgMiscLangKey.TOOL_NAME).contains(item.getItemMeta().displayName(), Component.EQUALS);
    }

    /**
     * Gets the item, Minigames considers as a Minigame tool, from the players inventory
     * It will prefer the item in main/offhand
     *
     * @param player The player to get the tool from
     * @return null if no tool was found
     */
    public static @Nullable MinigameTool getMinigameTool(@NotNull MinigamePlayer player) {
        ItemStack inHand = player.getPlayer().getInventory().getItemInMainHand();
        if (isMinigameTool(inHand)) {
            return new MinigameTool(inHand);
        }

        inHand = player.getPlayer().getInventory().getItemInOffHand();
        if (isMinigameTool(inHand)) {
            return new MinigameTool(inHand);
        }

        //was not in hands, search in inventory.
        for (ItemStack item : player.getPlayer().getInventory().getContents()) {
            if (isMinigameTool(item)) {
                return new MinigameTool(item);
            }
        }
        return null;
    }

    public @NotNull ItemStack getTool() {
        return tool;
    }

    public @Nullable Minigame getMinigame() {
        return minigame;
    }

    public void setMinigame(@NotNull Minigame minigame) {
        ItemMeta meta = tool.getItemMeta();
        List<Component> lore = Objects.requireNonNullElse(meta.lore(), new ArrayList<>());
        lore.set(0, MinigameMessageManager.getMgMessage(MgMiscLangKey.TOOL_SELECTED_MINIGAME_DESCRIPTION,
                Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName())));
        meta.lore(lore);
        tool.setItemMeta(meta);
        this.minigame = minigame;
    }

    public @Nullable ToolMode getMode() {
        return mode;
    }

    public void setMode(@NotNull ToolMode mode) {
        ItemMeta meta = tool.getItemMeta();
        List<Component> lore = Objects.requireNonNullElse(meta.lore(), new ArrayList<>());
        lore.set(1, MinigameMessageManager.getMgMessage(MgMiscLangKey.TOOL_SELECTED_MODE_DESCRIPTION,
                Placeholder.unparsed(MinigamePlaceHolderKey.TYPE.getKey(), WordUtils.capitalizeFully(mode.getName().replace("_", " ")))));
        meta.lore(lore);
        tool.setItemMeta(meta);
        this.mode = mode;
    }

    public @Nullable TeamColor getTeamColor() {
        return teamColor;
    }

    public void setTeamColor(@Nullable TeamColor color) {
        ItemMeta meta = tool.getItemMeta();
        List<Component> lore = Objects.requireNonNullElse(meta.lore(), new ArrayList<>());

        if (color == null) {
            lore.set(2, MinigameMessageManager.getMgMessage(MgMiscLangKey.TOOL_SELECTED_TEAM_DESCRIPTION,
                    Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(),
                            MinigameMessageManager.getMgMessage(MgMiscLangKey.QUANTIFIER_NONE))));
        } else {
            lore.set(2, MinigameMessageManager.getMgMessage(MgMiscLangKey.TOOL_SELECTED_TEAM_DESCRIPTION,
                    Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), color.getCompName())));
        }

        meta.lore(lore);
        tool.setItemMeta(meta);
        teamColor = color;
    }

    public @NotNull String getSetting(@NotNull String name) {
        return Objects.requireNonNullElse(customSetting.get(name),
                MinigameMessageManager.getStrippedMgMessage(MgMiscLangKey.QUANTIFIER_NONE));
    }

    public void setSetting(@NotNull String name, @NotNull String setting) {
        if (customSetting.containsKey(name)) {
            removeSetting(name);
        }

        customSetting.put(name, setting);

        ItemMeta meta = tool.getItemMeta();
        List<Component> lore = Objects.requireNonNullElse(meta.lore(), new ArrayList<>());
        lore.add(MinigameMessageManager.getMgMessage(MgMiscLangKey.TOOL_SELECTED_CUSTOM_DESCRIPTION,
                Placeholder.unparsed(MinigamePlaceHolderKey.TYPE.getKey(), name),
                Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), setting)));
        meta.lore(lore);
        tool.setItemMeta(meta);
    }

    public void removeSetting(@NotNull String name) {
        ItemMeta meta = tool.getItemMeta();
        List<Component> lore = meta.lore();

        if (lore != null && customSetting.remove(name) != null) {
            PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();

            for (Component l : lore) {
                if (StringUtils.startsWithIgnoreCase(plainSerializer.serialize(l), name)) {
                    lore.remove(l);
                    break;
                }
            }
            meta.lore(lore);
            tool.setItemMeta(meta);
        }
    }

    public void openMenu(final @NotNull MinigamePlayer player) {
        Menu menu = new Menu(2, MgMenuLangKey.MENU_TOOL_SETMODE_NAME, player);

        final MenuItemCustom miselect = new MenuItemCustom(Material.DIAMOND_BLOCK, MgMenuLangKey.MENU_TOOL_SELECT_NAME,
                MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_TOOL_SELECT_DESCRIPTION));
        miselect.setClick(() -> {
            if (minigame != null && mode != null) {
                mode.select(player, minigame, TeamsModule.getMinigameModule(minigame).getTeam(teamColor));
            }
            return miselect.getDisplayItem();
        });
        menu.addItem(miselect, menu.getSize() - 2);


        final MenuItemCustom mideselect = new MenuItemCustom(Material.GLASS, MgMenuLangKey.MENU_TOOL_DESELECT_NAME,
                MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_TOOL_DESELECT_DESCRIPTION));
        mideselect.setClick(() -> {
            if (minigame != null && mode != null) {
                mode.deselect(player, minigame, TeamsModule.getMinigameModule(minigame).getTeam(teamColor));
            }
            return mideselect.getDisplayItem();
        });
        menu.addItem(mideselect, menu.getSize() - 1);

        menu.addItem(new MenuItemToolTeam(Material.PAPER, MgMenuLangKey.MENU_TOOL_SETTEAM_NAME, new Callback<>() { //todo new MenuItemList("Lock to Team", Material.LEATHER_CHESTPLATE, loadout.getTeamColorCallback(), teams)

            @Override
            public TeamColor getValue() {
                if (getTeamColor() != null) {
                    return getTeamColor();
                }
                return TeamColor.NONE;
            }

            @Override
            public void setValue(TeamColor value) {
                setTeamColor(value);
            }
        }, Arrays.asList(TeamColor.values())), menu.getSize() - 3);

        for (ToolMode toolMode : ToolModes.getToolModes()) {
            menu.addItem(new MenuItemToolMode(toolMode.getIcon(), toolMode.getDisplayName(), toolMode.getDescription(), toolMode));
        }

        menu.displayMenu(player);
    }
}
