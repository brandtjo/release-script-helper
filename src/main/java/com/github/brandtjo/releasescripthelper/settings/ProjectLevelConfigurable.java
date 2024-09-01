package com.github.brandtjo.releasescripthelper.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
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
		boolean modified = !component.getDefaultDirectory().equals(settings.getOptions().getDefaultDirectory());
		modified |= component.isUseCustomScriptNumber() != settings.getOptions().getUseCustomScriptNumber();
		modified |= component.isUseUnixTimestamp() == settings.getOptions().getUseCustomScriptNumber();
		modified |= !Arrays.equals(component.getFileEndings().toArray(new String[0]),
				settings.getOptions().getFileEndings().toArray(new String[0]));
		modified |= !Arrays.equals(component.getTicketTypes().toArray(new String[0]),
				settings.getOptions().getTicketTypes().toArray(new String[0]));
		return modified;
	}

	@Override
	public void apply() {
		ProjectLevelState settings = ProjectLevelState.getInstanceFor(project);
		settings.getOptions().setDefaultDirectory(component.getDefaultDirectory());
		settings.getOptions().setUseCustomScriptNumber(component.isUseCustomScriptNumber());
		settings.getOptions().setTicketTypes(component.getTicketTypes());
		settings.getOptions().setFileEndings(component.getFileEndings());
	}

	@Override
	public void reset() {
		ProjectLevelState settings = ProjectLevelState.getInstanceFor(project);
		component.setDefaultDirectory(settings.getOptions().getDefaultDirectory());
		component.setUseCustomScriptNumber(settings.getOptions().getUseCustomScriptNumber());
		component.setUseUnixTimeStamp(!settings.getOptions().getUseCustomScriptNumber());
		component.setTicketTypes(new ArrayList<>(settings.getOptions().getTicketTypes()));
		component.setFileEndings(new ArrayList<>(settings.getOptions().getFileEndings()));
	}

	@Override
	public void disposeUIResources() {
		component = null;
	}
}
