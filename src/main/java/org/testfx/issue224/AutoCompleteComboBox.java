package org.testfx.issue224;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;

import com.google.common.annotations.VisibleForTesting;

public class AutoCompleteComboBox extends Region
{
    /**
     * The {@code ComboBox<String>} control which will have auto-completion behavior
     * enabled on.
     */
    private final ComboBox<String> comboBox;

    /**
     * The list of pre-populated values to select from to complete.
     */
    private final List<String> values;

    /**
     * The mode of autocompletion (starts with or contains). Note that starts with
     * is a subset of contains.
     */
    private AutoCompleteMode autoCompleteMode;

    /**
     * If true, autocomplete will matche values that have accented characters
     * (such as "á") with just the base letter (in this case, "a").
     */
    private boolean foldAccentMarks = true;

    public enum AutoCompleteMode
    {
        STARTS_WITH,
        CONTAINS
    }

    public AutoCompleteComboBox(final List<String> values)
    {
        this(values, AutoCompleteMode.CONTAINS);
    }

    public AutoCompleteComboBox(final List<String> values, final AutoCompleteMode autoCompleteMode)
    {
        this.values = values;
        this.autoCompleteMode = autoCompleteMode;
        comboBox = new ComboBox<>();
        comboBox.setVisibleRowCount(8);
        comboBox.setEditable(true);
        comboBox.setItems(FXCollections.observableArrayList(values));
        getChildren().setAll(comboBox);

        comboBox.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> comboBox.hide());

        comboBox.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>()
        {
            private boolean moveCaretToPos = false;
            private int caretPos;

            @Override
            public void handle(KeyEvent event)
            {
                if (event.getCode() == KeyCode.UP)
                {
                    caretPos = -1;
                    moveCaret(comboBox.getEditor().getText().length());
                    return;
                }
                else if (event.getCode() == KeyCode.DOWN)
                {
                    if (!comboBox.isShowing())
                    {
                        comboBox.show();
                    }
                    caretPos = -1;
                    moveCaret(comboBox.getEditor().getText().length());
                    return;
                }
                else if (event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.DELETE)
                {
                    moveCaretToPos = true;
                    caretPos = comboBox.getEditor().getCaretPosition();
                    if (comboBox.getSelectionModel().getSelectedItem() != null)
                    {
                        // the user has previously selected an item, but now they are changing it
                        // so un-select whatever is selected and set text to mimic key behavior
                        String newText = "";
                        if (event.getCode() == KeyCode.BACK_SPACE)
                        {
                            newText = comboBox.getEditor().getText().substring(0, comboBox.getEditor().getText().length() - 1);
                        }

                        comboBox.getSelectionModel().select(-1);
                        comboBox.getEditor().setText(newText);
                    }
                }

                if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.LEFT
                        || event.isControlDown() || event.getCode() == KeyCode.HOME
                        || event.getCode() == KeyCode.END || event.getCode() == KeyCode.TAB)
                {
                    return;
                }

                ObservableList<String> list = FXCollections.observableArrayList();

                for (String item : values)
                {
                    if (getAutoCompleteMode() == AutoCompleteMode.CONTAINS)
                    {
                        if (item.toLowerCase().contains(comboBox.getEditor().getText().toLowerCase()))
                        {
                            list.add(item);
                        }
                    }
                    else if (getAutoCompleteMode() == AutoCompleteMode.STARTS_WITH)
                    {
                        if (item.toLowerCase().startsWith(comboBox.getEditor().getText().toLowerCase()))
                        {
                            list.add(item);
                        }
                    }
                }

                String text = comboBox.getEditor().getText();

                comboBox.hide();
                comboBox.setVisibleRowCount(Math.min(list.size(), 8));
                comboBox.setItems(list);
                if (!moveCaretToPos)
                {
                    caretPos = -1;
                }

                moveCaret(text.length());

                // show combobox as long as:
                // * there is at least one match
                // * the textfield as at least one character
                // * the user has not selected an item
                if (!list.isEmpty() && !text.isEmpty() && comboBox.getSelectionModel().getSelectedItem() == null)
                {
                    comboBox.show();
                }
            }

            private void moveCaret(int textLength)
            {
                if (caretPos == -1)
                {
                    comboBox.getEditor().positionCaret(textLength);
                }
                else
                {
                    comboBox.getEditor().positionCaret(caretPos);
                }
                moveCaretToPos = false;
            }
        });
    }

    public void setAutoCompleteMode(AutoCompleteMode autoCompleteMode)
    {
        this.autoCompleteMode = autoCompleteMode;
    }

    public AutoCompleteMode getAutoCompleteMode()
    {
        return autoCompleteMode;
    }

    public String getSelection()
    {
        return comboBox.getSelectionModel().getSelectedItem();
    }

    @VisibleForTesting
    ComboBox<String> getComboBox()
    {
        return comboBox;
    }
}