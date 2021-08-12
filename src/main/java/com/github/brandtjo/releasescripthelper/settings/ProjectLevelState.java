package com.github.brandtjo.releasescripthelper.settings;

import com.github.brandtjo.releasescripthelper.model.Options;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
		name = "com.github.brandtjo.releasescripthelper.settings.ProjectLevelState",
		storages = {@Storage("ReleaseScriptHelperPlugin.xml")}
)
public class ProjectLevelState implements PersistentStateComponent<ProjectLevelState> {

	public Options options = new Options();

	public static ProjectLevelState getInstanceFor(Project project) {
		return ServiceManager.getService(project, ProjectLevelState.class);
	}

	@Override
	public @Nullable ProjectLevelState getState() {
		return this;
	}

	@Override
	public void loadState(@NotNull ProjectLevelState state) {
		XmlSerializerUtil.copyBean(state, this);
	}
}
