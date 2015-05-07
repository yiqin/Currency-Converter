package com.apress.gerber.currencies;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;


public class MainActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener {

    //define members that correspond to Views in our layout
    private Button mCalcButton;
    private TextView mConvertedTextView;
    private EditText mAmountEditText;
    private Spinner mForSpinner, mHomSpinner;
    private String[] mCurrencies;


    public static final String FOR = "FOR";
    public static final String Hom = "HOM";



    private String mKey;
    public static final String RATES = "rates";
    public static final String URL_BASE = "http://openexchangerates.org/api/latest.json?app_id=";


    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00000");




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //unpack ArrayList from the bundle and convert to array
        ArrayList<String> arrayList = ((ArrayList<String>)
                getIntent().getSerializableExtra(SplashActivity.KEY_ARRAYLIST));
        //if there is not an intent from SplashActivity, then it's null.

        //sort the arrayList...
        Collections.sort(arrayList);
        mCurrencies = arrayList.toArray(new String[arrayList.size()]);


        //assign references to our Views
        mConvertedTextView = (TextView) findViewById(R.id.txt_converted);
        mAmountEditText = (EditText) findViewById(R.id.edt_amount);
        mCalcButton = (Button) findViewById(R.id.btn_calc);
        mForSpinner = (Spinner) findViewById(R.id.spn_for);
        mHomSpinner = (Spinner) findViewById(R.id.spn_hom);


        //controller: mediates model and view
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(

                //context
                this,
                //view: layout you see when the spinner is closed
                R.layout.spinner_closed,
                //model: the array of Strings
                mCurrencies
        );

        //view: layout you see when the spinner is open
        arrayAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);



        //assign adapters to spinners
        mHomSpinner.setAdapter(arrayAdapter);
        mForSpinner.setAdapter(arrayAdapter);



        mHomSpinner.setOnItemSelectedListener(this);
        mForSpinner.setOnItemSelectedListener(this);


        // Something is required here
        mCalcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo for later

            }
        });

        mKey = getKey("open_key");



    }//end onCreate()



    private String getKey(String strKey) throws IOException {
        AssetManager assetManager = this.getResources().getAssets();
        Properties properties = new Properties();

        //we need to check everytime we use input stream
        try {
            InputStream inputStream = assetManager.open("open_key");
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.getProperty(strKey);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id){

            case R.id.mnu_invert:
                invertCurrencies();
                break;

            case R.id.mnu_codes:
                launchBrowser(SplashActivity.URL_CODES);
                break;

            case R.id.mnu_exit:
                finish();
                break;
        }

        return true;
    }



    //android stack
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }


    private void launchBrowser(String strUri) {
        if (isOnline()) {
            Uri uri = Uri.parse(strUri);
            //call an implicit intent
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }


    private void invertCurrencies() {
        int nFor = mForSpinner.getSelectedItemPosition();
        int nHom = mHomSpinner.getSelectedItemPosition();
        mForSpinner.setSelection(nHom);
        mHomSpinner.setSelection(nFor);
        mConvertedTextView.setText("");
    }




    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (parent.getId()) {

            case R.id.spn_for:
                //TODO define behavior here
                // PrefsMgr.setString(this, FOR, extractCodeFromCurency((String)mForSpinner.getSelectedItem());
                break;

            case R.id.spn_hom:
                //TODO define behavior here
                // PrefsMgr.setString(this, HOM, extractCodeFromCurency((String)mHomSpinner.getSelectedItem());
                break;

            default:
                break;

        }
        mConvertedTextView.setText("");

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //stb - never called


    }

    private class CurrencyConverterTask extends AsyncTask<String, Void, JSONObject>{

        private ProgressDialog progressDialog;

        //1
        @Override
        protected void onPreExecute() {
            // super.onPreExecute();


            progressDialog = new ProgressDialog(MainActivity.this);
            // We need more here....

            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener(){

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    CurrencyConverterTask.this.cancel(true);
                    progressDialog.dismiss();

                }
            });



            progressDialog.show();
        }

        //2
        @Override
        protected JSONObject doInBackground(String... params) {
            return new JSONParser().getJSONFromUrl(params[0]);
        }

        //3
        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            // super.onPostExecute(jsonObject);

            double dCalculated = 0.0;
            // String strForCode = // extractCodeFromCurrency()......


            String strAmount = mAmountEditText.getText().toString();

            try {

                if (jsonObject == null){
                    throw new JSONException("no data available");
                }



            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        

    }


}
