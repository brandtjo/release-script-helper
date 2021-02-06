package com.github.brandtjo.releasescripthelper.action;

import com.github.brandtjo.releasescripthelper.model.ReleaseScript;
import com.github.brandtjo.releasescripthelper.ui.BasicAddDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ProjectExplorerAddScript extends AnAction {

    private Project currentProject;

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        currentProject = event.getProject();

        Object navigatable = event.getData(CommonDataKeys.NAVIGATABLE);
        if(navigatable instanceof PsiDirectory) {
            PsiDirectory directory = (PsiDirectory) navigatable;
            try {
                createAndOpenReleaseScript(directory);
            } catch (IllegalArgumentException e) {
                showErrorDialog(e);
            }
        } else {
            Messages.showInfoMessage(currentProject, "Select a directory in which to create a release script.", "Could Not Create Release Script");
        }
    }

    @Override
    public void update(AnActionEvent e) {
        // Set the availability based on whether a project is open
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }

    private void createAndOpenReleaseScript(final PsiDirectory directory) {
        final ReleaseScript releaseScript = promptValuesForReleaseScript();
        ApplicationManager.getApplication().runWriteAction(() -> {
            PsiFile releaseScriptFile = directory.createFile(releaseScript.getReleaseScriptName());
            releaseScriptFile.getVirtualFile().setCharset(StandardCharsets.UTF_8);
            try {
                releaseScriptFile.getVirtualFile().setBinaryContent(releaseScript.getReleaseScriptContent());
            } catch (IOException e) {
                showErrorDialog(e);
            }
            new OpenFileDescriptor(currentProject, releaseScriptFile.getVirtualFile(), 3, 0).navigate(true);
        });
    }

    private void showErrorDialog(Exception e) {
        Messages.showErrorDialog(currentProject, e.getMessage(), "Error While Creating Release Script");
    }

    private ReleaseScript promptValuesForReleaseScript() {
        ReleaseScript model = new ReleaseScript();
        if(new BasicAddDialog(model).showAndGet()) {
            return model;
        } else {
            throw new IllegalArgumentException("Script creation canceled");
        }
    }
}
