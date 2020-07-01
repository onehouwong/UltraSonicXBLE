package com.seclab.signal;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.seclab.signal.Controller.BLEController;
import com.seclab.signal.Controller.SonarController;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.PermissionChecker;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.Button;

public class MainActivity extends AppCompatActivity {


    public static String TAG = "SignalTest";


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


        // check permission
        int permission;
        if (Build.VERSION.SDK_INT < 23) {
            permission = PermissionChecker.checkSelfPermission(this.getApplicationContext(), Manifest.permission.RECORD_AUDIO);
            permission = PermissionChecker.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
            permission = PermissionChecker.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);

        }
        else {
            if (this.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                        123);
            }

            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        456);
            }

            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        789);
            }
        }


        final SonarController sonarController = new SonarController(this.getApplicationContext());
        final BLEController bleController = new BLEController(this.getApplicationContext(), sonarController);



        // BLE
        Button startBtn = findViewById(R.id.start);
        Button stopBtn = findViewById(R.id.stop);
        Button scanBtn = findViewById(R.id.scan);


        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start BLE advertising
                bleController.advertise();
            }
        });


        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleController.stopAdvertising();
            }
        });

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bleController.scan();
                    }
                });
            }
        });



        // Sonar
        Button startSonarBtn = findViewById(R.id.startSonar);
        Button readSonarBtn = findViewById(R.id.readSonar);


        startSonarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sonarController.startSonar();
            }
        });


        readSonarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sonarController.startReceiveSonar();
            }
        });


        // play
        Button ganBtn = findViewById(R.id.play);
        ganBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bleController.advertise();
                sonarController.startSonar();

                // sleep one second
//                try {
//                    Thread.sleep(1000);
//                }
//                catch(InterruptedException e) {
//                    e.printStackTrace();
//                }
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
}
