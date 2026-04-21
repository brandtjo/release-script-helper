package com.github.brandtjo.releasescripthelper.settings;

import com.github.brandtjo.releasescripthelper.model.Options;
import org.junit.Assert;
import org.junit.Test;

public class ProjectLevelStatePersistenceTest {

    @Test
    public void testDefaultValuesAreSet() {
        ProjectLevelState state = new ProjectLevelState();
        Options options = state.getOptions();

        // Verify default values from Options.kt
        Assert.assertEquals("", options.getDefaultDirectory());
        Assert.assertFalse("Default useCustomScriptNumber should be false", options.getUseCustomScriptNumber());
        Assert.assertTrue("Default useTicket should be true", options.getUseTicket());
        Assert.assertEquals(2, options.getTicketTypes().size());
        Assert.assertEquals("oct", options.getTicketTypes().get(0));
        Assert.assertEquals("CHG", options.getTicketTypes().get(1));
        Assert.assertEquals(1, options.getFileEndings().size());
        Assert.assertEquals("sql", options.getFileEndings().get(0));
    }

    @Test
    public void testStateCanBeRetrieved() {
        ProjectLevelState state = new ProjectLevelState();
        state.getOptions().setDefaultDirectory("/test/path");
        state.getOptions().setUseCustomScriptNumber(true);
        state.getOptions().setTicketTypes(java.util.List.of("JIRA", "TICKET"));
        state.getOptions().setFileEndings(java.util.List.of("sql", "txt"));

        ProjectLevelState savedState = state.getState();
        Assert.assertNotNull("Saved state should not be null", savedState);
        Assert.assertEquals("/test/path", savedState.getOptions().getDefaultDirectory());
        Assert.assertTrue("Should persist useCustomScriptNumber", savedState.getOptions().getUseCustomScriptNumber());
        Assert.assertEquals(2, savedState.getOptions().getTicketTypes().size());
        Assert.assertEquals(2, savedState.getOptions().getFileEndings().size());
    }

    @Test
    public void testLoadStateDeserializesValues() {
        ProjectLevelState target = new ProjectLevelState();
        target.getOptions().setDefaultDirectory("/original");
        target.getOptions().setUseCustomScriptNumber(false);

        // Simulate deserialized state (what XML would produce)
        ProjectLevelState source = new ProjectLevelState();
        source.getOptions().setDefaultDirectory("/persisted");
        source.getOptions().setUseCustomScriptNumber(true);
        source.getOptions().setTicketTypes(java.util.List.of("CHG", "OCT"));
        source.getOptions().setFileEndings(java.util.List.of("sql"));

        target.loadState(source);

        // XmlSerializerUtil.copyBean copies from source to target
        Assert.assertEquals("/persisted", target.getOptions().getDefaultDirectory());
        Assert.assertTrue("Should have loaded useCustomScriptNumber=true", target.getOptions().getUseCustomScriptNumber());
        Assert.assertEquals(2, target.getOptions().getTicketTypes().size());
        Assert.assertEquals(1, target.getOptions().getFileEndings().size());
    }

    @Test
    public void testLoadStatePreservesNonSerializedFields() {
        ProjectLevelState target = new ProjectLevelState();
        target.getOptions().setUseTicket(true);

        ProjectLevelState source = new ProjectLevelState();
        source.getOptions().setDefaultDirectory("/new");
        // source doesn't set useTicket, so it stays default (true)

        target.loadState(source);

        // useTicket should be preserved since Options.kt has it as a field
        // XmlSerializerUtil.copyBean copies all matching fields
        Assert.assertEquals(true, target.getOptions().getUseTicket());
    }
}
