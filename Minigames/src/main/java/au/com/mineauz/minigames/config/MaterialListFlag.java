package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.MenuItem;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MaterialListFlag extends Flag<List<Material>> {

    public MaterialListFlag(List<Material> value, String name) {
        setFlag(value);
        setDefaultFlag(value);
        setName(name);
    }

    @Override
    public void saveValue(String path, FileConfiguration config) {
        if (!getFlag().isEmpty()) {
            MaterialFlag matflag;
            for (int i = 0; i < getFlag().size(); i++) {
                matflag = new MaterialFlag(null, getName() + "." + i);
                matflag.setFlag(getFlag().get(i));
                matflag.saveValue(path, config);
            }
        }
    }

    @Override
    public void loadValue(String path, FileConfiguration config) {
        List<Material> materials = new ArrayList<>();
        Set<String> ids = config.getConfigurationSection(path + "." + getName()).getKeys(false);
        MaterialFlag matFlag;

        for (int i = 0; i < ids.size(); i++) {
            matFlag = new MaterialFlag(null, getName() + "." + i);
            matFlag.loadValue(path, config);
            materials.add(matFlag.getFlag());
        }
        setFlag(materials);
    }

    @Override
    public MenuItem getMenuItem(String name, Material displayItem) {
        return null;
    }

    @Override
    public MenuItem getMenuItem(String name, Material displayItem,
                                List<String> description) {
        return null;
    }

}
