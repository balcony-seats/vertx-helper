package io.github.balconyseats.vertx.helper.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class StringHelperTest {

    public static final char DELIMITER = ':';

    @Test
    public void testSplitEnclosed_whenNullOrEmptyStringIsProvided() {
        List<String> got = StringHelper.splitEnclosed("", DELIMITER, "\"", "'");
        Assertions.assertThat(got).isEmpty();

        got = StringHelper.splitEnclosed(null, DELIMITER, "\"", "'");
        Assertions.assertThat(got).isEmpty();
    }

    @Test
    public void testSplitEnclosed_whenEnclosingNotInString() {
        List<String> got = StringHelper.splitEnclosed("a:b:ab:c:d:f", DELIMITER, "\"", "'");
        Assertions.assertThat(got).containsExactly("a", "b", "ab", "c", "d", "f");
    }

    @Test
    public void testSplitEnclosed_whenEnclosed() {
        List<String> got = StringHelper.splitEnclosed("a:'b:c':\"a:b\":\"c':d\":'\"d\"':f", DELIMITER, "\"", "'");
        Assertions.assertThat(got).containsExactly("a", "b:c", "a:b", "c':d", "\"d\"", "f");
    }

    @Test
    public void testSplitEnclosed_whenEnclosedAndEscaped() {
        List<String> got = StringHelper.splitEnclosed("a:'b\\\\\\'\\\":c':\"a:b\":\"c':d\":'\"d\"':f", DELIMITER, "\"", "'");
        Assertions.assertThat(got).containsExactly("a", "b\\'\":c", "a:b", "c':d", "\"d\"", "f");
    }

}