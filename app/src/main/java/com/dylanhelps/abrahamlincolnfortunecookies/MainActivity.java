package com.dylanhelps.abrahamlincolnfortunecookies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private Button nextCookie;
    private final String TAG = this.getClass().getName();
    private final static String WEB_URL = "http://www.quotes.net/authors/Abraham+Lincoln";
    private FloatingActionButton fab , sms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textView= (TextView) findViewById(R.id.displayCookie);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                emailIntent.setType("vnd.android.cursor.item/email");
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Abraham Lincoln Says");
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, textView.getText());
                startActivity(Intent.createChooser(emailIntent, "Send mail using..."));
            }
        });

        sms = (FloatingActionButton) findViewById(R.id.sms);
        sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("sms:"));
                sendIntent.putExtra("sms_body",textView.getText());
                startActivity(sendIntent);
            }
        });

        nextCookie = (Button) findViewById(R.id.nextCookie);
        nextCookie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.setVisibility(View.VISIBLE);
                sms.setVisibility(View.VISIBLE);
                nextCookie.setEnabled(false);
                getCookie(view);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getCookie(View v){
        SendfeedbackJob job = new SendfeedbackJob();
        job.execute(null,null);
    }

    private String getCookie() {
        //Elements elements;
        final CountDownLatch latch = new CountDownLatch(1);
        final List<String> value = new ArrayList<>();

        Thread thread = new Thread(new Runnable() {
            Elements elements;
            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect(WEB_URL).get();

                    elements = doc.select("div.author-quote");
                    value.add(elements.text());
                    latch.countDown();
                    } catch (IOException e) {

                    setPowerball();
                    Log.e(TAG,e.getMessage());
                }
                catch (NetworkOnMainThreadException nomte){
                    setPowerball();
                    Log.e(TAG,nomte.getMessage());
                }

            }// end run
        });

        thread.start();
        try {
            latch.await();
        } catch (InterruptedException ie) {
            setPowerball();
            Log.e(TAG,ie.getMessage());
        }
        return value.get(0);
    }

    private class SendfeedbackJob extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {
                return getCookie();
        }

        @Override
        protected void onPostExecute(String message) {
            char[] chars = message.toCharArray();
            StringBuilder sb = new StringBuilder(chars[0]);
            List<String> listOfQuotes = new ArrayList<>();
            for (int i=1;i<chars.length; i++){
                sb.append(chars[i]);
                if(chars[i]=='.' ||chars[i]=='!' ||chars[i]=='?'){
                    listOfQuotes.add(sb.toString());
                    sb = new StringBuilder();
                }
            }
            textView.setText(listOfQuotes.get(new Random().nextInt(listOfQuotes.size()-1))+" - Abraham Lincoln");
            nextCookie.setEnabled(true);
        }
    }

    private void setPowerball() {
        Random rand = new Random(98);
        int[] ints = new int[8];
        StringBuilder sb = new StringBuilder("Today's lucky number");
        for(int i=0; i<ints.length; i++){
            ints[i] = rand.nextInt();
            sb.append(ints[i]+", ");
            if (i==ints.length-1){
                ints[i] = rand.nextInt();
                sb.append("Powerball: "+ints[i]);
            }
        } // end for
        textView.setText(sb.toString());
    }
}
