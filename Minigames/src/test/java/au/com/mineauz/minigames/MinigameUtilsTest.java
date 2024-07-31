package au.com.mineauz.minigames;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.regex.Pattern;

public class MinigameUtilsTest {

    @Test
    public void TestSanitize() {
        Assertions.assertNull(this.sanitize("EnderJump2.0"));
        Assertions.assertEquals("TestGame", this.sanitize("TestGame"));
        Assertions.assertEquals("asdasgfar231123asd__", this.sanitize("asdasgfar231123asd__"));
    }

    private @Nullable String sanitize(@NotNull String input) {
        final Pattern pattern = Pattern.compile("^[a-zA-Z\\d_]+$");
        return !pattern.matcher(input).matches() ? null : input;
    }
}
