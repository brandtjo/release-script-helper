package com.github.brandtjo.releasescripthelper.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ProjectExplorerAddScript extends BasicAddScript {

	@Override
	public void actionPerformed(@NotNull AnActionEvent event) {

		Optional<PsiDirectory> defaultDirectory = getDefaultDirectory();
		PsiDirectory directory;

		boolean selectedbyUser = false;
		Object navigatable = event.getData(CommonDataKeys.NAVIGATABLE);
		if (navigatable instanceof PsiDirectory) {
			directory = (PsiDirectory) navigatable;
			selectedbyUser = true;
		} else if (defaultDirectory.isPresent()) {
			directory = defaultDirectory.get();
		} else {
			Messages.showInfoMessage(currentProject, "Select a default directory in settings or select one " +
					"to create a release script in.", "Could Not Create Release Script");
			return;
		}

		createAndOpenReleaseScript(directory, selectedbyUser, Optional.ofNullable(event.getData(CommonDataKeys.EDITOR))
				.map(Editor::getSelectionModel)
				.map(model -> model.getSelectedText(true))
				.filter(StringUtils::isNotBlank)
				.orElse(null));
	}

}
