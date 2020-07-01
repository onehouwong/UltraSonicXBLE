package com.seclab.signal.Controller;

import android.content.Context;
import android.media.AudioRecord;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.seclab.signal.MainActivity;
import com.seclab.signal.UltraSonic.Result;
import com.seclab.signal.UltraSonic.Sonar;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class SonarController {

    private String TAG = "SonarController";
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
    private Context context;


    public SonarController(Context c) {
        context = c;
    }

    public void startSonar() {

        sonsys = new Sonar(thresholdPeak, context);

        sonsys.run();

        String distanceMeters = df.format(sonsys.result.distance);
//        Log.i(TAG, "Distance=" + distanceMeters);
        Toast.makeText(context, "Distance=" + distanceMeters, Toast.LENGTH_SHORT).show();


//        try {
//            sonsys.scheduleSensing();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

    }

    public void startReceiveSonar() {

        sonsys = new Sonar(thresholdPeak, context);

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

}
