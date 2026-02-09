package se.ciserver.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handles parsing of GitHub push event.
 */
public class PushParser
{
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Parses GitHub push event.
     *
     * @param json The JSON push event file
     *
     * @return A Push object defining the attributes of the JSON push file.
     *
     * @throws InvalidPayloadException If the payload object has invalid attributes.
     */
    public Push parse(String json) throws InvalidPayloadException
    {
        try
        {
            JsonNode root = mapper.readTree(json);

            JsonNode commitsNode    = root.get("commits");
            JsonNode headCommitNode = root.get("head_commit");
            JsonNode repositoryNode = root.get("repository");
            JsonNode ownerNode      = repositoryNode.get("owner");

            String after    = root.get("after").asText();
            String base_ref = root.get("base_ref") == null || root.get("base_ref").isNull() ? null : root.get("base_ref").asText();
            String before   = root.get("before").asText();
            String ref      = root.get("ref").asText().replace("refs/heads/", "");
            String compare  = root.get("compare").asText();

            boolean created = root.get("created").asBoolean();
            boolean deleted = root.get("deleted").asBoolean();
            boolean forced  = root.get("forced").asBoolean();

            Pusher pusher = new Pusher(root.get("pusher").get("name").asText());


            Commit[] commits = new Commit[commitsNode.size()];

            for (int commitIndex = 0; commitIndex < commitsNode.size(); commitIndex++)
            {
                JsonNode commitNode    = commitsNode.get(commitIndex);
                JsonNode authorNode    = commitNode.get("author");
                JsonNode committerNode = commitNode.get("committer");

                Author author    = new Author(authorNode.get("email").asText(),
                                              authorNode.get("name").asText());
                Author committer = new Author(committerNode.hasNonNull("email") ? committerNode.get("email").asText() : null,
                                              committerNode.get("name").asText());

                commits[commitIndex] = new Commit(author, committer, commitNode.get("distinct").asBoolean(),
                                                  commitNode.get("id").asText(), commitNode.get("message").asText(), commitNode.get("timestamp").asText(),
                                                  commitNode.get("tree_id").asText(), commitNode.get("url").asText());

            }

            Commit head_commit;

            if(headCommitNode != null)
            {
                JsonNode headAuthorNode    = headCommitNode.get("author");
                JsonNode headCommitterNode = headCommitNode.get("committer");

                Author author = new Author(headAuthorNode.hasNonNull("email") ? headAuthorNode.get("email").asText() : null,
                        headAuthorNode.get("name").asText());
                Author committer = new Author(headCommitterNode.hasNonNull("email") ? headCommitterNode.get("email").asText() : null,
                                              headCommitterNode.get("name").asText());
                head_commit = new Commit(author, committer, headCommitNode.get("distinct").asBoolean(),
                                         headCommitNode.get("id").asText(), headCommitNode.get("message").asText(), headCommitNode.get("timestamp").asText(),
                                         headCommitNode.get("tree_id").asText(), headCommitNode.get("url").asText());
            }
            else
                head_commit = null;

            Author owner          = new Author(ownerNode.hasNonNull("email") ? ownerNode.get("email").asText() : null,
                                               ownerNode.get("name").asText());
            Repository repository = new Repository(repositoryNode.get("id").asText(),
                                                   repositoryNode.get("name").asText(),
                                                   repositoryNode.get("clone_url").asText(),
                                                   owner);

            return new Push(after, base_ref, before,
                            commits, compare, created,
                            deleted, forced, head_commit,
                            pusher, ref, repository);

        }
        catch (Exception e)
        {
            throw new InvalidPayloadException("Invalid GitHub push payload", e);
        }
    }
}