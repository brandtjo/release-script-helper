package com.github.brandtjo.releasescripthelper.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;

public class ProjectLevelConfigurable implements Configurable {

	private final Project project;
	private ProjectLevelComponent component;

	public ProjectLevelConfigurable(Project project) {
		this.project = project;
	}

	@Nls(capitalization = Nls.Capitalization.Title)
	@Override
	public String getDisplayName() {
		return "Release Script Helper Plugin Settings";
	}

	@Nullable
	@Override
	public JComponent createComponent() {
		component = new ProjectLevelComponent(project);
		return component.getPanel();
	}

	@Override
	public boolean isModified() {
		ProjectLevelState settings = ProjectLevelState.getInstanceFor(project);
		boolean modified = !component.getDefaultDirectory().equals(settings.options.getDefaultDirectory());
		modified |= component.isUseCustomScriptNumber() != settings.options.getUseCustomScriptNumber();
		modified |= component.isUseUnixTimestamp() == settings.options.getUseCustomScriptNumber();
		modified |= !Arrays.equals(component.getFileEndings().toArray(new String[0]),
				settings.options.getFileEndings());
		modified |= !Arrays.equals(component.getTicketTypes().toArray(new String[0]),
				settings.options.getTicketTypes());
		return modified;
	}

	@Override
	public void apply() {
		ProjectLevelState settings = ProjectLevelState.getInstanceFor(project);
		settings.options.setDefaultDirectory(component.getDefaultDirectory());
		settings.options.setUseCustomScriptNumber(component.isUseCustomScriptNumber());
		settings.options.setTicketTypes(component.getTicketTypes().toArray(new String[0]));
		settings.options.setFileEndings(component.getFileEndings().toArray(new String[0]));
	}

	@Override
	public void reset() {
		ProjectLevelState settings = ProjectLevelState.getInstanceFor(project);
		component.setDefaultDirectory(settings.options.getDefaultDirectory());
		component.setUseCustomScriptNumber(settings.options.getUseCustomScriptNumber());
		component.setUseUnixTimeStamp(!settings.options.getUseCustomScriptNumber());
		component.setTicketTypes(Arrays.asList(settings.options.getTicketTypes()));
		component.setFileEndings(Arrays.asList(settings.options.getFileEndings()));
	}

	@Override
	public void disposeUIResources() {
		component = null;
	}
}
