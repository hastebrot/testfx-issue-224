package org.testfx.issue224;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class AutoCompleteComboBoxTest extends ApplicationTest
{
    AutoCompleteComboBox autoCompleteComboBox;

    @Override
    public void start(Stage stage) throws Exception
    {
        autoCompleteComboBox = new AutoCompleteComboBox(fruits);
        autoCompleteComboBox.setId("autocompletebox");
        autoCompleteComboBox.requestFocus();
        StackPane root = new StackPane(autoCompleteComboBox);
        Scene scene = new Scene(root, 400, 600);

        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void shouldAutoCompleteUsingContainsMode()
    {
        press(KeyCode.A);
        release(KeyCode.A);

        assertThat(autoCompleteComboBox.getComboBox().getItems()).containsExactly("Açaí", "Apple", "Apricot", "Avocado",
                "Ackee", "Banana", "Blackberry", "Cantaloupe", "Cardón", "Cranberry", "Currant", "Date", "Durian",
                "Eggplant", "Grape", "Grapefruit", "Guava", "Ita Palm", "Jatobá", "Kumquat", "Lúcuma", "Mango",
                "Mangosteen", "Nectarine", "Orange", "Papaya", "Passion Fruit", "Peach", "Pear", "Pineapple",
                "Pomegranate", "Prickly Pear", "Rambuton", "Raspberry", "Rose Apple", "Starfruit", "Sapadilla",
                "Strawberry", "Tamarind", "Tangelo", "Tanagerine", "Tomato", "Tōtara", "Voavanga", "Watermelon",
                "Xigua Melon");

        press(KeyCode.P);
        release(KeyCode.P);

        assertThat(autoCompleteComboBox.getComboBox().getItems()).containsExactly("Apple", "Apricot", "Grape",
                "Grapefruit", "Papaya", "Pineapple", "Rose Apple", "Sapadilla");

        press(KeyCode.P);
        release(KeyCode.P);

        assertThat(autoCompleteComboBox.getComboBox().getItems()).containsExactly("Apple", "Pineapple", "Rose Apple");

        press(KeyCode.DOWN);
        release(KeyCode.DOWN);

        press(KeyCode.ENTER);
        release(KeyCode.ENTER);

        assertThat(autoCompleteComboBox.getSelection()).isEqualTo("Apple");
    }

    @Test
    public void testAutoCompleteStartsWithMode()
    {
        autoCompleteComboBox.setAutoCompleteMode(AutoCompleteComboBox.AutoCompleteMode.STARTS_WITH);
        assertThat(autoCompleteComboBox.getAutoCompleteMode()).isEqualTo(AutoCompleteComboBox.AutoCompleteMode.STARTS_WITH);

        press(KeyCode.A);
        release(KeyCode.A);

        assertThat(autoCompleteComboBox.getComboBox().getItems()).containsExactly("Açaí", "Apple", "Apricot", "Avocado",
                "Ackee");

        press(KeyCode.P);
        release(KeyCode.P);

        assertThat(autoCompleteComboBox.getComboBox().getItems()).containsExactly("Apple", "Apricot");

        press(KeyCode.P);
        release(KeyCode.P);

        assertThat(autoCompleteComboBox.getComboBox().getItems()).containsExactly("Apple");
    }

    public static List<String> fruits;
    static
    {
        fruits = new ArrayList<>();
        fruits.addAll(Arrays.asList("Açaí", "Apple", "Apricot", "Avocado", "Ackee", "Banana", "Blueberry",
                "Blackberry", "Cantaloupe", "Cardón", "Cherry", "Cranberry", "Cucumber", "Currant", "Date", "Durian",
                "Eggplant", "Elderberry", "Fig", "Gooseberry", "Grape", "Grapefruit", "Guava", "Honey Dew Melon",
                "Horned Melon", "Huckleberry", "Ita Palm", "Jatobá", "Jujube", "Kiwi", "Kumquat", "Lemon", "Lime",
                "Lúcuma", "Lychee", "Mango", "Mangosteen", "Mortiño", "Mulberry", "Muskmelon", "Nectarine", "Néré",
                "Olive", "Orange", "Papaya", "Passion Fruit", "Peach", "Pear", "Pepper", "Persimmon", "Pineapple",
                "Plum", "Pluot", "Pomegranate", "Prickly Pear", "Quince", "Rambuton", "Raspberry", "Rose Apple",
                "Starfruit", "Sapadilla", "Strawberry", "Tamarind", "Tangelo", "Tanagerine", "Tomato", "Tōtara",
                "Ugli Fruit", "Voavanga", "Watermelon", "Xigua Melon", "Zucchini"));
    }
}
