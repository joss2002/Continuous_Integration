package se.ciserver.github;

/**
 * Defines a GitHub pusher.
 */
public class Pusher
{
    public final String name;

    /**
     * Constructs a GitHub Pusher.
     *
     * @param name The git author's name
     */
    public Pusher(String name)
    {
        this.name = name;
    }
}