package edu.purdue.davis779.cnit355finalproject;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TableLayout;
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
import java.util.ArrayList;
import java.util.Collections;

public class LiveDataActivity extends AppCompatActivity implements Runnable {
    String[] Coins = {"BTC", "LTC", "ETH"};
    ArrayList<String> percentGain = new ArrayList<>();
    TableLayout liveDataTable;
    View tr;
    String percentString;
    String coin;
    String price;
    Thread thread;
    String msg;

    private static LiveDataActivity parent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_data);
        parent = this;
        getExchangeCoinPrice();
    }

    //api key: 1c5ffff4fc72b2138210c4a9ea578dc4
    //api markets/prices: https://api.nomics.com/v1/markets/prices?key=1c5ffff4fc72b2138210c4a9ea578dc4&currency=BTC
    public void getExchangeCoinPrice(){
        thread = new Thread(this);
        thread.start();
    }

    private View.OnClickListener historicDataButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //TODO: Intent for HistoricDataActivity
        }
    };

    private void inflateLiveDataTable(int count){
        //This section will populate the live data table
            //Toast.makeText(parent.getBaseContext(), "hit", Toast.LENGTH_LONG).show();

            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            tr = inflater.inflate(R.layout.row, null);
            liveDataTable = findViewById(R.id.coinTable);

            TextView coinNameText = tr.findViewById(R.id.coinNameText);
            coinNameText.setText(coin);

            TextView coinPriceText = tr.findViewById(R.id.coinPriceText);
            //String percent = Integer.toString(percentGain.size());
            //Toast.makeText(parent.getBaseContext(), percent, Toast.LENGTH_LONG).show();
            coinPriceText.setText(percentGain.get(count));

            Button button = tr.findViewById(R.id.button2);
            button.setOnClickListener(historicDataButton);

            //Reference: https://stackoverflow.com/questions/17379002/java-lang-runtimeexception-cant-create-handler-inside-thread-that-has-not-call
            parent.runOnUiThread(new Runnable() {
                public void run() {
                    liveDataTable.addView(tr);
                }
            });
    }

    //Reference: https://stackoverflow.com/questions/3505930/make-an-http-request-with-android
    //Gets JSON object of cryptocurrency data
    public void run() {
        HttpURLConnection urlConnection = null;
        URL url = null;
        JSONArray object = null;
        InputStream inStream = null;
        int counter = 0;

        for (String Coin : Coins){
            try {
                url = new URL("https://api.nomics.com/v1/markets/prices?key=1c5ffff4fc72b2138210c4a9ea578dc4&currency=" + Coin);
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

                coin = Coin;
                ArrayList<Double> Prices = new ArrayList<Double>();

                for (int count = 0; count < object.length(); count++){
                    price = object.getJSONObject(count).getString("price");
                    Prices.add(Double.parseDouble(price));
                }

                double min = Collections.min(Prices).doubleValue();
                double max = Collections.max(Prices).doubleValue();
                percentString = Double.toString(max/min).toString();
                percentGain.add(percentString);

                msg = Integer.toString(percentGain.size());

                //Reference: https://stackoverflow.com/questions/17379002/java-lang-runtimeexception-cant-create-handler-inside-thread-that-has-not-call
                parent.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(parent.getBaseContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                msg = e.getMessage();

                //Reference: https://stackoverflow.com/questions/17379002/java-lang-runtimeexception-cant-create-handler-inside-thread-that-has-not-call
                parent.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(parent.getBaseContext(), msg, Toast.LENGTH_LONG).show();
                    }
                });
            } finally {
                inflateLiveDataTable(counter);
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
            counter++;
        }
    }
}
