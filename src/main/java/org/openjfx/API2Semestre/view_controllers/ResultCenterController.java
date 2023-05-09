package org.openjfx.api2semestre.view_controllers;

import org.openjfx.api2semestre.custom_tags.LookupTextField;
import org.openjfx.api2semestre.data.ResultCenter;
import org.openjfx.api2semestre.database.QueryLibs;

import java.util.Arrays;
import java.util.LinkedList;

import org.openjfx.api2semestre.authentication.User;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

public class ResultCenterController {

    @FXML private TextField tf_codigo;
    @FXML private TextField tf_colaborador;
    @FXML private TextField tf_gestor;
    @FXML private TextField tf_name;
    @FXML private TextField tf_sigla;

    @FXML private FlowPane fp_colaboradores;
    private LinkedList<User> selectedUsers = new LinkedList<User>();
    
    @FXML private TableView<ResultCenter> tabela;
    @FXML private TableColumn<ResultCenter, Integer> col_id;
    @FXML private TableColumn<ResultCenter, String> col_nome;
    @FXML private TableColumn<ResultCenter, String> col_sigla;
    @FXML private TableColumn<ResultCenter, String> col_codigo;
    @FXML private TableColumn<ResultCenter, User> col_gestor;
    @FXML private TableColumn<ResultCenter, Integer> col_membros;

    public void initialize(){
        System.out.println("ResultCenterController initialize");
        // colaborators = FXCollections.observableArrayList(QueryLibs.executeSelect(
        //     User.class,
        //     new QueryParam<?>[0]
        // ));
        // User[] users = new User[] {
        //     new User("Fulano colaborador 0", Profile.Colaborador, List.of("oi", "oi2"), List.of()),
        //     new User("Fulano gestor 0", Profile.Gestor, List.of("oi3"), List.of("oi", "oi2")),
        //     new User("Cicrano col 0", Profile.Colaborador, List.of(), List.of("oi3")),
        //     new User("Cicrano ges 0", Profile.Gestor, List.of(), List.of("oi3")),
        //     new User("Fulano colaborador 1", Profile.Colaborador, List.of("oi", "oi2"), List.of()),
        //     new User("Fulano gestor 1", Profile.Gestor, List.of("oi3"), List.of("oi", "oi2")),
        //     new User("Cicrano col 1", Profile.Colaborador, List.of(), List.of("oi3")),
        //     new User("Cicrano ges 2", Profile.Gestor, List.of(), List.of("oi3")),
        //     new User("Fulano colaborador 2", Profile.Colaborador, List.of("oi", "oi2"), List.of()),
        //     new User("Fulano gestor 2", Profile.Gestor, List.of("oi3"), List.of("oi", "oi2")),
        //     new User("Cicrano col 3", Profile.Colaborador, List.of(), List.of("oi3")),
        //     new User("Cicrano ges 3", Profile.Gestor, List.of(), List.of("oi3")),
        //     new User("Fulano colaborador 3", Profile.Colaborador, List.of("oi", "oi2"), List.of()),
        //     new User("Fulano gestor 4", Profile.Gestor, List.of("oi3"), List.of("oi", "oi2")),
        //     new User("Cicrano col 4", Profile.Colaborador, List.of(), List.of("oi3")),
        //     new User("Cicrano ges 4", Profile.Gestor, List.of(), List.of("oi3")),
        //     new User("Fulano colaborador", Profile.Colaborador, List.of("oi", "oi2"), List.of()),
        //     new User("Fulano gestor", Profile.Gestor, List.of("oi3"), List.of("oi", "oi2")),
        //     new User("Cicrano adm", Profile.Administrator, List.of(), List.of("oi3"))
        // };

        User[] users = QueryLibs.selectAllUsers();

        // tf_colaborador = AutocompleteTextField.setupAutocomplete(tf_colaborador, ((HBox)tf_colaborador.getParent()), users);
        LookupTextField lookupTextFieldColaborador = new LookupTextField(users);
        ((HBox)tf_colaborador.getParent()).getChildren().set(
            ((HBox)tf_colaborador.getParent()).getChildren().indexOf(tf_colaborador),
            lookupTextFieldColaborador
        );
        tf_colaborador = lookupTextFieldColaborador;

        LookupTextField lookupTextFieldGestor = new LookupTextField(

            // Filter users to only show level 1 (gestor) and 2 (adm) profile for the tf_gestor field
            (User[])Arrays.stream(users).filter((User user) -> user.getPerfil().getProfileLevel() > 0).toArray(User[]::new)
        );
        ((HBox)tf_gestor.getParent()).getChildren().set(
            ((HBox)tf_gestor.getParent()).getChildren().indexOf(tf_gestor),
            lookupTextFieldGestor
        );
        lookupTextFieldGestor.selectedUserProperty().addListener(new ChangeListener<User>() {
            @Override
            public void changed(ObservableValue<? extends User> arg0, User oldPropertyValue, User newPropertyValue) {
                if (oldPropertyValue != null) lookupTextFieldColaborador.addSuggestion(oldPropertyValue);
                if (newPropertyValue != null) lookupTextFieldColaborador.removeSuggestion(newPropertyValue);
            }
        });
        tf_gestor = lookupTextFieldGestor;
    }


