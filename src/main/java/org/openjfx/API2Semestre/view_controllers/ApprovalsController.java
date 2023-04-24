package org.openjfx.api2semestre.view_controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.openjfx.api2semestre.appointments.Appointment;
import org.openjfx.api2semestre.appointments.Status;
import org.openjfx.api2semestre.authentication.Authentication;
import org.openjfx.api2semestre.custom_tags.ViewConfig;
import org.openjfx.api2semestre.database.QueryLibs;
import org.openjfx.api2semestre.view_macros.TableCheckBoxMacros;
import org.openjfx.api2semestre.view_macros.TableColumnFilterMacros;
import org.openjfx.api2semestre.view_utils.AppointmentFilter;
import org.openjfx.api2semestre.view_utils.AppointmentWrapper;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ApprovalsController implements Initializable {

    @FXML
    private ViewConfig view;

    @FXML
    private Button btn_approve;

    @FXML
    private Button btn_reject;

    @FXML
    private TableColumn<AppointmentWrapper, Boolean> col_selecionar;

    @FXML
    private TableColumn<AppointmentWrapper, String> col_requester;
    private BooleanProperty col_requester_enableFilter = new SimpleBooleanProperty();

    @FXML
    private TableColumn<AppointmentWrapper, String> col_squad;
    private BooleanProperty col_squad_enableFilter = new SimpleBooleanProperty();

    @FXML
    private TableColumn<AppointmentWrapper, String> col_tipo;
    private BooleanProperty col_tipo_enableFilter = new SimpleBooleanProperty();

    @FXML
    private TableColumn<AppointmentWrapper, String> col_inicio;
    private BooleanProperty col_inicio_enableFilter = new SimpleBooleanProperty();

    @FXML
    private TableColumn<AppointmentWrapper, String> col_fim;
    private BooleanProperty col_fim_enableFilter = new SimpleBooleanProperty();

    @FXML
    private TableColumn<AppointmentWrapper, String> col_cliente;
    private BooleanProperty col_cliente_enableFilter = new SimpleBooleanProperty();

    @FXML
    private TableColumn<AppointmentWrapper, String> col_projeto;
    private BooleanProperty col_projeto_enableFilter = new SimpleBooleanProperty();

    @FXML
    private TableColumn<AppointmentWrapper, String> col_total;

    @FXML 
    private TableView<AppointmentWrapper> tabela;
    private ObservableList<AppointmentWrapper> displayedAppointments;
    private List<Appointment> loadedAppointments;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
    
        buildTable();

        updateTable();

    }

    private void buildTable () {

        col_selecionar.setCellValueFactory( new PropertyValueFactory<>( "selected" ));
        TableCheckBoxMacros.setCheckBoxHeader(tabela, col_selecionar);

        ChangeListener<Boolean> applyFilterCallback = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                applyFilter();
            }
        };

        col_requester.setCellValueFactory( new PropertyValueFactory<>( "requester" ));
        TableColumnFilterMacros.setTextFieldHeader(col_requester, "Solicitante", col_requester_enableFilter);
        col_requester_enableFilter.addListener(applyFilterCallback);

        col_squad.setCellValueFactory( new PropertyValueFactory<>( "squad" ));
        TableColumnFilterMacros.setTextFieldHeader(col_squad, "CR", col_squad_enableFilter);
        col_squad_enableFilter.addListener(applyFilterCallback);

        col_tipo.setCellValueFactory( new PropertyValueFactory<>( "type" ));
        // TableColumnFilterMacros.setTextFieldHeader(col_tipo, "Tipo", col_tipo_enableFilter);
        // col_tipo_enableFilter.addListener(applyFilterCallback);

        col_inicio.setCellValueFactory( new PropertyValueFactory<>( "startDate" ));
        // TableColumnFilterMacros.setTextFieldHeader(col_inicio, "Início", col_inicio_enableFilter);
        // col_inicio_enableFilter.addListener(applyFilterCallback);

        col_fim.setCellValueFactory( new PropertyValueFactory<>( "endDate" ));
        // TableColumnFilterMacros.setTextFieldHeader(col_fim, "Fim", col_fim_enableFilter);
        // col_fim_enableFilter.addListener(applyFilterCallback);

        col_cliente.setCellValueFactory( new PropertyValueFactory<>( "client" ));
        TableColumnFilterMacros.setTextFieldHeader(col_cliente, "Cliente", col_cliente_enableFilter);
        col_cliente_enableFilter.addListener(applyFilterCallback);

        col_projeto.setCellValueFactory( new PropertyValueFactory<>( "project" ));
        TableColumnFilterMacros.setTextFieldHeader(col_projeto, "Projeto", col_projeto_enableFilter);
        col_projeto_enableFilter.addListener(applyFilterCallback);

        col_total.setCellValueFactory( new PropertyValueFactory<>( "total" ));
    }

    private void updateTable () {
        List<Appointment> items = List.of();
        for (String centroResultado : Authentication.getCurrentUser().getManagesCRs()) {
            for(Appointment apt : QueryLibs.squadSelect(centroResultado)) {
                items.add(apt);
            }
        }
        System.out.println(items.size() + " appointments returned from select ");
    
        loadedAppointments = items;

        applyFilter();
    }

    private void applyFilter () {

        System.out.println("applyFilter");

        List<Appointment> appointmentsToDisplay = AppointmentFilter.filterFromView(
            loadedAppointments,
            col_requester_enableFilter.get() ? Optional.of(col_requester) : Optional.empty(),
            col_tipo_enableFilter.get() ? Optional.of(col_tipo) : Optional.empty(),
            col_inicio_enableFilter.get() ? Optional.of(col_inicio) : Optional.empty(),
            col_fim_enableFilter.get() ? Optional.of(col_fim) : Optional.empty(),
            col_squad_enableFilter.get() ? Optional.of(col_squad) : Optional.empty(),
            col_cliente_enableFilter.get() ? Optional.of(col_cliente) : Optional.empty(),
            col_projeto_enableFilter.get() ? Optional.of(col_projeto) : Optional.empty(),
            Optional.of(Status.Pending)
        );

        displayedAppointments = FXCollections.observableArrayList(
            appointmentsToDisplay.stream().map((Appointment apt) -> new AppointmentWrapper(apt)).collect(Collectors.toList())
        );

        tabela.setItems(displayedAppointments);
        tabela.refresh();

        col_selecionar.setGraphic(null);
        TableCheckBoxMacros.setCheckBoxHeader(tabela, col_selecionar);
    }

    private List<AppointmentWrapper> getSelected () {
        return displayedAppointments.stream().filter((AppointmentWrapper apt) -> apt.getSelected()).collect(Collectors.toList());
    }

    @FXML
    private void showApprovePopup (ActionEvent event) throws IOException {
        System.out.println("showApprovePopup");
        createPopup(
            "approvePopupListItem.fxml",
            "Aprovar",
            (List<ApprovePopupListItem> controllers) -> {
                System.out.println("showApprovePopup callback");
                for (ApprovePopupListItem controller : controllers) {
                    Appointment appointment = controller.getSelected().getAppointment();
                    appointment.setStatus(1);
                    QueryLibs.updateTable(appointment);
                    System.out.println("Apontamento atualizado");
                }
                updateTable();
            }
        );
    }

    @FXML
    private void showRejectPopup (ActionEvent event) throws IOException {
        System.out.println("showRejectPopup");
        createPopup(
            "rejectPopupListItem.fxml",
            "Rejeitar",
            (List<RejectPopupListItem> controllers) -> {
                System.out.println("showRejectPopup callback");
                List<Appointment> appointments = new ArrayList<>();
                for (RejectPopupListItem controller : controllers) {
                    String feedback = controller.getFeedback();
                    if (feedback == null || feedback.isEmpty()) {
                        System.out.println("feedback não preenchido");
                        return;
                    }
                    Appointment appointment = controller.getSelected().getAppointment();
                    appointment.setFeedback(feedback);
                    appointment.setStatus(2);
                    appointments.add(appointment);
                }
                for (Appointment appointment : appointments) {
                    QueryLibs.updateTable(appointment);
                    System.out.println("Apontamento atualizado");
                }
                updateTable();
            }
        );
    }

    private <T extends PopupController> Stage createPopup (String popupFxmlFile, String actionText, PopupCallbackHandler<T> callback) {

        // Get the list of items from the main application
        List<AppointmentWrapper> selectedAppointments = getSelected();

        // Create a new stage for the popup
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle(actionText + " " + selectedAppointments.size() + " apontamentos selecionados");

        // Create a VBox to hold the HBox controls for each item
        VBox vbox = new VBox();
        vbox.setSpacing(8);
        vbox.setPadding(new Insets(16));

        List<T> controllers = selectedAppointments.stream().map((AppointmentWrapper aptWrapper) -> {
            try {
                // Load the FXML file for the list item
                FXMLLoader loader = new FXMLLoader(getClass().getResource(popupFxmlFile));
                VBox listItem = loader.load();
                T controller = loader.getController();


                controller.setSelected(aptWrapper);
                controller.buildTable();

                // TextField textField = (TextField) listItem.lookup("#textField");

                // Add the HBox to the VBox
                vbox.getChildren().add(listItem);

                return controller;

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());

        // Create a scroll pane to hold the VBox
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(vbox);
        scrollPane.setFitToWidth(true);

        // Create a button to close the popup
        Button closeButton = new Button(actionText);
        closeButton.setOnAction(event -> {
            callback.handlePopupList(controllers);
            popupStage.close();
        });

        // Create a VBox to hold the scroll pane and close button
        VBox root = new VBox(scrollPane, closeButton);

        // Create a scene for the popup
        Scene scene = new Scene(root, 800, 400);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    
        return popupStage;
    }

}