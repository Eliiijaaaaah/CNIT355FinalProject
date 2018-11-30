package edu.purdue.davis779.cnit355finalproject;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LiveDataActivity extends AppCompatActivity implements Runnable {
    String[] Coins = {"BTC", "BCH", "LTC"};
    String[] Exchanges = {"Coinbase", "Kraken"};
    Thread thread;
    Context context;
    String msg;

    private static LiveDataActivity parent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_data);
        parent = this;
    }

    private double getArbitragePotential(String Coin){
        double percentage = 0;

        return percentage;
    }

    //api key: 1c5ffff4fc72b2138210c4a9ea578dc4
    //api markets/prices: https://api.nomics.com/v1/markets/prices?key=1c5ffff4fc72b2138210c4a9ea578dc4&currency=BTC
    public void getExchangeCoinPrice(View view){
        thread = new Thread(this);
        thread.start();
    }

    public void run() {
        HttpURLConnection urlConnection = null;
        URL url = null;
        JSONArray object = null;
        InputStream inStream = null;
        try {
            url = new URL("https://api.nomics.com/v1/markets/prices?key=1c5ffff4fc72b2138210c4a9ea578dc4&currency=BTC");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.connect();
            inStream = urlConnection.getInputStream();
            BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
            String temp, response = "";
            while ((temp = bReader.readLine()) != null) {
                response += temp;
            }
            object = (JSONArray) new JSONTokener(response).nextValue();
            msg = object.getJSONObject(0).getString("price");
            parent.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(parent.getBaseContext(), msg, Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            msg = e.getMessage();
            parent.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(parent.getBaseContext(), msg, Toast.LENGTH_LONG).show();
                }
            });
        } finally {
            if (inStream != null) {
                try {
                    // this will close the bReader as well
                    inStream.close();
                } catch (IOException ignored) {
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
