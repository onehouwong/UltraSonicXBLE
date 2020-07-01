package com.seclab.signal;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.PermissionChecker;

import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.bluetooth.le.BluetoothLeAdvertiser.*;
import android.widget.Button;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {


    public static String TAG = "SignalTest";

    public BluetoothLeAdvertiser advertiser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        int permission;
        if (Build.VERSION.SDK_INT < 26)
            permission = PermissionChecker.checkSelfPermission(this.getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        else {
            if (this.checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {

                this.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                        123);
            }
        }


        Button startBtn = findViewById(R.id.start);
        Button stopBtn = findViewById(R.id.stop);


        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start BLE advertising
                advertise();
            }
        });


        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            @TargetApi(26)
            public void onClick(View v) {
                Log.i(TAG, "Stop Advertising!");
                advertiser.stopAdvertisingSet(new AdvertisingSetCallback() {
                    @Override
                    public void onAdvertisingSetStarted(AdvertisingSet advertisingSet, int txPower, int status) {
                        Log.i(TAG, "onAdvertisingSetStarted");
                        super.onAdvertisingSetStarted(advertisingSet, txPower, status);
                    }

                    @Override
                    public void onAdvertisingEnabled(AdvertisingSet advertisingSet, boolean enable, int status) {
                        Log.i(TAG, "onAdvertisingEnabled");
                        super.onAdvertisingEnabled(advertisingSet, enable, status);
                    }
                });
            }
        });


        Button startSonarBtn = findViewById(R.id.startSonar);
        Button readSonarBtn = findViewById(R.id.readSonar);

        startSonarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSonar();
            }
        });


        readSonarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startReceiveSonar();
            }
        });


    }


    public void advertise() {



        AdvertiseSettings settings = new AdvertiseSettings.Builder()
        .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
        .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
        .setConnectable(false)
        .build();

        ParcelUuid pUuid = new ParcelUuid( UUID.fromString("5921174c-bb48-11ea-b3de-0242ac130004"));


        boolean includeDeviceName = true;

        boolean includeTxPower = false;

        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName( includeDeviceName )
                .setIncludeTxPowerLevel(includeTxPower )
                .addServiceUuid( pUuid )
//                .addServiceData( pUuid, "Data".getBytes(Charset.forName("UTF-8") ) )
                .build();


        AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                Log.i("BLE", "LE Advertise success.");

            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.e("BLE", "Advertising onStartFailure: " + errorCode);
                super.onStartFailure(errorCode);
            }
        };

        advertiser.startAdvertising(settings, advertiseData, advertisingCallback);

        Log.i(TAG, "Advertising started");
    }


    @TargetApi(26)
    public void advertiseCustom() {


        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        adapter.setName("Test Signal");
        advertiser = adapter.getBluetoothLeAdvertiser();



        // int txpower = -15;   // low
        // int txpower = -7;    // medium
        int txpower = -28;      // very low

        /* Part 1 -----------------Broadcast Settings-----------------  */

        // set up parameters
        AdvertisingSetParameters.Builder parameters = new AdvertisingSetParameters.Builder();

        // legacy mode
        parameters.setLegacyMode(true);

        // connectable or not
        parameters.setConnectable(false);

        // scannable or not
        parameters.setScannable(true);

        // broadcast interval
        parameters.setInterval(1600); // 1s

        // power level
        parameters.setTxPowerLevel(txpower);

        // broadcast duration
        int duration = 0; // 0 for non-stop advertising



        /* Part 2 -----------------Broadcast Data-----------------  */

        boolean includeDeviceName = true;

        boolean includeTxPower = false;

        ParcelUuid pUuid = new ParcelUuid( UUID.fromString("b161c53c-0715-11e6-b512-3e1d05defe78"));


        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName( includeDeviceName )
                .setIncludeTxPowerLevel(includeTxPower )
//                .addServiceUuid( pUuid )
//                .addServiceData( pUuid, "Data".getBytes(Charset.forName("UTF-8") ) )
                .build();


        AdvertiseData scanResponse = new AdvertiseData.Builder()
                .setIncludeDeviceName(includeDeviceName)
                .build();



        /* Part 3 -----------------Callback-----------------  */

        AdvertisingSetCallback callback = new AdvertisingSetCallback() {
            @Override
            public void onAdvertisingSetStarted(AdvertisingSet advertisingSet, int txPower, int status) {
                Log.i(TAG, "onAdvertisingSetStarted");
                super.onAdvertisingSetStarted(advertisingSet, txPower, status);
            }

            @Override
            public void onAdvertisingEnabled(AdvertisingSet advertisingSet, boolean enable, int status) {
                Log.i(TAG, "onAdvertisingEnabled");
                super.onAdvertisingEnabled(advertisingSet, enable, status);
            }
        };



        advertiser.startAdvertisingSet(parameters.build(), advertiseData, scanResponse, null,
                null, duration, 0, callback);


        Log.i(TAG, "Custom Advertising started");

    }


    private final Handler mHandler = new Handler();
    private Runnable mTimer1;
//    private LineGraphSeries<DataPoint> mSeries1;
    private Sonar sonsys ;
//    Preview mPreview;
//    private  TextView distanceView;
//    private TextView countView;
//    private TextView singleReading;
    private final int sampleRate = 44100;
    private final int phase =0;
    private final int f0 = 3402;//6803;
    private final int bufferSize = 8000;//32768;
    private final double threshold =90;
    private final int freq = 20;
    private boolean stopped =false;
    private static boolean started = false;
    private static short[] pulse;
    private AudioRecord recorder;
    private short[] buffer;
    public Result result;
    public ArrayList<Double> values = new ArrayList<Double>();
    public Context currentContext;
    public static DecimalFormat df = new DecimalFormat("#.##");
    public int countSamples =0;
//    public ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
//    public RadarChart chart;
    public ArrayList<String> xVals;
//    public ArrayList<RadarDataSet> dataSets;
//    public RadarDataSet setComp1;

    int thresholdPeak = 0;



    public void startSonar() {


        sonsys = new Sonar(thresholdPeak, this.getApplicationContext());

        sonsys.run();


        String distanceMeters = df.format(sonsys.result.distance);
        Log.i(TAG, "Distance=" + distanceMeters);
        Toast.makeText(this.getApplicationContext(), "Distance=" + distanceMeters, Toast.LENGTH_SHORT).show();

//        sonsys.listenToSonar();


//        try {
//            sonsys.scheduleSensing();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

    }

    public void startReceiveSonar() {


        sonsys = new Sonar(thresholdPeak, this.getApplicationContext());

//        String distanceMeters = df.format(sonsys.result.distance);
//        Log.i(TAG, "Distance=" + distanceMeters);

//        sonsys = new Sonar(thresholdPeak, this.getApplicationContext());
//        sonsys.listenToSonar();


        try {
            sonsys.scheduleSensing();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        result = getResult();
//        Log.i(TAG, "Distance=" + result.distance);
    }


//    public Result getResult() {
//
//        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
//                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
//                bufferSize * 2);
//
//        buffer = new short[bufferSize];
//
//        try {
//            recorder.startRecording();
//            recorder.read(buffer, 0, buffer.length);
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        finally{
//            recorder.stop();
//            recorder.release();
//        }
//
//
//        return FilterAndClean.Distance(buffer, pulse, sampleRate, threshold, freq);
//
//
//    }


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
}
