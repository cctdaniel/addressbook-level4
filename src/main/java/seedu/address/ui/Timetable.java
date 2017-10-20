package seedu.address.ui;

import java.net.URL;
import java.util.logging.Logger;

import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import seedu.address.MainApp;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.events.ui.LessonPanelSelectionChangedEvent;
import seedu.address.model.module.ReadOnlyLesson;


/**
 * The timetable panel of the App.
 */
public class Timetable extends UiPart<Region> {

    public static final String DEFAULT_PAGE = "default.html";

    private static final String FXML = "Timetable.fxml";

    private final Logger logger = LogsCenter.getLogger(this.getClass());

    @FXML
    private StackPane stackPane;
    @FXML
    private GridPane timetable;

    public Timetable() {
        super(FXML);
        registerAsAnEventHandler(this);
    }

    @Subscribe
    private void handleLessonPanelSelectionChangedEvent(LessonPanelSelectionChangedEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
    }
}