package se.ciserver.github;

/**
 * Defines a simple GitHub repository.
 */
public class Repository
{
    public final String id;
    public final String name;
    public final String clone_url;
    public final Author owner;

    /**
     * Constructs a GitHub repository.
     *
     * @param id        The Node ID of the Repository object
     * @param name      The name of the repository
     * @param clone_url The clone URL of the repository
     * @param owner     The User owner of the repository
     */
    public Repository(String id, String name, String clone_url, Author owner)
    {
        this.id        = id;
        this.name      = name;
        this.clone_url = clone_url;
        this.owner     = owner;
    }
}