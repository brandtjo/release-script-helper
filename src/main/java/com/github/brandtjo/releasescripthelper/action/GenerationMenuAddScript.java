package com.github.brandtjo.releasescripthelper.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class GenerationMenuAddScript extends BasicAddScript {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Editor editor = e.getData(CommonDataKeys.EDITOR);

        Optional<PsiDirectory> defaultDirectory = getDefaultDirectory();
        if (defaultDirectory.isPresent() && editor != null) {
            createAndOpenReleaseScript(defaultDirectory.get(), false,
                    editor.getSelectionModel().getSelectedText(true));
        } else if (defaultDirectory.isPresent()) {
            createAndOpenReleaseScript(defaultDirectory.get(), false, null);
        } else {
            Messages.showErrorDialog("Select a default directory in plugin project settings!",
                    "Script Generation Canceled");
        }
    }

    @Override
    public void update(@NotNull final AnActionEvent e) {
        currentProject = e.getProject();
        final Editor editor = e.getData(CommonDataKeys.EDITOR);

        // Set visibility only in case of existing project and editor and if a selection exists
        e.getPresentation().setEnabledAndVisible(currentProject != null
                && editor != null
                && editor.getSelectionModel().hasSelection());
    }
}
