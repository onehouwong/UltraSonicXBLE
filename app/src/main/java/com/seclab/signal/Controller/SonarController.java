package com.seclab.signal.Controller;

import android.content.Context;
import android.media.AudioRecord;
import android.os.Handler;

import com.seclab.signal.UltraSonic.Result;
import com.seclab.signal.UltraSonic.Sonar;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class SonarController {

    private String TAG = "SonarController";
    private final Handler mHandler = new Handler();
    private Runnable mTimer1;
    private Sonar sonsys ;
    private final int sampleRate = 44100;
    private final int phase =0;
    private final int f0 = 3402;//6803;
    private final int bufferSize = 32768;
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
    public ArrayList<String> xVals;


    private Context context;

    public BLEController bleController;


    public SonarController(Context c) {
        context = c;
    }

    public void setBLEController(BLEController bleController) {
        this.bleController = bleController;
    }

    public Sonar getSonsys() {
        return sonsys;
    }

    /**
     * Start playing beep
     */
    public void startSonar() {

        if (sonsys == null)
            sonsys = new Sonar(context, bleController);

        sonsys.run();
    }

    /**
     * Start listening to beep
     */
    public void startReceiveSonar() {

        if (sonsys == null)
            sonsys = new Sonar(context, bleController);
        sonsys.diffTime = 0;
        sonsys.beepCount = 0;

        sonsys.scheduleSensing();

    }



}
