package se.ciserver;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Utility methods for unit tests.
 */
public final class TestUtils
{
    /**
     * Constructor to prevent instantiation.
     */
    private TestUtils() {}

    /**
     * Reads a given test file or throws Exception if the file does not exist.
     *
     * @param filename Name of the test file
     *
     * @return The file content as a string.
     *
     * @throws Exception When the file does not exist.
     */
    public static String readFile(String filename) throws Exception
    {
        try (InputStream is = TestUtils.class.getClassLoader().getResourceAsStream(filename))
        {
            if (is == null)
            {
                throw new IllegalArgumentException("Test resource not found: " + filename);
            }

            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
