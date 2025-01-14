package org.openjfx.api2semestre.view.controllers.custom_tags;

import java.util.Arrays;
import java.util.LinkedList;

import org.openjfx.api2semestre.data.HasDisplayName;
import org.openjfx.api2semestre.data.HasId;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Popup;

public class LookupTextField<T extends HasDisplayName & HasId> extends TextField {

    private final ObservableList<T> suggestions = FXCollections.observableList(new LinkedList<T>());
    private final ObjectProperty<T> selectedItem;
    private final ObservableList<T> listViewSuggestions;
    private ListView<T> suggestionsListView;

    public LookupTextField (T[] suggestions) {
        this(null, new LinkedList<T>(Arrays.asList(suggestions)));
    }
    public LookupTextField (String promptText, T[] suggestions) {
        this(promptText, new LinkedList<T>(Arrays.asList(suggestions)));
    }
    public LookupTextField (String promptText, LinkedList<T> suggestions) {
        this.suggestions.setAll(suggestions);
        this.listViewSuggestions = FXCollections.observableArrayList();
        this.selectedItem = new SimpleObjectProperty<>();

        this.setPromptText(promptText);

        suggestionsListView = new ListView<>(this.listViewSuggestions);
        suggestionsListView.setCellFactory(param -> new ListCell<>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
        Popup popup = new Popup();
        popup.getContent().add(suggestionsListView);

        focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
                if (newPropertyValue) {
                    if (getText().isEmpty()) {
                        updateListViewSuggestions();
                        // System.out.println(suggestionsListView.getItems().size() + " suggestions (focus)");
                        showPopup(popup);
                    }
                    // System.out.println("Textfield focus");
                } else {
                    popup.hide();
                    // System.out.println("Textfield out focus");
                }
            }
        });

        textProperty().addListener((observable, oldValue, newValue) -> {
            T selItem = selectedItem.get();
            if (selItem != null && newValue.equals(selItem.getName())) {
                setStyle("-fx-text-fill: black;");
            } else {
                if (newValue.isBlank()) {
                    setStyle("-fx-text-fill: black;");
                } else {
                    setStyle("-fx-text-fill: red;");
                }
                selectedItem.set(null);
            }
            if (focusedProperty().get()) {
                // System.out.println("newvalue NOT empty");
                suggestionsListView.getItems().clear();
                // System.out.println(this.suggestions.length);
                for (T suggestion : this.suggestions) {
                    // System.out.println(
                    //     "suggestion(" + suggestion.getName().toLowerCase() + 
                    //     ") newValue(" + newValue.toLowerCase() +
                    //     ") contains? " + suggestion.getName().toLowerCase().contains(newValue.toLowerCase())
                    // );
                    if (suggestion.getName().toLowerCase().contains(newValue.toLowerCase())) {
                        suggestionsListView.getItems().add(suggestion);
                    }
                };        
                if (!showPopup(popup)) {
                    popup.hide();
                }
            }
        });

        suggestionsListView.setOnMouseClicked(event -> {
            T selectedItem = suggestionsListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                setText(selectedItem.getName());
                this.selectedItem.set(selectedItem);
                setStyle("-fx-text-fill: black;");
            }
            popup.hide();
        });
    }

    private boolean showPopup (Popup popup) {
        if (suggestionsListView.getItems().isEmpty()) {
            // System.out.println("empty");
            return false;
        }
        // System.out.println(suggestionsListView.getItems().size() + " suggestions");
        Bounds bounds = localToScreen(getBoundsInLocal());
        double popupY = bounds.getMaxY();
        double popupHeight = 24 * suggestionsListView.getItems().size();
        suggestionsListView.setMaxHeight(popupHeight);
        suggestionsListView.setMinHeight(popupHeight);
        suggestionsListView.setPrefHeight(popupHeight);
        suggestionsListView.refresh();
        popup.show(this, bounds.getMinX(), popupY);
        return true;
    }

    private void updateListViewSuggestions() {
        suggestionsListView.getItems().clear();
        for (T item : this.suggestions) {
            suggestionsListView.getItems().add(item);
        }
    }

    public T getSelectedItem() {
        return selectedItem.get();
    }

    public void clear() {
        selectedItem.set(null);
        setText("");
    }

    public ObjectProperty<T> selectedItemProperty() {
        return selectedItem;
    }

    public void addSuggestion (T suggestion) {
        this.suggestions.add(suggestion);
    }

    public void removeSuggestion (T suggestion) {
        if (this.suggestions.size() < 1) return;
        for (T item : this.suggestions) {
            if (item.getId() != suggestion.getId()) continue; 
            suggestions.remove(item);
            return;
        }
    }

    public java.util.List<T> getSuggestions() {
        return this.suggestions.stream().collect(java.util.stream.Collectors.toList());
    }

}
