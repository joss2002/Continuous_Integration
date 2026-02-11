import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

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
     * Tests that CompilationResult correctly stores a successful build.
     */
    @Test
    public void compilationResultStoresSuccess()
    {
        CompilationResult result = new CompilationResult(true, "BUILD SUCCESS");
        assertTrue(result.success);
        assertEquals("BUILD SUCCESS", result.output);
    }

    /**
     * Tests that CompilationResult correctly stores a failed build.
     */
    @Test
    public void compilationResultStoresFailure()
    {
        CompilationResult result = new CompilationResult(false, "BUILD FAILURE");
        assertFalse(result.success);
        assertEquals("BUILD FAILURE", result.output);
    }

    /**
     * Tests that the Compiler handles a clone failure gracefully
     * by returning a failed CompilationResult instead of throwing.
     */
    @Test
    public void compilerHandlesCloneFailure()
    {
        // Override createProcessBuilder to simulate a failing git clone
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

        assertFalse(result.success);
        assertNotNull(result.output);
    }

    /**
     * Tests that the Compiler returns a successful CompilationResult
     * when all process steps (clone, checkout, compile) succeed.
     */
    @Test
    public void compilerReturnsSuccessWhenAllStepsPass()
    {
        // Override createProcessBuilder to simulate all commands succeeding
        Compiler successCompiler = new Compiler()
        {
            @Override
            protected ProcessBuilder createProcessBuilder(String... command)
            {
                return new ProcessBuilder("true");
            }
        };

        CompilationResult result = successCompiler.compile(
            "https://example.com/repo.git", "main", "abc123");

        assertTrue(result.success);
    }

    /**
     * Tests the CI-server webhook endpoint with a valid push payload
     * and verifies the response contains the compilation result.
     * Uses a mock server to avoid cloning a real repository.
     *
     * @throws Exception If the server fails to start or the HTTP request fails
     */
    @Test
    public void ciServerHandleCompilationOnPush() throws Exception
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

        // Server should return 200 regardless of compilation outcome
        assertEquals(200, conn.getResponseCode());

        // Verify the response body mentions the compilation result
        String body;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream())))
        {
            body = reader.lines().collect(Collectors.joining());
        }

        // Response should contain the commit SHA from the payload
        assertTrue(body.contains("e5f6g7h8"));
        // Response should indicate compilation status
        assertTrue(body.contains("Compilation"));

        server.stop();
        server.join();
    }
}