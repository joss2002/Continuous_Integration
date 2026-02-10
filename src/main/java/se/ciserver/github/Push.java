package se.ciserver.github;

/**
 * Defines a Github push.
 */
public class Push
{
    public final String after;
    public final String base_ref;
    public final String before;
    public final Commit[] commits;
    public final String compare;
    public final boolean created;
    public final boolean deleted;
    public final boolean forced;
    public final Commit head_commit;
    public final Pusher pusher;
    public final String ref;
    public final Repository repository;

    /**
     * Constructs a GitHub Push.
     *
     * @param after       The SHA of the most recent commit on ref after the push
     * @param base_ref    The base branch for the push
     * @param before      The SHA of the most recent commit on ref before the push
     * @param commits     An array of commit objects describing the pushed commits
     * @param compare     URL that shows the changes in this ref update, from the before commit to the after commit
     * @param created     Whether this push created the ref
     * @param deleted     Whether this push deleted the ref
     * @param forced      Whether this push was a force push of the ref
     * @param head_commit The most recent commit included in the push
     * @param pusher      Metaproperties for Git author/committer information
     * @param ref         The full git ref that was pushed
     * @param repository  A git repository
     */
    public Push(String after, String base_ref, String before,
                Commit[] commits, String compare, boolean created,
                boolean deleted, boolean forced, Commit head_commit,
                Pusher pusher, String ref, Repository repository)
    {
        this.after       = after;
        this.base_ref    = base_ref;
        this.before      = before;
        this.commits     = commits;
        this.compare     = compare;
        this.created     = created;
        this.deleted     = deleted;
        this.forced      = forced;
        this.head_commit = head_commit;
        this.pusher      = pusher;
        this.ref         = ref;
        this.repository  = repository;
    }
}