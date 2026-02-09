import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import se.ciserver.github.*;
import se.ciserver.TestUtils;

/**
 * Test class
 */
public class MainTest
{
    /**
     * Tests the PushParser class with a valid GitHub push payload
     * JSON file.
     *
     * @throws Exception
     */
    @Test
    public void pushParserValidPayload() throws Exception
    {
        String json = TestUtils.readFile("githubPush.json");
        PushParser parser = new PushParser();

        Push push = parser.parse(json);

        assertEquals("main", push.ref);
        assertEquals("e5f6g7h8", push.after);
        assertEquals("https://github.com/user/repo.git", push.repository.clone_url);
        assertEquals("name", push.pusher.name);
        assertEquals("Update README", push.head_commit.message);
    }

    /**
     * Tests the PushParser class with a invalid GitHub push payload
     * JSON file.
     *
     * @throws Exception
     */
    @Test(expected = InvalidPayloadException.class)
    public void pushParseInvalidPayload() throws Exception
    {
        String brokenJson = "{Invalid json}";

        PushParser parser = new PushParser();
        parser.parse(brokenJson);
    }
}