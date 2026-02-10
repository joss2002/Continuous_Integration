import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import se.ciserver.TestUtils;
import se.ciserver.ContinuousIntegrationServer;
import se.ciserver.build.Compiler;
import se.ciserver.build.CompilationResult;
import se.ciserver.github.PushParser;
import se.ciserver.github.Push;
import se.ciserver.github.InvalidPayloadException;

/**
 * Test class
 */
public class MainTest
{
    /**
     * Tests the CI-server for valid push event payload locally.
     *
     * @throws Exception If an input/output error occurs, if the server
     *                   fails to start or if sending the HTTP request
     *                   fails
     */
    @Test
    public void ciServerHandlePushValidPayloadLocal() throws Exception
    {
        Server server = new Server(0);
        server.setHandler(new ContinuousIntegrationServer());
        server.start();
        int port = ((ServerConnector) server.getConnectors()[0]).getLocalPort();

        String json = TestUtils.readFile("githubPush.json");

        URL url = new URL("http://localhost:" + port + "/webhook");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream())
        {
            os.write(json.getBytes());
        }

        assertEquals(200, conn.getResponseCode());

        server.stop();
        server.join();
    }

    /**
     * Tests the CI-server for invalid push event payload locally.
     *
     * @throws Exception If an input/output error occurs, if the server
     *                   fails to start or if sending the HTTP request
     *                   fails
     */
    @Test
    public void ciServerHandlePushInvalidPayloadLocal() throws Exception
    {
        Server server = new Server(0);
        server.setHandler(new ContinuousIntegrationServer());
        server.start();
        int port = ((ServerConnector) server.getConnectors()[0]).getLocalPort();

        String json = "{Invalid JSON}";

        URL url = new URL("http://localhost:" + port + "/webhook");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream())
        {
            os.write(json.getBytes());
        }

        assertEquals(400, conn.getResponseCode());

        server.stop();
        server.join();
    }

    /**
     * Tests the PushParser class with a valid GitHub push payload
     * JSON file.
     *
     * @throws Exception If the JSON file can not be read or if
     *                   parsing the payload fails
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
     * Tests the PushParser class with an invalid GitHub push payload
     * JSON file.
     *
     * @throws Exception If the payload can not be parsed
     */
    @Test(expected = InvalidPayloadException.class)
    public void pushParseInvalidPayload() throws Exception
    {
        String brokenJson = "{Invalid json}";

        PushParser parser = new PushParser();
        parser.parse(brokenJson);
    }

    /**
     * Tests that CompilationResult correctly stores success and output.
     */
    @Test
    public void compilationResultStoresValues()
    {
        CompilationResult success = new CompilationResult(true, "BUILD SUCCESS");
        assertEquals(true, success.success);
        assertEquals("BUILD SUCCESS", success.output);

        CompilationResult failure = new CompilationResult(false, "BUILD FAILURE");
        assertEquals(false, failure.success);
        assertEquals("BUILD FAILURE", failure.output);
    }

    /**
     * Tests that the Compiler handles a clone failure gracefully
     * by returning a failed CompilationResult.
     */
    @Test
    public void compilerHandlesCloneFailure()
    {
        Compiler failCompiler = new Compiler()
        {
            @Override
            protected ProcessBuilder createProcessBuilder(String... command)
            {
                return new ProcessBuilder("false");
            }
        };

        CompilationResult result = failCompiler.compile(
            "https://invalid-url.example.com/repo.git", "main", "abc123");

        assertEquals(false, result.success);
    }
}