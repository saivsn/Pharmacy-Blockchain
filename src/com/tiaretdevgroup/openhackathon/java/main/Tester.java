package com.tiaretdevgroup.openhackathon.java.main;

import blockchain.chains.SalesBlockChain;
import blockchain.factory.BlockchainFactory;
import blockchain.models.Sale;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.tiaretdevgroup.openhackathon.java.utils.Constants;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tester {


    public static void main(String[] args) {
        SalesBlockChain chain = BlockchainFactory.INSTANCE.readSalesBlockChainFromJSONFile();

        List<Sale> sales = chain.getSales();
        JSONArray array = new JSONArray();
        for (Sale sale : sales) {
            JSONObject object = new JSONObject();
            object.put("client_id", sale.getIdPatient())
                    .put("product_id", sale.getProductId());
            array.put(object);
        }


        try {
            String url = Constants.SALES;

            HttpResponse<JsonNode> call = Unirest.post(url).field("q",array.toString())
                    .asJson();


            System.out.println(call.getBody().toString());
        } catch (UnirestException e) {
            e.printStackTrace();
        }

    }

}
