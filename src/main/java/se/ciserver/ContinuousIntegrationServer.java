package se.ciserver;

import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import se.ciserver.buildlist.Build;
import se.ciserver.buildlist.BuildStore;
import se.ciserver.github.InvalidPayloadException;
import se.ciserver.github.Push;
import se.ciserver.github.PushParser;

/**
 * A Jetty-based CI-server that can start locally and receive HTTP-requests.
 */
public class ContinuousIntegrationServer extends AbstractHandler
{
    private final PushParser parser = new PushParser();
    private final BuildStore store = new BuildStore("buildhist/build-history.json");

    /**
     * Handles incoming HTTP requests for the CI server and presents necessary information.
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
        if ("/webhook".equals(target) && "POST".equalsIgnoreCase(request.getMethod()))
        {
            String json = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

            try
            {
                Push push = parser.parse(json);
                // TODO: 1. To add respective build results
                // 2. Wrap into build and persist
                // 3. Response with info and URL
                // To be added after compilation branch

                System.out.println("\nReceived push on branch : " + push.ref +
                                   "\nAfter SHA               : " + push.after +
                                   "\nRepository URL          : " + push.repository.clone_url +
                                   "\nPusher name             : " + push.pusher.name +
                                   "\n\nHead commit message     : " + push.head_commit.message);

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println("Push received: " + push.after);
            }
            catch (InvalidPayloadException e)
            {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Invalid payload: " + e.getMessage());
            }

            baseRequest.setHandled(true);
        }
        else if ("/builds".equals(target) && "GET".equalsIgnoreCase(request.getMethod())) {
            response.setContentType("text/html;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);

            StringBuilder html = new StringBuilder();
            html.append("<html><body><h1>Build history</h1><ul>");

            for (Build b : store.getAll()) {
                html.append("<li>")
                    .append("<a href=\"/builds/").append(b.id).append("\">")
                    .append(b.id).append("</a>")
                    .append(" — commit ").append(b.commitId)
                    .append(" (").append(b.branch).append(") ")
                    .append(b.timestamp)
                    .append(" [").append(b.status).append("]")
                    .append("</li>");
            }

            html.append("</ul></body></html>");
            response.getWriter().println(html.toString());
            baseRequest.setHandled(true);
        }
        else if (target.startsWith("/builds/") && "GET".equalsIgnoreCase(request.getMethod())) {
            String id = target.substring("/builds/".length());
            Build b = store.getById(id);

            if (b == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().println("Build not found");
            } else {
                response.setContentType("text/html;charset=utf-8");
                response.setStatus(HttpServletResponse.SC_OK);

                StringBuilder html = new StringBuilder();
                html.append("<html><body>")
                    .append("<h1>Build ").append(b.id).append("</h1>")
                    .append("<p>Commit: ").append(b.commitId).append("</p>")
                    .append("<p>Branch: ").append(b.branch).append("</p>")
                    .append("<p>Date: ").append(b.timestamp).append("</p>")
                    .append("<p>Status: ").append(b.status).append("</p>")
                    .append("<h2>Log</h2>")
                    .append("<pre>").append(b.log).append("</pre>")
                    .append("</body></html>");

                response.getWriter().println(html.toString());
            }
            baseRequest.setHandled(true);
        }
        else // Placeholder for other endpoints
        {
            response.setContentType("text/html;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);

            System.out.println(target);

            response.getWriter().println("CI job done (placeholder)");
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