    @FXML
    void adicionarColaborador(ActionEvent event) {

        LookupTextField lookupTfColaborador = ((LookupTextField)tf_colaborador);
        LookupTextField lookupTfGestor = ((LookupTextField)tf_gestor);

        User selectedUser = lookupTfColaborador.getSelectedUser();

        if (selectedUser == null) return;

        System.out.println(selectedUser.getNome());
        
        lookupTfGestor.removeSuggestion(selectedUser);
        lookupTfColaborador.removeSuggestion(selectedUser);
        lookupTfColaborador.clear();

        Label userLabel = new Label(selectedUser.getNome());
        HBox.setMargin(userLabel, new Insets(0, 0, 0, 0));
        userLabel.setPadding(new Insets(0));
        userLabel.setFont(Font.font("Arial", 12));

        Button userRemoveButton = new Button("X");
        HBox.setMargin(userRemoveButton, new Insets(0, 0, 0, 4));
        userRemoveButton.setPadding(new Insets(0, 4, 0, 4));
        
        userRemoveButton.setMinHeight(18);
        userRemoveButton.setMaxHeight(18);
        userRemoveButton.setPrefHeight(18);
        userRemoveButton.setAlignment(Pos.CENTER);

        HBox userContainer = new HBox(userLabel, userRemoveButton);
        userContainer.setPadding(new Insets(2));

        userContainer.setAlignment(Pos.CENTER_LEFT);
        fp_colaboradores.getChildren().add(userContainer);
        selectedUsers.add(selectedUser);

        userRemoveButton.setOnAction(e -> {
            fp_colaboradores.getChildren().remove(userContainer);
            lookupTfColaborador.addSuggestion(selectedUser);
            lookupTfGestor.addSuggestion(selectedUser);
            selectedUsers.remove(selectedUser);
        });
        
    }

    @FXML void cadastrarCentro (ActionEvent event) {
        // System.out.println("todo!()");

        LookupTextField lookupTfcolaborador = ((LookupTextField)tf_colaborador);
        LookupTextField lookupTfgestor = ((LookupTextField)tf_gestor);

        User gestor = lookupTfgestor.getSelectedUser();

        if (gestor == null) {
            System.out.println("Khali | ResultCenterController.cadastrarCentro() -- Erro: Gestor não informado!");
            return;
        }

        int cr_id = QueryLibs.insertResultCenter(new ResultCenter(
            tf_name.getText(),
            tf_sigla.getText(),
            tf_codigo.getText(),
            gestor.getId()
        ));
        
        for (User selectedUser : selectedUsers) {
            QueryLibs.addUserToResultCenter(selectedUser.getId(), cr_id);
        }

        tf_name.clear();
        tf_sigla.clear();
        tf_codigo.clear();

        lookupTfcolaborador.clear();
        lookupTfgestor.clear();

        fp_colaboradores.getChildren().clear();
        
    } 

}
