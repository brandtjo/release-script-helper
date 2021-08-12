package com.github.brandtjo.releasescripthelper.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class FileUtil {

    private FileUtil() {
    }

    @Nullable
    public static VirtualFile fromRelativePresentableUrl(@NotNull Project project,
                                                         @NotNull String relativePresentableUrl) {
        if (!project.isDefault() && project.getBasePath() != null) {
            VirtualFile projectFile = LocalFileSystem.getInstance().findFileByPath(project.getBasePath());
            if (projectFile != null && !relativePresentableUrl.startsWith(projectFile.getPresentableUrl())) {
                String projectPresentableUrl = projectFile.getPresentableUrl() + File.separatorChar;
                return LocalFileSystem.getInstance().findFileByPath(projectPresentableUrl + relativePresentableUrl);
            }
        }
        return LocalFileSystem.getInstance().findFileByPath(relativePresentableUrl);
    }

    @NotNull
    public static String toRelativePresentableUrl(@NotNull Project project, @NotNull VirtualFile projectChildFile) {
        if (!project.isDefault() && project.getBasePath() != null) {
            VirtualFile projectFile = LocalFileSystem.getInstance().findFileByPath(project.getBasePath());
            if (projectFile != null
                    && projectChildFile.getPresentableUrl().startsWith(projectFile.getPresentableUrl())) {

                return projectChildFile.getPresentableUrl().substring((projectFile.getPresentableUrl()
                        + File.separatorChar).length());
            }
        }
        return projectChildFile.getPresentableUrl();
    }
}
