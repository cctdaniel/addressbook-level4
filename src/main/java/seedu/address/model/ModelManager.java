package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.model.ListingUnit.LESSON;
import static seedu.address.model.ListingUnit.LOCATION;
import static seedu.address.model.ListingUnit.MODULE;

import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.ComponentManager;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.events.model.AddressBookChangedEvent;
import seedu.address.model.module.Code;
import seedu.address.model.module.Lesson;
import seedu.address.model.module.Location;
import seedu.address.model.module.ReadOnlyLesson;
import seedu.address.model.module.exceptions.DuplicateLessonException;
import seedu.address.model.module.exceptions.LessonNotFoundException;
import seedu.address.model.module.predicates.*;

/**
 * Represents the in-memory model of the address book data.
 * All changes to any model should be synchronized.
 */
public class ModelManager extends ComponentManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final FilteredList<ReadOnlyLesson> filteredLessons;
    private final HashSet<ReadOnlyLesson> favouriteList;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, UserPrefs userPrefs) {
        super();
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.addressBook = new AddressBook(addressBook);
        filteredLessons = new FilteredList<>(this.addressBook.getLessonList());
        filteredLessons.setPredicate(new UniqueModuleCodePredicate(getUniqueCodeSet()));
        favouriteList = new HashSet<ReadOnlyLesson>();
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    @Override
    public HashSet<Location> getUniqueLocationSet() {
        HashSet<Location> set = new HashSet<>();

        ObservableList<ReadOnlyLesson> lessonLst = getFilteredLessonList();
        updateFilteredLessonList(PREDICATE_SHOW_ALL_LESSONS);
        for (ReadOnlyLesson l : lessonLst) {
            if (!set.contains(l.getLocation())) {
                set.add(l.getLocation());
            }
        }
        return set;
    }

    @Override
    public HashSet<Code> getUniqueCodeSet() {
        HashSet<Code> set = new HashSet<>();

        ObservableList<ReadOnlyLesson> lessonLst = getFilteredLessonList();
        updateFilteredLessonList(PREDICATE_SHOW_ALL_LESSONS);
        for (ReadOnlyLesson l : lessonLst) {
            if (!set.contains(l.getCode())) {
                set.add(l.getCode());
            }
        }
        return set;
    }

    @Override
    public FavouriteListPredicate getFavouriteListPredicate() {
        return new FavouriteListPredicate(favouriteList);
    }


    @Override
    public void resetData(ReadOnlyAddressBook newData) {
        addressBook.resetData(newData);
        indicateAddressBookChanged();
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    /** Raises an event to indicate the model has changed */
    private void indicateAddressBookChanged() {
        raise(new AddressBookChangedEvent(addressBook));
    }

    @Override
    public synchronized void deleteLesson(ReadOnlyLesson target) throws LessonNotFoundException {
        addressBook.removeLesson(target);
        indicateAddressBookChanged();
    }

    @Override
    public synchronized void deleteLessonSet(List<ReadOnlyLesson> lessonList) throws LessonNotFoundException {

        for (ReadOnlyLesson lesson : lessonList) {
            addressBook.removeLesson(lesson);
        }
        indicateAddressBookChanged();
    }

    @Override
    public synchronized void addLesson(ReadOnlyLesson lesson) throws DuplicateLessonException {
        addressBook.addLesson(lesson);
        indicateAddressBookChanged();
    }

    @Override
    public void bookmarkLesson(ReadOnlyLesson target) throws DuplicateLessonException {
        if (!favouriteList.contains(target)) {
            favouriteList.add(target);
        } else {
            throw new DuplicateLessonException();
        }
    }

    @Override
    public void updateLesson(ReadOnlyLesson target, ReadOnlyLesson editedLesson)
            throws DuplicateLessonException, LessonNotFoundException {
        requireAllNonNull(target, editedLesson);

        addressBook.updateLesson(target, editedLesson);
        indicateAddressBookChanged();
    }

    //=========== Filtered Module List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code ReadOnlyModule} backed by the internal list of
     * {@code addressBook}
     */
    @Override
    public ObservableList<ReadOnlyLesson> getFilteredLessonList() {
        return FXCollections.unmodifiableObservableList(filteredLessons);
    }

    @Override
    public void updateFilteredLessonList(Predicate<ReadOnlyLesson> predicate) {
        requireNonNull(predicate);
        filteredLessons.setPredicate(predicate);
    }

    @Override
    public void handleListingUnit() {
        ListingUnit unit = ListingUnit.getCurrentListingUnit();

        if (unit.equals(LOCATION)) {
            UniqueLocationPredicate predicate = new UniqueLocationPredicate(getUniqueLocationSet());
            updateFilteredLessonList(predicate);
        } else if (unit.equals(MODULE)) {
            UniqueModuleCodePredicate predicate = new UniqueModuleCodePredicate(getUniqueCodeSet());
            updateFilteredLessonList(predicate);
        } else {
            ListingUnit previousUnit = ListingUnit.getPreviousListingUnit();
            if (!getFilteredLessonList().isEmpty()) {
                ReadOnlyLesson typicalLesson = getFilteredLessonList().get(0);
                if (previousUnit == MODULE) {
                    Code code = typicalLesson.getCode();
                    FixedCodePredicate predicate = new FixedCodePredicate(code);
                    updateFilteredLessonList(predicate);
                } else {
                    Location location = typicalLesson.getLocation();
                    FixedLocationPredicate predicate = new FixedLocationPredicate(location);
                    updateFilteredLessonList(predicate);
                }
            }

        }
    }

    @Override
    public boolean equals(Object obj) {
        // short circuit if same object
        if (obj == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(obj instanceof ModelManager)) {
            return false;
        }

        // state check
        ModelManager other = (ModelManager) obj;
        return addressBook.equals(other.addressBook)
                && filteredLessons.equals(other.filteredLessons);
    }


}
