package com.github.brandtjo.releasescripthelper.action;

import com.github.brandtjo.releasescripthelper.model.Options;
import com.github.brandtjo.releasescripthelper.model.ReleaseScript;
import com.github.brandtjo.releasescripthelper.settings.ProjectLevelState;
import com.github.brandtjo.releasescripthelper.ui.BasicAddDialog;
import com.github.brandtjo.releasescripthelper.util.FileUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public abstract class BasicAddScript extends AnAction {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected Project currentProject;
	protected AnActionEvent currentEvent;

	@Override
	public void update(AnActionEvent e) {
		// Set the availability based on whether a project is open
		currentProject = e.getProject();
		currentEvent = e;
		e.getPresentation().setEnabledAndVisible(currentProject != null);
	}

	protected Optional<PsiDirectory> getDefaultDirectory() {
		return Optional.ofNullable(ProjectLevelState.getInstanceFor(currentProject))
				.map(it -> it.options)
				.map(Options::getDefaultDirectory)
				.filter(StringUtils::isNotBlank)
				.map(filePath -> FileUtil.fromRelativePresentableUrl(currentProject, filePath))
				.filter(VirtualFile::isDirectory)
				.map(virtualFile -> PsiDirectoryFactory.getInstance(currentProject).createDirectory(virtualFile));
	}

	protected void createAndOpenReleaseScript(final PsiDirectory directory, boolean selectedByUser, String content) {
		try {
			final ReleaseScript releaseScript = promptValuesForReleaseScript();
			releaseScript.setContent(content);
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
				new OpenFileDescriptor(currentProject, releaseScriptFile.getVirtualFile(), 3, 0)
						.navigate(true);
			});
		} catch (ScriptCreationCanceledException e) {
			logger.info("Script creation canceled");
		} catch (IllegalArgumentException e) {
			showErrorDialog(e);
		}
	}

	protected void showErrorDialog(Exception e) {
		Messages.showErrorDialog(currentProject, e.getMessage(), "Error While Creating Release Script");
	}

	private ReleaseScript promptValuesForReleaseScript() {
		ReleaseScript model = new ReleaseScript();
		updateOptions(model);
		presetFromVcsBranch(model);
		if (new BasicAddDialog(model, currentProject).showAndGet()) {
			return model;
		} else {
			throw new ScriptCreationCanceledException("Script creation canceled");
		}
	}

	private void presetFromVcsBranch(ReleaseScript model) {
		List<GitRepository> repositories = GitUtil.getRepositoryManager(currentProject).getRepositories();
		if (CollectionUtils.isEmpty(repositories))
			return;
		String currentBranchName = repositories.get(0).getCurrentBranchName();
		if (StringUtils.isBlank(currentBranchName))
			return;
		String[] currentBranch = currentBranchName.split("/");
		String[] nameParts = currentBranch[currentBranch.length - 1].split("[-_]");
		String ticketType = model.getOptions().getTicketTypes().stream()
				.filter(type -> StringUtils.startsWithIgnoreCase(nameParts[0], type))
				.findFirst()
				.orElse(null);
		if (StringUtils.isBlank(ticketType))
			return;
		String ticketNumber = StringUtils.isNotBlank(StringUtils.removeStartIgnoreCase(nameParts[0], ticketType))
				? StringUtils.removeStartIgnoreCase(nameParts[0], ticketType)
				: nameParts.length > 1
				    ? nameParts[1]
				    : StringUtils.EMPTY;

        model.setTicketType(ticketType);
        model.setTicketNumber(ticketNumber);
	}

	private void updateOptions(ReleaseScript model) {
		model.setOptions(ProjectLevelState.getInstanceFor(currentProject).options);
	}
}
