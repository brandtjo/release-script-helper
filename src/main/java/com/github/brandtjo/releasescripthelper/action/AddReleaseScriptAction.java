package com.github.brandtjo.releasescripthelper.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AddReleaseScriptAction extends AnAction {

    private Project currentProject;

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        currentProject = event.getProject();

        Object navigatable = event.getData(CommonDataKeys.NAVIGATABLE);
        if(navigatable instanceof PsiDirectory) {
            PsiDirectory directory = (PsiDirectory) navigatable;
            try {
                createReleaseScript(directory);
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

    private void createReleaseScript(final PsiDirectory directory) {
        final Date now = new Date();
        final ReleaseScriptOptions options = promptUserOptions();
        ApplicationManager.getApplication().runWriteAction(() -> {
            PsiFile releaseScript = directory.createFile(getReleaseScriptName(now, options));
            releaseScript.getVirtualFile().setCharset(StandardCharsets.UTF_8);
            try {
                releaseScript.getVirtualFile().setBinaryContent(getReleaseScriptContent(now, options));
            } catch (IOException e) {
                showErrorDialog(e);
            }
            new OpenFileDescriptor(currentProject, releaseScript.getVirtualFile(), 3, 0).navigate(true);
        });
    }

    private void showErrorDialog(Exception e) {
        Messages.showErrorDialog(currentProject, e.getMessage(), "Error While Creating Release Script");
    }

    private ReleaseScriptOptions promptUserOptions() {
        ReleaseScriptOptionsDialog dialog = new ReleaseScriptOptionsDialog();
        if(dialog.showAndGet()) {
            return dialog.getOptions();
        } else {
            throw new IllegalArgumentException("Script creation canceled");
        }
    }

    private ReleaseScriptOptions getDefaultOptions() {
        ReleaseScriptOptions defaultOptions = new ReleaseScriptOptions();
        defaultOptions.setUseTicket(false);
        defaultOptions.setDescription("release-script");
        defaultOptions.setFileEnding("sql");
        return defaultOptions;
    }

    private String getReleaseScriptName(Date date, ReleaseScriptOptions options) {
        String prefix = Long.toString(date.getTime());
        String ticket = parseTicket(options);
        String description = parseDescription(options);
        String suffix = parseSuffix(options);
        return Stream.of(prefix, ticket, description)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("_")) + '.' + suffix;
    }

    private String parseTicket(ReleaseScriptOptions options) {
        Optional<Boolean> useTicket = Optional.ofNullable(options).map(ReleaseScriptOptions::getUseTicket);
        if(useTicket.isPresent()
                && useTicket.get()
                && StringUtils.isNotBlank(options.getTicketType())
                && StringUtils.isNotBlank(options.getTicketNumber())) {

            return options.getTicketType() + StringUtils.strip(options.getTicketNumber());
        }
        return null;
    }

    private String parseDescription(ReleaseScriptOptions options) {
        Optional<String> rawDescription = Optional.ofNullable(options).map(ReleaseScriptOptions::getDescription);
        if(rawDescription.isPresent() && StringUtils.isNotBlank(rawDescription.get())) {
            String description = rawDescription.get();
            description = sanitizeFileNamePart(description, " ");
            description = StringUtils.strip(description);
            description = description.replaceAll("\\s+", "-");
            return description.toLowerCase();
        }
        return getDefaultOptions().getDescription();
    }

    private String parseSuffix(ReleaseScriptOptions options) {
        String defaultSuffix = getDefaultOptions().getFileEnding();
        Optional<String> suffix = Optional.ofNullable(options).map(ReleaseScriptOptions::getFileEnding);
        if(suffix.isPresent() && StringUtils.isNotBlank(suffix.get())) {
            return StringUtils.defaultIfBlank(sanitizeFileNamePart(suffix.get(), "").toLowerCase(), defaultSuffix);
        }
        return defaultSuffix;
    }

    private String sanitizeFileNamePart(String fileNamePart, String replacement) {
        if(StringUtils.isNotBlank(fileNamePart)) {
            return fileNamePart.replaceAll("[\\Q<>:\"/\\|?*.,´`+~#-_!§$%&()[]{}^°@€\\E]+", replacement);
        }
        throw new IllegalArgumentException("Invalid File Name Part");
    }

    private byte[] getReleaseScriptContent(Date date, ReleaseScriptOptions options) {

        String line1 = "# description: " + Optional.ofNullable(options)
                .map(ReleaseScriptOptions::getDescription)
                .orElse("none") + "\n";

        String dateTime = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss z", Locale.ENGLISH).format(date);
        String line2 = "#        date: " + dateTime + "\n";

        return (line1 + line2).getBytes(StandardCharsets.UTF_8);
    }
}
