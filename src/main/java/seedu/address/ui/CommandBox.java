package seedu.address.ui;

import static seedu.address.logic.commands.CustomiseCommand.FONT_SIZE_LARGE;
import static seedu.address.logic.commands.CustomiseCommand.FONT_SIZE_NORMAL;
import static seedu.address.logic.commands.CustomiseCommand.FONT_SIZE_SMALL;
import static seedu.address.logic.commands.CustomiseCommand.FONT_SIZE_XLARGE;
import static seedu.address.logic.commands.CustomiseCommand.FONT_SIZE_XSMALL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.controlsfx.control.textfield.TextFields;

import com.google.common.eventbus.Subscribe;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.events.ui.ChangeFontSizeEvent;
import seedu.address.commons.events.ui.ColorKeywordEvent;
import seedu.address.commons.events.ui.NewResultAvailableEvent;
import seedu.address.logic.ListElementPointer;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.CliSyntax;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * The UI component that is responsible for receiving user command inputs.
 */
public class CommandBox extends UiPart<Region> {

    public static final String ERROR_STYLE_CLASS = "error";
    private static final String FXML = "CommandBox.fxml";
    private static final String TAG_PREFIX = "prefix";

    private static final int NAME = 0;
    private static final int EMAIL = 1;
    private static final int PHONE = 2;
    private static final int ADDRESS = 3;
    private static final int TAG = 4;
    private static final int FONT_SIZE = 5;

    private final Logger logger = LogsCenter.getLogger(CommandBox.class);
    private final Logic logic;
    private ListElementPointer historySnapshot;

    private final AddressBookParser tester;

    private HashMap<String, String> keywordColorMap;
    private ArrayList<String> prefixList;
    private int fontIndex = 0;
    private boolean enableHighlight = false;
    private String userPrefFontSize;

    private final ImageView tick = new ImageView("/images/tick.png");
    private final ImageView cross = new ImageView("/images/cross.png");


    @FXML
    private TextField commandTextField;

    @FXML
    private Text commandTextDefault;

    @FXML
    private Text commandTextXsmall;

    @FXML
    private Text commandTextSmall;

    @FXML
    private Text commandTextLarge;

    @FXML
    private Text commandTextXLarge;

    @FXML
    private StackPane stackPane;

    @FXML
    private Label keywordLabel;

    @FXML
    private Label checkbox;

    public CommandBox(Logic logic) {
        super(FXML);
        this.logic = logic;
        // calls #setStyleToDefault() whenever there is a change to the text of the command box.
        commandTextField.textProperty().addListener((unused1, unused2, unused3) -> setStyleToDefault());
        configInactiveKeyword();
        configPrefixList();
        keywordLabel.getStyleClass().add("keyword-label-default");
        keywordColorMap = logic.getCommandKeywordColorMap();
        String[] commands = {"help", "add", "list", "edit", "find",
            "delete", "select", "history", "undo", "redo", "clear", "exit", "customise", "view"};
        TextFields.bindAutoCompletion(commandTextField, commands);
        tick.setFitHeight(30);
        tick.setFitWidth(30);
        cross.setFitHeight(30);
        cross.setFitWidth(30);
        historySnapshot = logic.getHistorySnapshot();
        tester = new AddressBookParser();
        registerAsAnEventHandler(this);
    }

    /**
     * This method create a list of prefix used in the command
     */
    private void configPrefixList() {
        prefixList = new ArrayList<>();
        prefixList.add(CliSyntax.PREFIX_NAME.getPrefix());
        prefixList.add(CliSyntax.PREFIX_EMAIL.getPrefix());
        prefixList.add(CliSyntax.PREFIX_PHONE.getPrefix());
        prefixList.add(CliSyntax.PREFIX_ADDRESS.getPrefix());
        prefixList.add(CliSyntax.PREFIX_TAG.getPrefix());
        prefixList.add(CliSyntax.PREFIX_FONT_SIZE.getPrefix());
    }

    /**
     * Handles the key press event, {@code keyEvent}.
     */
    @FXML
    private void handleKeyPress(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {

        case UP:
            // As up and down buttons will alter the position of the caret,
            // consuming it causes the caret's position to remain unchanged
            keyEvent.consume();

            navigateToPreviousInput();
            break;
        case DOWN:
            keyEvent.consume();
            navigateToNextInput();
            break;
        default:
                // let JavaFx handle the keypress
        }
    }

