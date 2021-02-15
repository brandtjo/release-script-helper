package com.github.brandtjo.releasescripthelper.action;

import com.github.brandtjo.releasescripthelper.model.Options;
import com.github.brandtjo.releasescripthelper.model.ReleaseScript;
import com.github.brandtjo.releasescripthelper.settings.ProjectLevelState;
import com.github.brandtjo.releasescripthelper.ui.BasicAddDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class ProjectExplorerAddScript extends AnAction {

    private Project currentProject;

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        currentProject = event.getProject();

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
            Messages.showInfoMessage(currentProject, "Select a default directory in settings or select one to create a release script in.", "Could Not Create Release Script");
            return;
        }

        try {
            createAndOpenReleaseScript(directory, selectedbyUser);
        } catch (IllegalArgumentException e) {
            showErrorDialog(e);
        }
    }

    @Override
    public void update(AnActionEvent e) {
        // Set the availability based on whether a project is open
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }

    private Optional<PsiDirectory> getDefaultDirectory() {
        return Optional.ofNullable(ProjectLevelState.getInstanceFor(currentProject))
                .map(it -> it.options)
                .map(Options::getDefaultDirectory)
                .filter(StringUtils::isNotBlank)
                .map(defaultDirectory -> LocalFileSystem.getInstance().findFileByPath(defaultDirectory))
                .filter(VirtualFile::isDirectory)
                .map(virtualFile -> PsiDirectoryFactory.getInstance(currentProject).createDirectory(virtualFile));
    }

    private void createAndOpenReleaseScript(final PsiDirectory directory, boolean selectedByUser) {
        final ReleaseScript releaseScript = promptValuesForReleaseScript();
        ApplicationManager.getApplication().runWriteAction(() -> {
            PsiFile releaseScriptFile = getDefaultDirectory()
                    .filter(it -> !selectedByUser)
                    .orElse(directory)
                    .createFile(releaseScript.getReleaseScriptName());
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
        updateOptions(model);
        if (new BasicAddDialog(model, currentProject).showAndGet()) {
            return model;
        } else {
            throw new IllegalArgumentException("Script creation canceled");
        }
    }

    private void updateOptions(ReleaseScript model) {
        model.setOptions(ProjectLevelState.getInstanceFor(currentProject).options);
    }
}
