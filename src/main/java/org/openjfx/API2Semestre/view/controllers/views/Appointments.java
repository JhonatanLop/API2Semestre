package org.openjfx.api2semestre.view.controllers.views;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openjfx.api2semestre.App;
import org.openjfx.api2semestre.appointment.Appointment;
import org.openjfx.api2semestre.appointment.AppointmentType;
import org.openjfx.api2semestre.appointment.Status;
import org.openjfx.api2semestre.authentication.Authentication;
import org.openjfx.api2semestre.data.Client;
import org.openjfx.api2semestre.data.ResultCenter;
import org.openjfx.api2semestre.database.QueryLibs;
import org.openjfx.api2semestre.utils.DateConverter;
import org.openjfx.api2semestre.view.controllers.custom_tags.LookupTextField;
import org.openjfx.api2semestre.view.controllers.popups.Feedback;
import org.openjfx.api2semestre.view.macros.ColumnConfig;
import org.openjfx.api2semestre.view.macros.ColumnConfigStatus;
import org.openjfx.api2semestre.view.macros.ColumnConfigString;
import org.openjfx.api2semestre.view.macros.TableMacros;
import org.openjfx.api2semestre.view.utils.filters.AppointmentFilter;
import org.openjfx.api2semestre.view.utils.wrappers.AppointmentWrapper;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Appointments {

    @FXML
    private TextField tf_cliente;

    @FXML
    private DatePicker tf_dataFinal;

    @FXML
    private DatePicker tf_dataInicio;

    @FXML
    private TextField tf_horaFinal;

    @FXML
    private TextField tf_horaInicio;

    @FXML
    private TextField tf_justificativa;

    @FXML
    private TextField tf_projeto;

    @FXML
    private TextField tf_resultCenter;

    @FXML
    private Button bt_horaExtra;

    @FXML
    private Button bt_sobreaviso;

    @FXML
    private TableColumn<AppointmentWrapper, String> col_status;
    // private BooleanProperty col_status_enableFilter = new SimpleBooleanProperty();

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


    public void initialize() {
        buildTable();

        updateTable();

        // Inicia a conexão com o banco de dados
        Optional<java.sql.Connection> connectionOptional = QueryLibs.connect();

        // Query all user's resultCenters. TODO: implement pagination
        ResultCenter[] resultCenters = QueryLibs.selectResultCentersOfMember(
            Authentication.getCurrentUser().getId(),
            connectionOptional
        );

        // Query all clients. TODO: implement pagination
        Client[] clients = QueryLibs.selectAllClients(connectionOptional);

        // Fecha a conexão com o banco de dados
        QueryLibs.close(connectionOptional);

        // Create a new LookupTextField to replace the default javafx TextField at tf_resultCenter
        LookupTextField<ResultCenter> lookupTextFieldResultCenter = new LookupTextField<ResultCenter>(resultCenters);
        ((FlowPane)tf_resultCenter.getParent()).getChildren().set(
            ((FlowPane)tf_resultCenter.getParent()).getChildren().indexOf(tf_resultCenter),
            lookupTextFieldResultCenter
        );
        tf_resultCenter = lookupTextFieldResultCenter;

        // Create a new LookupTextField to replace the default javafx TextField at tf_cliente
        LookupTextField<Client> lookupTextFieldClient = new LookupTextField<Client>(clients);
        ((FlowPane)tf_cliente.getParent()).getChildren().set(
            ((FlowPane)tf_cliente.getParent()).getChildren().indexOf(tf_cliente),
            lookupTextFieldClient
        );
        tf_cliente = lookupTextFieldClient;
    }

    @SuppressWarnings("unchecked")
    private void buildTable () {

        ChangeListener<Boolean> applyFilterCallback = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                applyFilter();
            }
        };

        TableMacros.buildTable(
            tabela,
            new ColumnConfig[] {
                new ColumnConfigStatus(col_status, "status", "Status"),
                new ColumnConfigString<>(col_squad, "resultCenter", "CR", Optional.of(col_squad_enableFilter)),
                new ColumnConfigString<>(col_tipo, "type", "Tipo", Optional.of(col_tipo_enableFilter)),
                new ColumnConfigString<>(col_inicio, "startDate", "Data Início", Optional.of(col_inicio_enableFilter)),
                new ColumnConfigString<>(col_fim, "endDate", "Data Fim", Optional.of(col_fim_enableFilter)),
                new ColumnConfigString<>(col_cliente, "client", "Cliente", Optional.of(col_cliente_enableFilter)),
                new ColumnConfigString<>(col_projeto, "project", "Projeto", Optional.of(col_projeto_enableFilter)),
                new ColumnConfigString<>(col_total, "total", "Total", Optional.empty())
            },
            Optional.of(applyFilterCallback)
        );

        tabela.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() != 1) return;

                Status targetStatus = Status.Rejected;
                AppointmentWrapper selectedItem = tabela.getSelectionModel().getSelectedItem();
                if (selectedItem == null || selectedItem.getAppointment().getStatus() != targetStatus) return;

                Feedback.apt_selected = selectedItem;
                popUp("popups/feedback");

            }
        });
    }

    private void updateTable () {

        System.out.println("current user id: " + Authentication.getCurrentUser().getId());
        loadedAppointments = Arrays.asList(QueryLibs.selectAppointmentsOfUser(Authentication.getCurrentUser().getId()));
        System.out.println(loadedAppointments.size() + " appointments returned from select ");

        applyFilter();
    }

    private void applyFilter () {

        List<Appointment> appointmentsToDisplay = AppointmentFilter.filterFromView(
            loadedAppointments,
            Optional.empty(),
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

    }

    @FXML
    void inputHoraExtra(ActionEvent event){
        System.out.println("inputHoraExtra");
        inputAppointment(AppointmentType.Overtime);
    }

    @FXML
    void inputSobreaviso(ActionEvent event) {
        System.out.println("inputSobreaviso");
        inputAppointment(AppointmentType.OnNotice);
    }

    void inputAppointment (AppointmentType type) {
        @SuppressWarnings("unchecked") LookupTextField<ResultCenter> lookupTfResultCenter = ((LookupTextField<ResultCenter>)tf_resultCenter);
        Integer resultCenter_id = lookupTfResultCenter.getSelectedItem().getId();
        @SuppressWarnings("unchecked") LookupTextField<Client> lookupTfClient = ((LookupTextField<Client>)tf_cliente);
        Client cliente = lookupTfClient.getSelectedItem();
        Integer cliente_id = cliente.getId();
        try {
            int appointmentId = QueryLibs.insertAppointment(new Appointment(
                Authentication.getCurrentUser().getId(),
                type,
                DateConverter.inputToTimestamp(tf_dataInicio.getValue(), tf_horaInicio.getText()),
                DateConverter.inputToTimestamp(tf_dataFinal.getValue(), tf_horaFinal.getText()),
                resultCenter_id,
                cliente_id,
                tf_projeto.getText(),
                tf_justificativa.getText()
            ));
            System.out.println("apontamento lançado com id " + appointmentId);
        } catch (Exception e) {
            System.out.println("Erro: Appointments.inputAppointment() -- Falha ao inserir apontamento");
            e.printStackTrace();
        }
        updateTable();
    }

    // função usada para exibir um pop up, que deve corresponder ao fxml de nome fileName
    void popUp(String fileName){
        try{

            Stage stage;
            Parent root;

            stage = new Stage();

            root = FXMLLoader.load(App.getFXML(fileName));
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(tabela.getScene().getWindow());
            stage.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    




}