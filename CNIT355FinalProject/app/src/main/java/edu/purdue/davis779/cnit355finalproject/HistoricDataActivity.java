package edu.purdue.davis779.cnit355finalproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Calendar;

public class HistoricDataActivity extends AppCompatActivity implements Runnable {
    String coin;
    Thread thread;
    String msg;
    GraphView graph;
    int size = -9;

    private static HistoricDataActivity parent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historic_data);

        parent = this;

        Intent intent = getIntent();
        coin = intent.getExtras().getString("Coin");

        TextView coinName = findViewById(R.id.txtCur);
        coinName.setText(coin);

        refresh();

        graph = (GraphView) findViewById(R.id.graph);

        SeekBar seekbar = findViewById(R.id.seekBar);
        seekbar.setMax(365);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                size = progress*-1;
                refresh();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void endActivity(View view){
        finish();
    }

    public void refresh(View view){
        thread = new Thread(this);
        thread.start();
    }

    private void refresh(){
        thread = new Thread(this);
        thread.start();
    }

    //Reference: https://stackoverflow.com/questions/3505930/make-an-http-request-with-android
    //Gets JSON object of cryptocurrency data
    public void run() {
        HttpURLConnection urlConnection = null;
        URL url = null;
        JSONArray object = null;
        InputStream inStream = null;

        try {
            url = new URL("https://api.nomics.com/v1/markets/prices?key=1c5ffff4fc72b2138210c4a9ea578dc4&currency=" + coin);
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

            ArrayList<Double> Prices = new ArrayList<Double>();
            ArrayList<String> Exchanges = new ArrayList<String>();

            for (int count = 0; count < object.length(); count++){
                String currencyConversion = object.getJSONObject(count).getString("quote").toString();
                if (currencyConversion.equals("USD")) {
                    String price = object.getJSONObject(count).getString("price");
                    String exchange = object.getJSONObject(count).getString("exchange");
                    Prices.add(Double.parseDouble(price));
                    Exchanges.add(exchange);
                }
            }

            double min = Collections.min(Prices).doubleValue();
            double max = Collections.max(Prices).doubleValue();

            String strMin = Collections.min(Prices).toString();
            String strMax = Collections.max(Prices).toString();

            String minExchange = Exchanges.get(Prices.indexOf(min));
            String maxExchange = Exchanges.get(Prices.indexOf(max));

            TextView txtMinPrice = findViewById(R.id.txtMinPrice);
            txtMinPrice.setText(strMin);

            TextView txtMaxPrice = findViewById(R.id.txtMaxPrice);
            txtMaxPrice.setText(strMax);

            TextView txtMinEx = findViewById(R.id.txtMinEx);
            txtMinEx.setText(minExchange);

            TextView txtMaxEx = findViewById(R.id.txtMaxEx);
            txtMaxEx.setText(maxExchange);
        } catch (Exception e) {
            msg = e.getMessage();

            //Reference: https://stackoverflow.com/questions/17379002/java-lang-runtimeexception-cant-create-handler-inside-thread-that-has-not-call
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

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd'T'HH:MM:ss");
            Calendar calendar = Calendar.getInstance();
            String end = dateFormat.format(calendar.getTime()) + "Z";
            calendar.add(Calendar.DAY_OF_YEAR, size);
            String start = dateFormat.format(calendar.getTime()) + "Z";
            //start = "2018-12-08T00:00:00Z";

            url = new URL("https://api.nomics.com/v1/candles?key=1c5ffff4fc72b2138210c4a9ea578dc4&currency=" + coin + "&start=" + start + "&end=" + end);
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

            //double min = Collections.min(Prices).doubleValue();
            //double max = Collections.max(Prices).doubleValue();

            DataPoint[] datapoints = new DataPoint[object.length()];

            for (int count = 0; count < object.length(); count++){
                String low = object.getJSONObject(count).getString("low").toString();
                Double dPrice = Double.parseDouble(low);
                datapoints[count] = new DataPoint(count, dPrice);
            }
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(datapoints);
            graph.removeAllSeries();
            graph.addSeries(series);

            for (int count = 0; count < object.length(); count++){
                String high = object.getJSONObject(count).getString("high").toString();
                Double dPrice = Double.parseDouble(high);
                datapoints[count] = new DataPoint(count, dPrice);
            }

            series = new LineGraphSeries<>(datapoints);
            graph.addSeries(series);

            msg = start;
            parent.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(parent.getBaseContext(), msg, Toast.LENGTH_LONG).show();
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