    /**
     * Handles the key released event, {@code keyEvent}.
     */
    @FXML
    private void handleKeyReleased(KeyEvent keyEvent) {
        listenCommandInputChanged();
    }

    /**
     * Handles the Command input changed event.
     */
    private void listenCommandInputChanged() {
        if (enableHighlight) {
            String allTextInput = commandTextField.getText();
            String[] inputArray = allTextInput.split(" ");
            int index = 0;

            configInActiveTag();
            configInactiveKeyword();
            configBorderColor(allTextInput);


            for (int i = 0; i < inputArray.length; i++) {
                String text = inputArray[i];

                //Command Keyword
                if (i == 0 && validCommandKeyword(text)) {
                    configActiveKeyword(text);
                }

                //Name
                if (text.contains(prefixList.get(NAME))) {
                    index = allTextInput.indexOf(prefixList.get(NAME));
                    configActiveTag(index, prefixList.get(NAME));
                }

                //Email
                if (text.contains(prefixList.get(EMAIL))) {
                    index = allTextInput.indexOf(prefixList.get(EMAIL));
                    configActiveTag(index, prefixList.get(EMAIL));
                }

                //Phone
                if (text.contains(prefixList.get(PHONE))) {
                    index = allTextInput.indexOf(prefixList.get(PHONE));
                    configActiveTag(index, prefixList.get(PHONE));
                }

                //Address
                if (text.contains(prefixList.get(ADDRESS))) {
                    index = allTextInput.indexOf(prefixList.get(ADDRESS));
                    configActiveTag(index, prefixList.get(ADDRESS));
                }

                //Tag
                if (text.contains(prefixList.get(TAG))) {
                    ArrayList<Integer> tagList = getTagIndexList(allTextInput);
                    for (int j = 0; j < tagList.size(); j++) {
                        index = tagList.get(j);
                        configActiveTag(index, index + prefixList.get(TAG));
                    }
                }

                //font size
                if (text.contains(prefixList.get(FONT_SIZE))) {
                    index = allTextInput.indexOf(prefixList.get(FONT_SIZE));
                    configActiveTag(index, prefixList.get(FONT_SIZE));
                }
            }
        }

    }

    private ArrayList<Integer> getTagIndexList(String allTextInput) {
        ArrayList<Integer> tagList = new ArrayList<>();
        int index = 0;
        while (index < allTextInput.length()) {
            int newIndex = allTextInput.indexOf(prefixList.get(TAG), index);
            if (newIndex == -1) {
                break;
            }
            tagList.add(newIndex);
            index = newIndex + 1;
        }
        return tagList;
    }


    /**
     * Check if keyword is a valid command keyword
     * @param keyWord
     * @return
     */
    private boolean validCommandKeyword(String keyWord) {
        if (keywordColorMap.containsKey(keyWord)) {
            return true;
        }
        return false;
    }


    /**
     * Configure words that are not command keyword
     */
    private void configInactiveKeyword() {
        keywordLabel.setVisible(false);
        keywordLabel.toBack();
        commandTextField.toFront();
    }

    /**
     * Configure border colour to indicate validity of user input.
     */
    private void configBorderColor(String allTextInput) {
        try {
            tester.parseCommand(allTextInput);
            commandTextField.setStyle(userPrefFontSize + "-fx-border-color: green; -fx-border-width: 2");
            checkbox.setGraphic(tick);

        } catch (ParseException e) {
            commandTextField.setStyle(userPrefFontSize + "-fx-border-color: red; -fx-border-width: 2");
            checkbox.setGraphic(cross);
        }
    }



