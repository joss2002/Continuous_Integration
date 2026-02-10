package se.ciserver.build;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Handles cloning a repository, checking out a specific commit,
 * and running Maven compilation.
 */
public class Compiler
{
    /**
     * Clones the repository, checks out the specified commit, and
     * runs {@code mvn clean compile}.
     *
     * @param cloneUrl  The clone URL of the repository
     * @param branch    The branch name to clone
     * @param commitSha The commit SHA to checkout
     *
     * @return A {@link CompilationResult} indicating success/failure and build output
     */
    public CompilationResult compile(String cloneUrl, String branch, String commitSha)
    {
        Path tempDir = null;

        try
        {
            tempDir = Files.createTempDirectory("ci-build-");

            // Clone the specific branch
            int cloneExit = runProcess(tempDir.getParent(),
                "git", "clone", "--branch", branch, "--single-branch",
                cloneUrl, tempDir.toString());

            if (cloneExit != 0)
            {
                return new CompilationResult(false,
                    "Git clone failed with exit code " + cloneExit);
            }

            // Checkout the exact commit
            int checkoutExit = runProcess(tempDir,
                "git", "checkout", commitSha);

            if (checkoutExit != 0)
            {
                return new CompilationResult(false,
                    "Git checkout failed with exit code " + checkoutExit);
            }

            // Run Maven compile and capture output
            return runCompilation(tempDir);
        }
        catch (IOException | InterruptedException e)
        {
            return new CompilationResult(false,
                "Compilation error: " + e.getMessage());
        }
        finally
        {
            if (tempDir != null)
            {
                cleanup(tempDir);
            }
        }
    }

    /**
     * Runs a process and returns its exit code.
     * Output is printed to System.out for server console visibility.
     *
     * @param workDir The working directory for the process
     * @param command The command and its arguments
     *
     * @return The process exit code
     *
     * @throws IOException          If an I/O error occurs
     * @throws InterruptedException If the process is interrupted
     */
    private int runProcess(Path workDir, String... command)
            throws IOException, InterruptedException
    {
        ProcessBuilder pb = createProcessBuilder(command);
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                System.out.println(line);
            }
        }

        return process.waitFor();
    }

    /**
     * Runs {@code mvn clean compile} in the given directory and captures output.
     *
     * @param workDir The directory containing the Maven project
     *
     * @return A {@link CompilationResult} with the build outcome
     *
     * @throws IOException          If an I/O error occurs
     * @throws InterruptedException If the process is interrupted
     */
    private CompilationResult runCompilation(Path workDir)
            throws IOException, InterruptedException
    {
        ProcessBuilder pb = createProcessBuilder("mvn", "clean", "compile");
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();

        String output;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())))
        {
            output = reader.lines()
                .collect(Collectors.joining(System.lineSeparator()));
        }

        int exitCode = process.waitFor();

        System.out.println(output);

        return new CompilationResult(exitCode == 0, output);
    }

    /**
     * Creates a ProcessBuilder for the given command.
     * Protected so tests can override to avoid spawning real processes.
     *
     * @param command The command and its arguments
     *
     * @return A new ProcessBuilder configured for the command
     */
    protected ProcessBuilder createProcessBuilder(String... command)
    {
        return new ProcessBuilder(command);
    }

    /**
     * Recursively deletes the temporary build directory.
     *
     * @param directory The directory to delete
     */
    private void cleanup(Path directory)
    {
        try
        {
            Files.walk(directory)
                 .sorted(Comparator.reverseOrder())
                 .forEach(path ->
                 {
                     try
                     {
                         Files.delete(path);
                     }
                     catch (IOException e)
                     {
                         /* best effort cleanup */
                     }
                 });
        }
        catch (IOException e)
        {
            System.err.println("Warning: Failed to clean up " + directory);
        }
    }
}
