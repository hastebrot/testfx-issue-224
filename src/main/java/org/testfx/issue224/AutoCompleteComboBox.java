package org.testfx.issue224;

import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;

/**
 * TODO: Expose items observable list so that controls can listen for changes (this
 * would be good for say a ListView which would sync its contents (what it shows)
 * with the items as they are matched/not matched)
 */
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
                    String itemBeforeFolding = item;
                    if (foldAccentMarks)
                    {
                        item = foldAccentMarks(item);
                    }

                    if (getAutoCompleteMode() == AutoCompleteMode.CONTAINS)
                    {
                        if (item.toLowerCase().contains(comboBox.getEditor().getText().toLowerCase()))
                        {
                            list.add(itemBeforeFolding);
                        }
                    }
                    else if (getAutoCompleteMode() == AutoCompleteMode.STARTS_WITH)
                    {
                        if (item.toLowerCase().startsWith(comboBox.getEditor().getText().toLowerCase()))
                        {
                            list.add(itemBeforeFolding);
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

    public boolean isFoldAccentMarks()
    {
        return foldAccentMarks;
    }

    public void setFoldAccentMarks(boolean foldAccentMarks)
    {
        this.foldAccentMarks = foldAccentMarks;
    }

    @VisibleForTesting
    ComboBox<String> getComboBox()
    {
        return comboBox;
    }

    private static String foldAccentMarks(final String accentString)
    {
        StringBuilder stringBuilder = new StringBuilder();

        for (char c : accentString.toCharArray())
        {
            boolean matchedAccent = false;
            for (Map.Entry<Character, Character> entry : accentMap.entrySet())
            {
                if (entry.getKey().equals(c))
                {
                    if (Character.isUpperCase(c))
                    {
                        stringBuilder.append(Character.toUpperCase(accentMap.get(c)));
                    }
                    else
                    {
                        stringBuilder.append(accentMap.get(c));
                    }
                    matchedAccent = true;
                    break;
                }
            }

            if (!matchedAccent)
            {
                stringBuilder.append(c);
            }
        }

        return stringBuilder.toString();
    }

    public static final Map<Character, Character> accentMap;

    static
    {
        accentMap = ImmutableMap.<Character, Character>builder()
            .put('ẚ', 'a')
            .put('Á', 'a')
            .put('á', 'a')
            .put('À', 'a')
            .put('à', 'a')
            .put('Ă', 'a')
            .put('ă', 'a')
            .put('Ắ', 'a')
            .put('ắ', 'a')
            .put('Ằ', 'a')
            .put('ằ', 'a')
            .put('Ẵ', 'a')
            .put('ẵ', 'a')
            .put('Ẳ', 'a')
            .put('ẳ', 'a')
            .put('Â', 'a')
            .put('â', 'a')
            .put('Ấ', 'a')
            .put('ấ', 'a')
            .put('Ầ', 'a')
            .put('ầ', 'a')
            .put('Ẫ', 'a')
            .put('ẫ', 'a')
            .put('Ẩ', 'a')
            .put('ẩ', 'a')
            .put('Ǎ', 'a')
            .put('ǎ', 'a')
            .put('Å', 'a')
            .put('å', 'a')
            .put('Ǻ', 'a')
            .put('ǻ', 'a')
            .put('Ä', 'a')
            .put('ä', 'a')
            .put('Ǟ', 'a')
            .put('ǟ', 'a')
            .put('Ã', 'a')
            .put('ã', 'a')
            .put('Ȧ', 'a')
            .put('ȧ', 'a')
            .put('Ǡ', 'a')
            .put('ǡ', 'a')
            .put('Ą', 'a')
            .put('ą', 'a')
            .put('Ā', 'a')
            .put('ā', 'a')
            .put('Ả', 'a')
            .put('ả', 'a')
            .put('Ȁ', 'a')
            .put('ȁ', 'a')
            .put('Ȃ', 'a')
            .put('ȃ', 'a')
            .put('Ạ', 'a')
            .put('ạ', 'a')
            .put('Ặ', 'a')
            .put('ặ', 'a')
            .put('Ậ', 'a')
            .put('ậ', 'a')
            .put('Ḁ', 'a')
            .put('ḁ', 'a')
            .put('Ⱥ', 'a')
            .put('ⱥ', 'a')
            .put('Ǽ', 'a')
            .put('ǽ', 'a')
            .put('Ǣ', 'a')
            .put('ǣ', 'a')
            .put('Ḃ', 'b')
            .put('ḃ', 'b')
            .put('Ḅ', 'b')
            .put('ḅ', 'b')
            .put('Ḇ', 'b')
            .put('ḇ', 'b')
            .put('Ƀ', 'b')
            .put('ƀ', 'b')
            .put('ᵬ', 'b')
            .put('Ɓ', 'b')
            .put('ɓ', 'b')
            .put('Ƃ', 'b')
            .put('ƃ', 'b')
            .put('Ć', 'c')
            .put('ć', 'c')
            .put('Ĉ', 'c')
            .put('ĉ', 'c')
            .put('Č', 'c')
            .put('č', 'c')
            .put('Ċ', 'c')
            .put('ċ', 'c')
            .put('Ç', 'c')
            .put('ç', 'c')
            .put('Ḉ', 'c')
            .put('ḉ', 'c')
            .put('Ȼ', 'c')
            .put('ȼ', 'c')
            .put('Ƈ', 'c')
            .put('ƈ', 'c')
            .put('ɕ', 'c')
            .put('Ď', 'd')
            .put('ď', 'd')
            .put('Ḋ', 'd')
            .put('ḋ', 'd')
            .put('Ḑ', 'd')
            .put('ḑ', 'd')
            .put('Ḍ', 'd')
            .put('ḍ', 'd')
            .put('Ḓ', 'd')
            .put('ḓ', 'd')
            .put('Ḏ', 'd')
            .put('ḏ', 'd')
            .put('Đ', 'd')
            .put('đ', 'd')
            .put('ᵭ', 'd')
            .put('Ɖ', 'd')
            .put('ɖ', 'd')
            .put('Ɗ', 'd')
            .put('ɗ', 'd')
            .put('Ƌ', 'd')
            .put('ƌ', 'd')
            .put('ȡ', 'd')
            .put('ð', 'd')
            .put('É', 'e')
            .put('Ə', 'e')
            .put('Ǝ', 'e')
            .put('ǝ', 'e')
            .put('é', 'e')
            .put('È', 'e')
            .put('è', 'e')
            .put('Ĕ', 'e')
            .put('ĕ', 'e')
            .put('Ê', 'e')
            .put('ê', 'e')
            .put('Ế', 'e')
            .put('ế', 'e')
            .put('Ề', 'e')
            .put('ề', 'e')
            .put('Ễ', 'e')
            .put('ễ', 'e')
            .put('Ể', 'e')
            .put('ể', 'e')
            .put('Ě', 'e')
            .put('ě', 'e')
            .put('Ë', 'e')
            .put('ë', 'e')
            .put('Ẽ', 'e')
            .put('ẽ', 'e')
            .put('Ė', 'e')
            .put('ė', 'e')
            .put('Ȩ', 'e')
            .put('ȩ', 'e')
            .put('Ḝ', 'e')
            .put('ḝ', 'e')
            .put('Ę', 'e')
            .put('ę', 'e')
            .put('Ē', 'e')
            .put('ē', 'e')
            .put('Ḗ', 'e')
            .put('ḗ', 'e')
            .put('Ḕ', 'e')
            .put('ḕ', 'e')
            .put('Ẻ', 'e')
            .put('ẻ', 'e')
            .put('Ȅ', 'e')
            .put('ȅ', 'e')
            .put('Ȇ', 'e')
            .put('ȇ', 'e')
            .put('Ẹ', 'e')
            .put('ẹ', 'e')
            .put('Ệ', 'e')
            .put('ệ', 'e')
            .put('Ḙ', 'e')
            .put('ḙ', 'e')
            .put('Ḛ', 'e')
            .put('ḛ', 'e')
            .put('Ɇ', 'e')
            .put('ɇ', 'e')
            .put('ɚ', 'e')
            .put('ɝ', 'e')
            .put('Ḟ', 'f')
            .put('ḟ', 'f')
            .put('ᵮ', 'f')
            .put('Ƒ', 'f')
            .put('ƒ', 'f')
            .put('Ǵ', 'g')
            .put('ǵ', 'g')
            .put('Ğ', 'g')
            .put('ğ', 'g')
            .put('Ĝ', 'g')
            .put('ĝ', 'g')
            .put('Ǧ', 'g')
            .put('ǧ', 'g')
            .put('Ġ', 'g')
            .put('ġ', 'g')
            .put('Ģ', 'g')
            .put('ģ', 'g')
            .put('Ḡ', 'g')
            .put('ḡ', 'g')
            .put('Ǥ', 'g')
            .put('ǥ', 'g')
            .put('Ɠ', 'g')
            .put('ɠ', 'g')
            .put('Ĥ', 'h')
            .put('ĥ', 'h')
            .put('Ȟ', 'h')
            .put('ȟ', 'h')
            .put('Ḧ', 'h')
            .put('ḧ', 'h')
            .put('Ḣ', 'h')
            .put('ḣ', 'h')
            .put('Ḩ', 'h')
            .put('ḩ', 'h')
            .put('Ḥ', 'h')
            .put('ḥ', 'h')
            .put('Ḫ', 'h')
            .put('ḫ', 'h')
            .put('̱', 'h')
            .put('ẖ', 'h')
            .put('Ħ', 'h')
            .put('ħ', 'h')
            .put('Ⱨ', 'h')
            .put('ⱨ', 'h')
            .put('Í', 'i')
            .put('í', 'i')
            .put('Ì', 'i')
            .put('ì', 'i')
            .put('Ĭ', 'i')
            .put('ĭ', 'i')
            .put('Î', 'i')
            .put('î', 'i')
            .put('Ǐ', 'i')
            .put('ǐ', 'i')
            .put('Ï', 'i')
            .put('ï', 'i')
            .put('Ḯ', 'i')
            .put('ḯ', 'i')
            .put('Ĩ', 'i')
            .put('ĩ', 'i')
            .put('Į', 'i')
            .put('į', 'i')
            .put('Ī', 'i')
            .put('ī', 'i')
            .put('Ỉ', 'i')
            .put('ỉ', 'i')
            .put('Ȉ', 'i')
            .put('ȉ', 'i')
            .put('Ȋ', 'i')
            .put('ȋ', 'i')
            .put('Ị', 'i')
            .put('ị', 'i')
            .put('Ḭ', 'i')
            .put('ḭ', 'i')
            .put('ı', 'i')
            .put('Ɨ', 'i')
            .put('ɨ', 'i')
            .put('Ĵ', 'j')
            .put('ĵ', 'j')
            .put('̌', 'j')
            .put('ǰ', 'j')
            .put('ȷ', 'j')
            .put('Ɉ', 'j')
            .put('ɉ', 'j')
            .put('ʝ', 'j')
            .put('ɟ', 'j')
            .put('ʄ', 'j')
            .put('Ḱ', 'k')
            .put('ḱ', 'k')
            .put('Ǩ', 'k')
            .put('ǩ', 'k')
            .put('Ķ', 'k')
            .put('ķ', 'k')
            .put('Ḳ', 'k')
            .put('ḳ', 'k')
            .put('Ḵ', 'k')
            .put('ḵ', 'k')
            .put('Ƙ', 'k')
            .put('ƙ', 'k')
            .put('Ⱪ', 'k')
            .put('ⱪ', 'k')
            .put('Ĺ', 'a')
            .put('ĺ', 'l')
            .put('Ľ', 'l')
            .put('ľ', 'l')
            .put('Ļ', 'l')
            .put('ļ', 'l')
            .put('Ḷ', 'l')
            .put('ḷ', 'l')
            .put('Ḹ', 'l')
            .put('ḹ', 'l')
            .put('Ḽ', 'l')
            .put('ḽ', 'l')
            .put('Ḻ', 'l')
            .put('ḻ', 'l')
            .put('Ł', 'l')
            .put('ł', 'l')
            .put('Ŀ', 'l')
            .put('ŀ', 'l')
            .put('Ƚ', 'l')
            .put('ƚ', 'l')
            .put('Ⱡ', 'l')
            .put('ⱡ', 'l')
            .put('Ɫ', 'l')
            .put('ɫ', 'l')
            .put('ɬ', 'l')
            .put('ɭ', 'l')
            .put('ȴ', 'l')
            .put('Ḿ', 'm')
            .put('ḿ', 'm')
            .put('Ṁ', 'm')
            .put('ṁ', 'm')
            .put('Ṃ', 'm')
            .put('ṃ', 'm')
            .put('ɱ', 'm')
            .put('Ń', 'n')
            .put('ń', 'n')
            .put('Ǹ', 'n')
            .put('ǹ', 'n')
            .put('Ň', 'n')
            .put('ň', 'n')
            .put('Ñ', 'n')
            .put('ñ', 'n')
            .put('Ṅ', 'n')
            .put('ṅ', 'n')
            .put('Ņ', 'n')
            .put('ņ', 'n')
            .put('Ṇ', 'n')
            .put('ṇ', 'n')
            .put('Ṋ', 'n')
            .put('ṋ', 'n')
            .put('Ṉ', 'n')
            .put('ṉ', 'n')
            .put('Ɲ', 'n')
            .put('ɲ', 'n')
            .put('Ƞ', 'n')
            .put('ƞ', 'n')
            .put('ɳ', 'n')
            .put('ȵ', 'n')
            .put('̈', 'n')
            .put('Ó', 'o')
            .put('ó', 'o')
            .put('Ò', 'o')
            .put('ò', 'o')
            .put('Ŏ', 'o')
            .put('ŏ', 'o')
            .put('Ô', 'o')
            .put('ô', 'o')
            .put('Ố', 'o')
            .put('ố', 'o')
            .put('Ồ', 'o')
            .put('ồ', 'o')
            .put('Ỗ', 'o')
            .put('ỗ', 'o')
            .put('Ổ', 'o')
            .put('ổ', 'o')
            .put('Ǒ', 'o')
            .put('ǒ', 'o')
            .put('Ö', 'o')
            .put('ö', 'o')
            .put('Ȫ', 'o')
            .put('ȫ', 'o')
            .put('Ő', 'o')
            .put('ő', 'o')
            .put('Õ', 'o')
            .put('õ', 'o')
            .put('Ṍ', 'o')
            .put('ṍ', 'o')
            .put('Ṏ', 'o')
            .put('ṏ', 'o')
            .put('Ȭ', 'o')
            .put('ȭ', 'o')
            .put('Ȯ', 'o')
            .put('ȯ', 'o')
            .put('Ȱ', 'o')
            .put('ȱ', 'o')
            .put('Ø', 'o')
            .put('ø', 'o')
            .put('Ǿ', 'o')
            .put('ǿ', 'o')
            .put('Ǫ', 'o')
            .put('ǫ', 'o')
            .put('Ǭ', 'o')
            .put('ǭ', 'o')
            .put('Ō', 'o')
            .put('ō', 'o')
            .put('Ṓ', 'o')
            .put('ṓ', 'o')
            .put('Ṑ', 'o')
            .put('ṑ', 'o')
            .put('Ỏ', 'o')
            .put('ỏ', 'o')
            .put('Ȍ', 'o')
            .put('ȍ', 'o')
            .put('Ȏ', 'o')
            .put('ȏ', 'o')
            .put('Ơ', 'o')
            .put('ơ', 'o')
            .put('Ớ', 'o')
            .put('ớ', 'o')
            .put('Ờ', 'o')
            .put('ờ', 'o')
            .put('Ỡ', 'o')
            .put('ỡ', 'o')
            .put('Ở', 'o')
            .put('ở', 'o')
            .put('Ợ', 'o')
            .put('ợ', 'o')
            .put('Ọ', 'o')
            .put('ọ', 'o')
            .put('Ộ', 'o')
            .put('ộ', 'o')
            .put('Ɵ', 'o')
            .put('ɵ', 'o')
            .put('Ṕ', 'p')
            .put('ṕ', 'p')
            .put('Ṗ', 'p')
            .put('ṗ', 'p')
            .put('Ᵽ', 'p')
            .put('Ƥ', 'p')
            .put('ƥ', 'p')
            .put('ʠ', 'q')
            .put('Ɋ', 'q')
            .put('ɋ', 'q')
            .put('Ŕ', 'r')
            .put('ŕ', 'r')
            .put('Ř', 'r')
            .put('ř', 'r')
            .put('Ṙ', 'r')
            .put('ṙ', 'r')
            .put('Ŗ', 'r')
            .put('ŗ', 'r')
            .put('Ȑ', 'r')
            .put('ȑ', 'r')
            .put('Ȓ', 'r')
            .put('ȓ', 'r')
            .put('Ṛ', 'r')
            .put('ṛ', 'r')
            .put('Ṝ', 'r')
            .put('ṝ', 'r')
            .put('Ṟ', 'r')
            .put('ṟ', 'r')
            .put('Ɍ', 'r')
            .put('ɍ', 'r')
            .put('ᵲ', 'r')
            .put('ɼ', 'r')
            .put('Ɽ', 'r')
            .put('ɽ', 'r')
            .put('ɾ', 'r')
            .put('ᵳ', 'r')
            .put('ß', 's')
            .put('Ś', 's')
            .put('ś', 's')
            .put('Ṥ', 's')
            .put('ṥ', 's')
            .put('Ŝ', 's')
            .put('ŝ', 's')
            .put('Š', 's')
            .put('š', 's')
            .put('Ṧ', 's')
            .put('ṧ', 's')
            .put('Ṡ', 's')
            .put('ṡ', 's')
            .put('ẛ', 's')
            .put('Ş', 's')
            .put('ş', 's')
            .put('Ṣ', 's')
            .put('ṣ', 's')
            .put('Ṩ', 's')
            .put('ṩ', 's')
            .put('Ș', 's')
            .put('ș', 's')
            .put('ʂ', 's')
            .put('̩', 's')
            .put('Þ', 't')
            .put('þ', 't')
            .put('Ť', 't')
            .put('ť', 't')
            .put('ẗ', 't')
            .put('Ṫ', 't')
            .put('ṫ', 't')
            .put('Ţ', 't')
            .put('ţ', 't')
            .put('Ṭ', 't')
            .put('ṭ', 't')
            .put('Ț', 't')
            .put('ț', 't')
            .put('Ṱ', 't')
            .put('ṱ', 't')
            .put('Ṯ', 't')
            .put('ṯ', 't')
            .put('Ŧ', 't')
            .put('ŧ', 't')
            .put('Ⱦ', 't')
            .put('ⱦ', 't')
            .put('ᵵ', 't')
            .put('ƫ', 't')
            .put('Ƭ', 't')
            .put('ƭ', 't')
            .put('Ʈ', 't')
            .put('ʈ', 't')
            .put('ȶ', 't')
            .put('Ú', 'u')
            .put('ú', 'u')
            .put('Ù', 'u')
            .put('ù', 'u')
            .put('Ŭ', 'u')
            .put('ŭ', 'u')
            .put('Û', 'u')
            .put('û', 'u')
            .put('Ǔ', 'u')
            .put('ǔ', 'u')
            .put('Ů', 'u')
            .put('ů', 'u')
            .put('Ü', 'u')
            .put('ü', 'u')
            .put('Ǘ', 'u')
            .put('ǘ', 'u')
            .put('Ǜ', 'u')
            .put('ǜ', 'u')
            .put('Ǚ', 'u')
            .put('ǚ', 'u')
            .put('Ǖ', 'u')
            .put('ǖ', 'u')
            .put('Ű', 'u')
            .put('ű', 'u')
            .put('Ũ', 'u')
            .put('ũ', 'u')
            .put('Ṹ', 'u')
            .put('ṹ', 'u')
            .put('Ų', 'u')
            .put('ų', 'u')
            .put('Ū', 'u')
            .put('ū', 'u')
            .put('Ṻ', 'u')
            .put('ṻ', 'u')
            .put('Ủ', 'u')
            .put('ủ', 'u')
            .put('Ȕ', 'u')
            .put('ȕ', 'u')
            .put('Ȗ', 'u')
            .put('ȗ', 'u')
            .put('Ư', 'u')
            .put('ư', 'u')
            .put('Ứ', 'u')
            .put('ứ', 'u')
            .put('Ừ', 'u')
            .put('ừ', 'u')
            .put('Ữ', 'u')
            .put('ữ', 'u')
            .put('Ử', 'u')
            .put('ử', 'u')
            .put('Ự', 'u')
            .put('ự', 'u')
            .put('Ụ', 'u')
            .put('ụ', 'u')
            .put('Ṳ', 'u')
            .put('ṳ', 'u')
            .put('Ṷ', 'u')
            .put('ṷ', 'u')
            .put('Ṵ', 'u')
            .put('ṵ', 'u')
            .put('Ʉ', 'u')
            .put('ʉ', 'u')
            .put('Ṽ', 'v')
            .put('ṽ', 'v')
            .put('Ṿ', 'v')
            .put('ṿ', 'v')
            .put('Ʋ', 'v')
            .put('ʋ', 'v')
            .put('Ẃ', 'w')
            .put('ẃ', 'w')
            .put('Ẁ', 'w')
            .put('ẁ', 'w')
            .put('Ŵ', 'w')
            .put('ŵ', 'w')
            .put('̊', 'w')
            .put('ẘ', 'w')
            .put('Ẅ', 'w')
            .put('ẅ', 'w')
            .put('Ẇ', 'w')
            .put('ẇ', 'w')
            .put('Ẉ', 'w')
            .put('ẉ', 'w')
            .put('Ẍ', 'x')
            .put('ẍ', 'x')
            .put('Ẋ', 'x')
            .put('ẋ', 'x')
            .put('Ý', 'y')
            .put('ý', 'y')
            .put('Ỳ', 'y')
            .put('ỳ', 'y')
            .put('Ŷ', 'y')
            .put('ŷ', 'y')
            .put('ẙ', 'y')
            .put('Ÿ', 'y')
            .put('ÿ', 'y')
            .put('Ỹ', 'y')
            .put('ỹ', 'y')
            .put('Ẏ', 'y')
            .put('ẏ', 'y')
            .put('Ȳ', 'y')
            .put('ȳ', 'y')
            .put('Ỷ', 'y')
            .put('ỷ', 'y')
            .put('Ỵ', 'y')
            .put('ỵ', 'y')
            .put('ʏ', 'y')
            .put('Ɏ', 'y')
            .put('ɏ', 'y')
            .put('Ƴ', 'y')
            .put('ƴ', 'y')
            .put('Ź', 'z')
            .put('ź', 'z')
            .put('Ẑ', 'z')
            .put('ẑ', 'z')
            .put('Ž', 'z')
            .put('ž', 'z')
            .put('Ż', 'z')
            .put('ż', 'z')
            .put('Ẓ', 'z')
            .put('ẓ', 'z')
            .put('Ẕ', 'z')
            .put('ẕ', 'z')
            .put('Ƶ', 'z')
            .put('ƶ', 'z')
            .put('Ȥ', 'z')
            .put('ȥ', 'z')
            .put('ʐ', 'z')
            .put('ʑ', 'z')
            .put('Ⱬ', 'z')
            .put('ⱬ', 'z')
            .put('Ǯ', 'z')
            .put('ǯ', 'z')
            .put('ƺ', 'z')
            .build();
    }
}
