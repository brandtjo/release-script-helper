package com.github.brandtjo.releasescripthelper.settings;

import com.github.brandtjo.releasescripthelper.util.FileUtil;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidatorEx;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.AddEditDeleteListPanel;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ListSpeedSearch;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectLevelComponent {

    private final TextFieldWithBrowseButton defaultDirectoryChooser = new TextFieldWithBrowseButton(new JBTextField());

    private final JPanel mainPanel;
    private final JBRadioButton useCustomScriptNumber = new JBRadioButton("Use custom script number", false);
    private final JBRadioButton useUnixTimeStamp = new JBRadioButton("Use Unix timestamp", true);

    private final MyListPanel ticketTypes = new MyListPanel("Ticket types", "Define the ticket type");
    private final MyListPanel fileEndings = new MyListPanel("File endings", "Define the file ending");

    public ProjectLevelComponent(Project currentProject) {
        if (!currentProject.isDefault() && currentProject.getBasePath() != null) {
            VirtualFile projectFile = LocalFileSystem.getInstance().findFileByPath(currentProject.getBasePath());
            defaultDirectoryChooser.addBrowseFolderListener(
                    new MyTextBrowseFolderListener(FileChooserDescriptorFactory.createSingleFolderDescriptor()
                            .withRoots(projectFile), currentProject));
        } else {
            defaultDirectoryChooser.setEditable(false);
        }

        ButtonGroup group = new ButtonGroup();
        group.add(useCustomScriptNumber);
        group.add(useUnixTimeStamp);
        JPanel radioGroup = new JPanel(new GridBagLayout());
        radioGroup.add(useCustomScriptNumber);
        radioGroup.add(useUnixTimeStamp);

        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("Default directory", defaultDirectoryChooser, 30)
                .addSeparator(20)
                .addComponent(new JBLabel("Default prefix"), 10)
                .addComponent(useCustomScriptNumber, 8)
                .addComponent(useUnixTimeStamp, 5)
                .addSeparator(20)
                .addComponent(ticketTypes, 10)
                .addComponent(fileEndings, 10)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return useUnixTimeStamp;
    }

    public String getDefaultDirectory() {
        return defaultDirectoryChooser.getText();
    }

    public void setDefaultDirectory(String text) {
        defaultDirectoryChooser.setText(text);
    }

    public boolean isUseCustomScriptNumber() {
        return useCustomScriptNumber.isSelected();
    }

    public void setUseCustomScriptNumber(boolean selected) {
        useCustomScriptNumber.setSelected(selected);
    }

    public boolean isUseUnixTimestamp() {
        return useUnixTimeStamp.isSelected();
    }

    public void setUseUnixTimeStamp(boolean selected) {
        useUnixTimeStamp.setSelected(selected);
    }

    public List<String> getTicketTypes() {
        List<String> types = new ArrayList<>();
        ticketTypes.applyTo(types);
        return types;
    }

    public void setTicketTypes(List<String> values) {
        ticketTypes.resetFrom(values);
    }

    public List<String> getFileEndings() {
        List<String> endings = new ArrayList<>();
        fileEndings.applyTo(endings);
        return endings;
    }

    public void setFileEndings(List<String> values) {
        fileEndings.resetFrom(values);
    }

    /**
     * Copied and adjusted from {@link com.intellij.execution.console.ConsoleConfigurable}.
     */
    private static class MyListPanel extends AddEditDeleteListPanel<String> {

        private final @NlsContexts.DialogMessage String myQuery;

        @Nls(capitalization = Nls.Capitalization.Title)
        public MyListPanel(String title, String query) {
            super(title, new ArrayList<>());
            myQuery = query;
            new ListSpeedSearch<>(myList);
        }

        @Override
        protected Border createTitledBorder(String title) {
            return IdeBorderFactory.createTitledBorder(title, false, JBUI.insetsTop(8)).setShowLine(false);
        }

        @Override
        protected @Nullable String findItemToAdd() {
            return showEditDialog("");
        }

        private @Nullable String showEditDialog(final String initialValue) {
            return Messages.showInputDialog(this, myQuery, "Entry", Messages.getQuestionIcon(), initialValue,
                    new InputValidatorEx() {
                        @Override
                        public boolean checkInput(String inputString) {
                            return StringUtils.isNotBlank(inputString)
                                    && inputString.matches("\\w+");
                        }

                        @Override
                        public boolean canClose(String inputString) {
                            return checkInput(inputString);
                        }

                        @Override
                        public @NlsContexts.DetailedDescription @Nullable String getErrorText(String inputString) {
                            if (!checkInput(inputString)) {
                                return "Input mustn't be empty and must only consist of word characters: [A-Za-z0-9_]";
                            }
                            return null;
                        }
                    });
        }

        void resetFrom(List<String> patterns) {
            myListModel.clear();
            patterns.forEach(myListModel::addElement);
        }

        void applyTo(List<? super String> patterns) {
            patterns.clear();
            for (Object o : getListItems()) {
                patterns.add((String) o);
            }
        }

        @Override
        protected String editSelectedItem(String item) {
            return showEditDialog(item);
        }
    }

    private static class MyTextBrowseFolderListener extends TextBrowseFolderListener {

        private final Project project;

        public MyTextBrowseFolderListener(@NotNull FileChooserDescriptor fileChooserDescriptor,
                                          @Nullable Project project) {
            super(fileChooserDescriptor, project);
            this.project = project;
        }

        @Override
        @NotNull
        protected String chosenFileToResultingText(@NotNull VirtualFile chosenFile) {
            return FileUtil.toRelativePresentableUrl(project, chosenFile);
        }
    }
}
