package se.ciserver.github;

/**
 * Defines a GitHub author.
 */
public class Author
{
    public final String email;
    public final String name;

    /**
     * Contstucts a GitHub Author.
     *
     * @param email The git author's email
     * @param name  The git author's name
     */
    public Author(String email, String name)
    {
        this.email = email;
        this.name  = name;
    }
}