    /**
     * Configure command keyword when appeared on Command Box
     * @param commandKeyword
     */
    private void configActiveKeyword(String commandKeyword) {
        keywordLabel.setId("keywordLabel");
        keywordLabel.setText(commandKeyword);
        keywordLabel.setVisible(true);

        keywordLabel.getStyleClass().clear();
        Insets leftInset = new Insets(0, 0, 0, 13);

        switch (fontIndex) {
        case 1:
            keywordLabel.getStyleClass().add("keyword-label-xsmall");
            leftInset = new Insets(0, 0, 0, 9);
            break;
        case 2:
            keywordLabel.getStyleClass().add("keyword-label-small");
            leftInset = new Insets(0, 0, 0, 10);
            break;
        case 3:
            keywordLabel.getStyleClass().add("keyword-label-default");
            leftInset = new Insets(0, 0, 0, 14);
            break;
        case 4:
            keywordLabel.getStyleClass().add("keyword-label-large");
            leftInset = new Insets(0, 0, 0, 10);
            break;
        case 5:
            keywordLabel.getStyleClass().add("keyword-label-xlarge");
            leftInset = new Insets(0, 0, 0, 9);
            break;
        default:
            keywordLabel.getStyleClass().add("keyword-label-default");
        }

        stackPane.setAlignment(keywordLabel, Pos.CENTER_LEFT);
        stackPane.setMargin(keywordLabel, leftInset);

        String color = keywordColorMap.get(commandKeyword);
        keywordLabel.setStyle("-fx-background-color: " + color + ";\n"
                + "-fx-text-fill: red;");
        keywordLabel.toFront();
    }

    /**
     * Configure tag that appear in the text field
     */
    private void configActiveTag(int index, String tag) {
        String allTextInput = commandTextField.getText();
        String inputText = allTextInput.substring(0, index);

        String tagName = tag.replaceAll("[0-9]", "");
        Label tagLabel = new Label(tagName);
        tagLabel.setId(TAG_PREFIX + tag);

        tagLabel.getStyleClass().clear();
        double margin = computeMargin(0, inputText);
        Insets leftInset = new Insets(0, 0, 0, margin + 13);

        switch (fontIndex) {
        case 1:
            tagLabel.getStyleClass().add("keyword-label-xsmall");
            margin = computeMargin(1, inputText);
            leftInset = new Insets(0, 0, 0, margin + 9);
            break;
        case 2:
            tagLabel.getStyleClass().add("keyword-label-small");
            margin = computeMargin(2, inputText);
            leftInset = new Insets(0, 0, 0, margin + 10);
            break;
        case 3:
            tagLabel.getStyleClass().add("keyword-label-default");
            margin = computeMargin(3, inputText);
            leftInset = new Insets(0, 0, 0, margin + 14);
            break;
        case 4:
            tagLabel.getStyleClass().add("keyword-label-large");
            margin = computeMargin(4, inputText);
            leftInset = new Insets(0, 0, 0, margin + 10);
            break;
        case 5:
            tagLabel.getStyleClass().add("keyword-label-xlarge");
            margin = computeMargin(5, inputText);
            leftInset = new Insets(0, 0, 0, margin + 9);
            break;
        default:
            tagLabel.getStyleClass().add("keyword-label-default");
        }

        stackPane.getChildren().add(tagLabel);
        stackPane.setAlignment(tagLabel, Pos.CENTER_LEFT);
        stackPane.setMargin(tagLabel, leftInset);

        tagLabel.setStyle("-fx-background-color:yellow;\n "
                + "-fx-text-fill: red; ");
        tagLabel.setVisible(true);
        tagLabel.toFront();
    }

    @Subscribe
    private void handleChangeFontSizeEvent(ChangeFontSizeEvent event) {
        setFontSize(event.message);
    }


    @Subscribe
    private void handleColorKeywordEvent(ColorKeywordEvent event) {
        setEnableHighlight(event.isEnabled);
    }

    /**
     * This method only remove all tag label in stack pane
     */
    private void configInActiveTag() {
        ObservableList<Node> list = stackPane.getChildren();
        final List<Node> removalCandidates = new ArrayList<>();

        Iterator<Node> iter = list.iterator();
        while (iter.hasNext()) {
            Node node = iter.next();
            if (node.getId().contains(TAG_PREFIX)) {
                node.setVisible(false);
                removalCandidates.add(node);
            }
        }
        stackPane.getChildren().removeAll(removalCandidates);
    }

