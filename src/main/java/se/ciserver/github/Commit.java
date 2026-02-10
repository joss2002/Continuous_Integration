package se.ciserver.github;

/**
 * Defines a GitHub commit.
 */
public class Commit
{
    public final Author author;
    public final Author committer;
    public final boolean distinct;
    public final String id;
    public final String message;
    public final String timestamp;
    public final String tree_id;
    public final String url;

    /**
     * Constructs a GitHub Commit.
     *
     * @param author    Metaproperties for Git author/committer information
     * @param committer Metaproperties for Git author/committer information
     * @param distinct  Whether this commit is distinct from any that have been pushed before
     * @param id        The commit identifier
     * @param message   The commit message
     * @param timestamp The ISO 8601 timestamp of the commit
     * @param tree_id   The id of the tree the commit points at describing the current condition of file system
     * @param url       URL that points to the commit API resource
     */
    public Commit(Author author, Author committer, boolean distinct,
                  String id, String message, String timestamp,
                  String tree_id, String url)
    {
        this.author     = author;
        this.committer  = committer;
        this.distinct   = distinct;
        this.id         = id;
        this.message    = message;
        this. timestamp = timestamp;
        this.tree_id    = tree_id;
        this.url        = url;
    }
}