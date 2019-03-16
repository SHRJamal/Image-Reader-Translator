package com.asisdroid.oneindialanguage;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.view.ViewCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.leptonica.android.GrayQuant;
import com.googlecode.leptonica.android.Pix;


public class Binarization extends AppCompatActivity implements View.OnClickListener, AppCompatSeekBar.OnSeekBarChangeListener {
    private ImageView img;
    private Toolbar toolbar;
    private AppCompatSeekBar seekBar;
    private Pix pix;
    private FloatingActionButton fab;
    public static Bitmap umbralization;
    private Spinner spinner;
    public static int language;
    String[] languageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.binarization);
        /*toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ViewCompat.setElevation(toolbar,10);*/
        ViewCompat.setElevation(findViewById(R.id.extension),10);
        spinner = findViewById(R.id.language);

        img = findViewById(R.id.croppedImage);
        fab = findViewById(R.id.nextStep);
        fab.setOnClickListener(this);
        pix = com.googlecode.leptonica.android.ReadFile.readBitmap(CropAndRotate.croppedImage);

        languageList = getBaseContext().getResources().getStringArray(R.array.languages_array);
        /*languageList.add("English");
        languageList.add("Gujrati");
        languageList.add("Hindi");
        languageList.add("Kannada");
        languageList.add("Malayali");
        languageList.add("Odia");
        languageList.add("Punjabi");
        languageList.add("Tamil");
        languageList.add("Telugu");*/

        final ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, languageList);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(view!=null) {
                    ((TextView) view).setTextColor(Color.WHITE);
                }
                language = i;
                OneIndiaLanguagePreferences.getInstance(getBaseContext()).setFromLanguage(language);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinner.setSelection(OneIndiaLanguagePreferences.getInstance(getBaseContext()).getFromLanguage());
        try {
            OtsuThresholder otsuThresholder = new OtsuThresholder();
            int threshold = otsuThresholder.doThreshold(pix.getData());
                /* increase threshold because is better*/
            threshold += 20;
            umbralization = com.googlecode.leptonica.android.WriteFile.writeBitmap(GrayQuant.pixThresholdToBinary(pix, threshold));
            img.setImageBitmap(umbralization);
            seekBar = findViewById(R.id.umbralization);
            seekBar.setProgress((50 * threshold) / 254);
            seekBar.setOnSeekBarChangeListener(this);
        }
        catch(Exception e){
            Toast.makeText(this, "Please, select correct language of the image!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

        umbralization = com.googlecode.leptonica.android.WriteFile.writeBitmap(
                GrayQuant.pixThresholdToBinary(pix, ((254 * seekBar.getProgress()) / 50))
        );
        img.setImageBitmap(umbralization);

    }


    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.nextStep) {
            if(checkInternetConenction()) {
                Intent intent = new Intent(Binarization.this, Recognizer.class);
                startActivity(intent);
            }
            else{
                Toast.makeText(this, "Cannot proceed without internet connection!", Toast.LENGTH_LONG).show();
            }
        }

    }

    private boolean checkInternetConenction() {
        // get Connectivity Manager object to check connection
        getBaseContext();
        ConnectivityManager connec
                =(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);

        //NetworkInfo info = connec.getActiveNetworkInfo();

        // Check for network connections
        // Only update if WiFi or 3G is connected and not roaming
        return connec.getNetworkInfo(0).isConnected()||
                connec.getNetworkInfo(1).isConnected();
    }
}
