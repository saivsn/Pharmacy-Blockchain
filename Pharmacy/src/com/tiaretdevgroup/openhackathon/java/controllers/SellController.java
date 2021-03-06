package com.tiaretdevgroup.openhackathon.java.controllers;

import blockchain.factory.BlockchainFactory;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXTextField;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.tiaretdevgroup.openhackathon.java.blockchain.chains.MaladyBlockChain;
import com.tiaretdevgroup.openhackathon.java.blockchain.chains.SalesBlockChain;
import com.tiaretdevgroup.openhackathon.java.utils.Constants;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;


public class SellController implements Initializable {

    // Medicaments
    private List<String[]> products;

    @FXML
    private HBox checkPane, sellProductPane;

    @FXML
    private VBox rightCheckPane, rightSellPane;

    @FXML
    private JFXTextField identifierField;
    private JFXSnackbar toastErrorMsgCheckPane, toastErrorMsgProductPane;

    /* Start Sell Pane */

    @FXML
    private JFXComboBox<String> comboMedicament;
    @FXML
    private Label identifierLbl, firstNameLbl, lastNameLbl, typeOfDiseaseLbl;

    /* End Sell Pane */

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        toastErrorMsgCheckPane = new JFXSnackbar(rightCheckPane);
        toastErrorMsgProductPane = new JFXSnackbar(rightSellPane);

    }

    @FXML
    public void onCheck() {


        MaladyBlockChain maladyBlockChain = BlockchainFactory.INSTANCE.
                readMaladyBlockChainFromJSONFile();


        if (maladyBlockChain.isPatientAMalady(identifierField.getText().trim())) {

            // receive JSON & inserted in views
            getJSONMedicament();
            getJSONProfile();

            checkPane.setVisible(false);
            sellProductPane.setVisible(true);
        } else {
            toastErrorMsgCheckPane.show("You are not sick !", 2000);
        }

        initMedicament();
    }

    @FXML
    private void onSell() {
        if (comboMedicament.getSelectionModel().getSelectedItem() == null) {
            toastErrorMsgProductPane.show("Please Select Medicament !", 2000);
            return;
        }

        String medicamentSelected = comboMedicament.getSelectionModel().getSelectedItem();
        String codeSelected = null;

        for (String[] item : products) {
            if (item[0].equals(medicamentSelected)) {
                codeSelected = item[1];
                break;
            }
        }

        SalesBlockChain salesBlockChain = BlockchainFactory.INSTANCE.readSalesBlockChainFromJSONFile();
        JSONObject json = null;
        try {
            json = new JSONObject(new String(Files.readAllBytes(Paths.get(Constants.FILE_TOKEN))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert json != null;
        int idPharmacy = json.getInt("id");
        Date last = salesBlockChain.lastSales(identifierLbl.getText().trim(), codeSelected);
        if (last == null || isThreeMonth(last, new Date())) {
            salesBlockChain.addBlock(identifierLbl.getText().trim(), codeSelected, String.valueOf(idPharmacy));
            BlockchainFactory.INSTANCE.saveBlockChainToJSONFile(salesBlockChain, Constants.FILE_SALES);
            Notifications notification = Notifications.create()
                        .title("You Successfuly Sell Product !")
                        .graphic(new ImageView(new Image("/com/tiaretdevgroup/openhackathon/resources/images/icons/valid.png")))
                        .hideAfter(Duration.millis(2000))
                        .position(Pos.BOTTOM_RIGHT);
                notification.darkStyle();
                notification.show();
        } else {
            toastErrorMsgProductPane.show("You bought recently the same product", 2000);
        }
    }

    private boolean isThreeMonth(Date last, Date date) {
        return date.getTime() - last.getTime() > 7776000000L;
    }

    private void initMedicament() {

    }

    private void getJSONMedicament() {
        String urls = Constants.CLIENT + "/" + identifierField.getText().trim() + "/product";

        String productsJSON = null;
        try {
            HttpResponse<JsonNode> node = Unirest.get(urls).asJson();
            productsJSON = node.getBody().toString();
        } catch (UnirestException e) {
            e.printStackTrace();
        }

        JSONObject obj = new JSONObject(productsJSON);
        JSONArray arrayData = obj.getJSONArray("data");
        products = new ArrayList<>();
        comboMedicament.getItems().clear();

        for (int i = 0; i < arrayData.length(); i++) {
            JSONObject object = arrayData.getJSONObject(i);

            String[] product = {object.getString("name"), object.getString("code")};
            comboMedicament.getItems().add(product[0]);

            products.add(product);
        }
    }

    private void getJSONProfile() {
        String urls = Constants.CLIENT + "/" + identifierField.getText().trim();

        String productsJSON = null;
        try {
            HttpResponse<JsonNode> node = Unirest.get(urls).asJson();
            productsJSON = node.getBody().toString();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        JSONObject obj = new JSONObject(productsJSON);
        identifierLbl.setText(obj.getString("identifier"));
        firstNameLbl.setText(obj.getString("firstname"));
        lastNameLbl.setText(obj.getString("lastname"));
        typeOfDiseaseLbl.setText(obj.getString("firstname"));
    }

}


