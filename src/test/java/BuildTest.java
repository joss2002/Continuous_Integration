import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import se.ciserver.buildlist.Build;

public class BuildTest {

    /**
     * Test if build object can be created normally
     */
    @Test
    public void newBuild_setsAllFields() {
        String commitId = "abc123";
        String branch = "assessment";
        Build.Status status = Build.Status.SUCCESS;
        String log = "build output";

        Build b = Build.newBuild(commitId, branch, status, log);

        assertNotNull("id should be generated", b.id);
        assertFalse("id should not be empty", b.id.isEmpty());
        assertEquals("commit id should match", commitId, b.commitId);
        assertEquals("branch should match", branch, b.branch);
        assertEquals("status should match", status, b.status);
        assertEquals("log should match", log, b.log);
        assertNotNull("timestamp should be set", b.timestamp);
    }
}

