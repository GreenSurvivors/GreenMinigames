

    @Override
    public ItemStack onClick() {
        Menu potionMenu = new Menu(5, getContainer().getName(), getContainer().getViewer());

        potionMenu.setAllowModify(true);
        potionMenu.setPreviousPage(getContainer());
        potionMenu.addItem(new MenuItemPotionAdd(MenuUtility.getCreateMaterial(), MgMenuLangKey.MENU_POTIONADD_NAME, loadout), 43);
        potionMenu.addItem(new MenuItemPage(MenuUtility.getSaveMaterial(),
                MgMenuLangKey.MENU_EFFECTS_SAVE_NAME,
                getContainer().getPreviousPage()), 44);

        int inc = 0;
        for (PotionEffect eff : loadout.getAllPotionEffects()) {
            potionMenu.addItem(new MenuItemPotion(Material.POTION,
                    Component.translatable(eff.getType().translationKey()),
                    List.of(MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_DELETE_SHIFTRIGHTCLICK)), // <-----
                    eff, loadout), inc);
            inc++;
        }

        potionMenu.displayMenu(getContainer().getViewer());

        return null;
    }
}
