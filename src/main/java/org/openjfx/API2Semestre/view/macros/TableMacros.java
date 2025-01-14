package org.openjfx.api2semestre.view.macros;

import java.io.IOException;
import java.util.Optional;

import org.openjfx.api2semestre.App;
import org.openjfx.api2semestre.view.utils.interfaces.EditPopup;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class TableMacros {

    public static <T> void buildTable (
        TableView<T> tabela,
        ColumnConfig<T,?>[] columns,
        Optional<ChangeListener<Boolean>> applyFilterCallback
    ) {
        for (ColumnConfig<T, ?> column : columns) {
            column.build(tabela, applyFilterCallback);
        }
    }

    @FunctionalInterface public static interface DeleteConfirmationCallback<T> { void execute(T item); }
    @FunctionalInterface public static interface Validator<T> { boolean validate(T value); }
    @FunctionalInterface public static interface Updater<S, T> { void update(S item, T value); }

    public interface Formatter<T> {
        public static Formatter<String> DEFAULT_STRING_FORMATTER = new Formatter<String>() {
            private final StringConverter<String> converter = new StringConverter<String>() {
                @Override public String toString(String object) { return object; }
                @Override public String fromString(String string) { return string; }
            };
            @Override public String format(String value, boolean editing) { return value; }
            @Override public String parse(String text) { return text; }
            @Override public StringConverter<String> getConverter() { return converter; }
        };
        String format(T value, boolean editing);
        String parse(String text);
        StringConverter<T> getConverter();
    }

    public static <T> void createDeleteColumn(TableView<T> table, String objectName, DeleteConfirmationCallback<T> deleteConfirmationCallback) {
        TableColumn<T, Void> buttonColumn = new TableColumn<>("Opção");
        buttonColumn.setMaxWidth(64);
        buttonColumn.setResizable(false);
        buttonColumn.setEditable(false);
        buttonColumn.setCellFactory(param -> new TableCell<>() {
            {
                setAlignment(Pos.CENTER);
            }
            private final Button button = new Button("Deletar"); {
                button.maxHeight(20);
                button.setPrefWidth(-1);
                button.setOnAction(event -> {
                    try {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "");

                        alert.initModality(Modality.APPLICATION_MODAL);
                        alert.initOwner((Stage) table.getScene().getWindow());

                        alert.getDialogPane().setContentText(
                            "Tem certeza que deseja excluir este " + objectName + "? Essa operação não pode ser desfeita."
                        );
                        alert.getDialogPane().setHeaderText("Excluir " + objectName + "");

                        Optional<ButtonType> result = alert.showAndWait();

                        if(result.get() != ButtonType.OK) return;

                        deleteConfirmationCallback.execute(table.getItems().get(getIndex()));
                    }
                    catch (Exception e) { e.printStackTrace(); }
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(button);
            }
        });
        table.getColumns().add(buttonColumn);
    }

    public static <T, P extends EditPopup<T>> void createEditPopupColumn (
        TableView<T> table,
        String objectName,
        String fxmlTemplate,
        Optional<DeleteConfirmationCallback<T>> deleteConfirmationCallback
    ) {
        TableColumn<T, Void> buttonColumn = new TableColumn<>("Opção");
        buttonColumn.setMaxWidth(64);
        buttonColumn.setResizable(false);
        buttonColumn.setEditable(false);
        buttonColumn.setCellFactory(param -> new TableCell<>() {
            private final Button button = new Button("Editar"); {
                button.maxHeight(20);
                button.setOnAction(event -> {
                    Stage popupStage = new Stage();
                    popupStage.initModality(Modality.APPLICATION_MODAL);
                    popupStage.setTitle("Editar " + objectName);

                    try {
                        FXMLLoader loader = new FXMLLoader(App.getFXML(fxmlTemplate));
                        VBox root = loader.load();
                        P controller = loader.getController();

                        controller.setSelected(table.getItems().get(getIndex()));

                        if (deleteConfirmationCallback.isPresent()) {
                            createDeleteColumn(
                                controller.getTable(),
                                objectName,
                                (T item) -> {
                                    deleteConfirmationCallback.get().execute(item);
                                    popupStage.close();
                                }
                            );
                        }

                        controller.setSaveCallback((T newItem) -> {
                            table.getItems().set(getIndex(), newItem);
                            table.refresh();
                        });

                        Scene scene = new Scene(root, -1, -1);
                        popupStage.setScene(scene);
                        popupStage.showAndWait();

                    } catch (IOException e) { e.printStackTrace(); }
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(button);
            }
        });
        table.getColumns().add(buttonColumn);

    }

    public static <S, T> void enableEditableCells(TableColumn<S, T> column, Validator<T> validator, Updater<S, T> updater, Formatter<T> formatter) {

        column.getTableView().setEditable(true);
        column.setEditable(true);

        column.setCellFactory(c -> {
            TextFieldTableCell<S, T> cell = new TextFieldTableCell<S, T>(formatter.getConverter()) {
                {
                    setAlignment(Pos.CENTER);
                }
                @Override public void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(formatter.format(item, isEditing()));
                    }
                }
            };

            cell.setEditable(true);

            cell.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !cell.isEditing()) {
                    cell.setEditable(true);
                    cell.startEdit();
                }
            });

            cell.setOnKeyPressed(event -> {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    cell.commitEdit(cell.getConverter().fromString(formatter.parse(cell.getText())));
                    S item = cell.getTableView().getItems().get(cell.getIndex());
                    updater.update(item, cell.getItem());
                    cell.setEditable(false);
                    cell.setStyle("");
                } else if (event.getCode().equals(KeyCode.ESCAPE)) {
                    cell.cancelEdit();
                    cell.setEditable(false);
                    cell.setStyle("");
                }
            });

            cell.setOnKeyReleased(event -> {
                if (event.getCode().equals(KeyCode.ENTER) || event.getCode().equals(KeyCode.ESCAPE)) {
                    cell.setEditable(false);
                    cell.setStyle("");
                }
            });

            cell.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && cell.isEditing()) {
                    cell.setEditable(false);
                    cell.setStyle("");
                }
            });

            return cell;
        });

        column.setOnEditCommit((CellEditEvent<S, T> event) -> {
            T newValue = event.getNewValue();
            if (validator.validate(newValue)) {
                S item = event.getTableView().getItems().get(event.getTablePosition().getRow());
                updater.update(item, newValue);
            } else {
                TableView<S> tableView = event.getTableView();
                tableView.refresh();
            }
        });
    }



}
