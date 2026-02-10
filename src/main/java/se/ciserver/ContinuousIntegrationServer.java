package se.ciserver;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.IOException;


/**
 * A Jetty-based CI-server that can start locally and recieve HTTP-requests.
 */
public class ContinuousIntegrationServer extends AbstractHandler
{
    /**
     * Handles incoming HTTP requests for the CI server.
     *
     * @param target      - The requested URL
     * @param baseRequest - Jetty-specific request object, used to mark the request as handled
     * @param request     - Standard Java Servlet request
     * @param response    - Standard Java Servlet response
     *
     * @throws IOException      If an input/output error occurs when reading the request or writing the response
     * @throws ServletException If an internal Jetty/Servlet error occurs while handling the request
     */
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException
    {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        System.out.println(target);

        response.getWriter().println("CI job done (placeholder)");
    }

    public static void setCommitStatus() {

        try {

            SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
            HttpClient client = new HttpClient(sslContextFactory);
            client.start();

            ContentResponse response = client.POST("https://api.github.com/repos/NAME/REPO/statuses/SHA")
                .header("Accept", "application/vnd.github+json")
                .header("Authorization", "Bearer GITHUB_AUTH_TOKEN")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .content(new StringContentProvider("{\"state\":\"success\",\"description\":\"test 3!\",\"context\":\"ci_tester_auto\"}"), "application/json")
                .send();

            client.stop();
        }
        catch (Exception e) {
            System.out.println(e);
        }   
        
    }

    /**
     * Starts the CI-server in command line
     *
     * @param args - Command-line arguments
     *
     * @throws Exception If the server fails to start or join the thread.
     */
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8080);
        server.setHandler(new ContinuousIntegrationServer());
        server.start();
        server.join();
    }

}