    /**
     * This method compute the margin for label
     * @param index the type of font size used in command box
     * @param str the text used to compute the width
     * @return
     */
    private double computeMargin(int index, String str) {
        Text text = new Text(str);
        text.getStyleClass().clear();
        switch (index) {
        case 1:
            text.setFont(commandTextXsmall.getFont());
            break;
        case 2:
            text.setFont(commandTextSmall.getFont());
            break;
        case 3:
            text.setFont(commandTextDefault.getFont());
            break;
        case 4:
            text.setFont(commandTextLarge.getFont());
            break;
        case 5:
            text.setFont(commandTextXLarge.getFont());
            break;
        default:
            text.setFont(commandTextDefault.getFont());

        }

        return text.getBoundsInLocal().getWidth();
    }


    /**
     * Updates the text field with the previous input in {@code historySnapshot},
     * if there exists a previous input in {@code historySnapshot}
     */
    private void navigateToPreviousInput() {
        assert historySnapshot != null;
        if (!historySnapshot.hasPrevious()) {
            return;
        }

        replaceText(historySnapshot.previous());
    }

    /**
     * Updates the text field with the next input in {@code historySnapshot},
     * if there exists a next input in {@code historySnapshot}
     */
    private void navigateToNextInput() {
        assert historySnapshot != null;
        if (!historySnapshot.hasNext()) {
            return;
        }

        replaceText(historySnapshot.next());
    }

    /**
     * Sets {@code CommandBox}'s text field with {@code text} and
     * positions the caret to the end of the {@code text}.
     */
    private void replaceText(String text) {
        commandTextField.setText(text);
        commandTextField.positionCaret(commandTextField.getText().length());
    }

    /**
     * Handles the Enter button pressed event.
     */
    @FXML
    private void handleCommandInputChanged() {
        try {
            CommandResult commandResult = logic.execute(commandTextField.getText());
            initHistory();
            historySnapshot.next();
            // process result of the command
            commandTextField.setText("");
            configInactiveKeyword();
            logger.info("Result: " + commandResult.feedbackToUser);
            raise(new NewResultAvailableEvent(commandResult.feedbackToUser));

        } catch (CommandException | ParseException e) {
            initHistory();
            // handle command failure
            setStyleToIndicateCommandFailure();
            logger.info("Invalid command: " + commandTextField.getText());
            raise(new NewResultAvailableEvent(e.getMessage()));
        }
    }

    /**
     * Sets the command box style to user preferred font size.
     */
    private void setFontSize(String userPref) {
        switch (userPref) {
        case FONT_SIZE_XSMALL:
            commandTextField.setStyle("-fx-font-size: x-small;");
            userPrefFontSize = "-fx-font-size: x-small;";
            fontIndex = 1;
            break;

        case FONT_SIZE_SMALL:
            commandTextField.setStyle("-fx-font-size: small;");
            userPrefFontSize = "-fx-font-size: small;";
            fontIndex = 2;
            break;

        case FONT_SIZE_NORMAL:
            commandTextField.setStyle("-fx-font-size: normal;");
            userPrefFontSize = "-fx-font-size: normal;";
            fontIndex = 3;
            break;

        case FONT_SIZE_LARGE:
            commandTextField.setStyle("-fx-font-size: x-large;");
            userPrefFontSize = "-fx-font-size: x-large;";
            fontIndex = 4;
            break;

        case FONT_SIZE_XLARGE:
            commandTextField.setStyle("-fx-font-size: xx-large;");
            userPrefFontSize = "-fx-font-size: xx-large;";
            fontIndex = 5;
            break;

        default:
            break;
        }
    }

    /**
     * Initializes the history snapshot.
     */
    private void initHistory() {
        historySnapshot = logic.getHistorySnapshot();
        // add an empty string to represent the most-recent end of historySnapshot, to be shown to
        // the user if she tries to navigate past the most-recent end of the historySnapshot.
        historySnapshot.add("");
    }

    /**
     * Sets the command box style to use the default style.
     */
    private void setStyleToDefault() {
        commandTextField.getStyleClass().remove(ERROR_STYLE_CLASS);
    }

    /**
     * Sets the command box style to indicate a failed command.
     */
    private void setStyleToIndicateCommandFailure() {
        ObservableList<String> styleClass = commandTextField.getStyleClass();

        if (styleClass.contains(ERROR_STYLE_CLASS)) {
            return;
        }

        styleClass.add(ERROR_STYLE_CLASS);
    }
    /**
     * Sets the command box to enable highlighting of command keywords
     */
    public void setEnableHighlight(boolean enableHighlight) {
        this.enableHighlight = enableHighlight;
    }
}
