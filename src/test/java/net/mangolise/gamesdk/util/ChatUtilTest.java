package net.mangolise.gamesdk.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ChatUtilTest {

    @Test
    public void capitaliseFirstLetter() {
        Assertions.assertEquals("Hello", ChatUtil.capitaliseFirstLetter("hello"));
        Assertions.assertEquals("Hello", ChatUtil.capitaliseFirstLetter("Hello"));
        Assertions.assertEquals("Hello", ChatUtil.capitaliseFirstLetter("hELLO"));
        Assertions.assertEquals("Hello", ChatUtil.capitaliseFirstLetter("HELLO"));
    }

    @Test
    public void snakeCaseToTitleCase() {
        Assertions.assertEquals("Hello World", ChatUtil.snakeCaseToTitleCase("hello_world"));
        Assertions.assertEquals("Hello World", ChatUtil.snakeCaseToTitleCase("Hello_World"));
        Assertions.assertEquals("Hello World", ChatUtil.snakeCaseToTitleCase("hELLO_wORLD"));
        Assertions.assertEquals("Hello World", ChatUtil.snakeCaseToTitleCase("HELLO_WORLD"));
    }
}
