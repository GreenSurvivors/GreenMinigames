package au.com.mineauz.minigames.objects;

import be.seeseemelk.mockbukkit.block.state.SignMock;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public class MockSign extends SignMock implements Sign {
    private final LinkedList<String> lines = new LinkedList<>();
    private boolean edittable;

    public MockSign(Material material, boolean edittable) {
        super(material);

        this.edittable = edittable;
        lines.add("");
        lines.addLast("");
        lines.addLast("");
        lines.addLast("");
    }

    @Override
    @Deprecated
    public String[] getLines() {
        String[] out = new String[lines.size()];
        return lines.toArray(out);
    }

    @Override
    @Deprecated
    public @NotNull String getLine(int i) throws IndexOutOfBoundsException {
        return lines.get(i);
    }


    @Override
    @Deprecated
    public void setLine(int i, @NotNull String s) throws IndexOutOfBoundsException {
        lines.set(i, s);
    }

    @Override
    public boolean isEditable() {
        return edittable;
    }

    @Override
    public void setEditable(boolean b) {
        edittable = b;
    }

    @Override
    public @NotNull Side getInteractableSideFor(double x, double z) {
        return Side.FRONT;
    }
}
