package org.openjfx.api2semestre.view_controllers;

import org.openjfx.api2semestre.App;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;


public class ViewButtonController {
    @FXML
    private Button button;
    
    private String viewFxmlFile;

    public void setView (String viewFxmlFile) {
        this.viewFxmlFile = viewFxmlFile;
    }

    public void setText (String text) {
        button.setText(text);
    }

    @FXML
    void enterView(ActionEvent event) {
        App.changeView(viewFxmlFile);
    }

}