package com.seclab.signal.UltraSonic;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTimestamp;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.seclab.signal.Controller.BLEController;
import com.seclab.signal.DSP.DSP;

/**
 * Code adapted from https://github.com/researcher111/SonarSimple
 */

/*
 * Thread to manage live recording/playback of voice input from the device's microphone.
 */
public class Sonar extends Thread {

    public int threshold = 1000;
    public int thresholdPeak = 1;
    public double maxDistanceMeters = 5;
    private final int sampleRate = 44100;
    private int receiveRate;
    public static Result result;
    // Chirp Configurations
    public static double f0 = 4000;
    public static double f1 = 8000;
    public static double t1 = 0.01;
    public static double phase = 0;
    private final int numSamples = (int) Math.round(t1 * sampleRate);
    private final int bufferSize = 32768;
    public static boolean stopped = false;
    public static double distance;
    public static double xcorrHeight;
    public static int pulseTrack = 10;
    public static int delay = 0;
    public int deadZoneLength = 60;// (int)(sampleRate*t1/6);
    private static String AUDIO_FILE_PATH = "";
    private Context context;
    private TempSense tempSense;
    AudioRecord recorder;
    AudioTrack track;
    short[] pulse;

    Thread listenThread;

    public int beepCount = 0; // counter to record the #beep it received

    long prev_buffer_cnt = 0;
    long curr_buffer_cnt = 0;
    long prev_peak_cnt = 0;
    long curr_peak_cnt = 0;
    public double diffTime;

    BLEController bleController;


    /**
     * Give the thread high priority so that it's not canceled unexpectedly, and
     * start it
     *
     */
    public Sonar(Context context, BLEController bleController) {
        this.bleController = bleController;
        receiveRate = sampleRate;
        this.context = context;
        this.tempSense = new TempSense(context);
        android.os.Process
                .setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

        pulse = DSP.ConvertToShort(DSP.padSignal(DSP.HanningWindow(
                DSP.linearChirp(phase, f0, f1, t1, sampleRate), 0, numSamples),
                bufferSize, delay));

        recorder = new AudioRecord(AudioSource.MIC, sampleRate,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                bufferSize * 2);

        // init soundTrack
        if (Build.VERSION.SDK_INT < 26)
            track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize * 2, AudioTrack.MODE_STREAM);
        else
            track = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build())
                    .setBufferSizeInBytes(bufferSize)
                    .build();

        // start();
    }

    /**
     * Playing Thread. It will play the beep only once.
     */
    @Override
    public void run() {
        this.thresholdPeak = thresholdPeak;
        Log.i("Audio", "Running Audio Thread");
        short[] buffer = new short[bufferSize];

        int ix = 0;

        int N = AudioRecord.getMinBufferSize(sampleRate,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);


        track.setVolume(AudioTrack.getMaxVolume());

        try {

            recorder.startRecording();
            for (int i=0; i<1; ++i) {
                track.write(pulse, 0, pulse.length);
                track.play();
            }

        }
        catch (Exception e) {

        }
        finally {
//            recorder.stop();
//            recorder.release();
        }

    }


    /**
     * Listening thread. It will create a thread to keep listening to the beep.
     */
    public void scheduleSensing()
    {
        if (recorder == null) {
            recorder = new AudioRecord(AudioSource.MIC, receiveRate,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize * 2);
        }

        // fixed pulse
        final short[] pulse = DSP.ConvertToShort(DSP.padSignal(DSP.HanningWindow(
                DSP.linearChirp(phase, f0, f1, t1, sampleRate), 0, numSamples),
                bufferSize, delay));

        // create listen thread
        listenThread = new Thread(
                new Runnable()
                {
                    @Override
                    public void run() {
                        int counter = 0;
                        while(true){

                            counter++;
                            short[] buffer = new short[bufferSize];
                            try {
                                recorder.startRecording();
                                long timeStamp = System.nanoTime();
                                recorder.read(buffer, 0, buffer.length);

                                // calculate
                                Result res = FilterAndClean.DistanceSingle(buffer, pulse, sampleRate, threshold, maxDistanceMeters, deadZoneLength, thresholdPeak, numSamples,0, timeStamp);

                                // a beep is detected with the filtering function
                                if (res.peakDetected) {
                                        // update buffer length
                                        prev_buffer_cnt = curr_buffer_cnt;
                                        curr_buffer_cnt = counter;

                                        // update peak index
                                        prev_peak_cnt = curr_peak_cnt;
                                        curr_peak_cnt = res.peakIndex;

                                        // calculate time difference based on the buffer distance
                                        double sampleNum = (curr_buffer_cnt - prev_buffer_cnt) * bufferSize + (curr_peak_cnt - prev_peak_cnt);
                                        double diff = sampleNum / sampleRate;

                                        Log.i("Sonar", "Diff Time: " + diff);

                                        if (beepCount < 1) {
                                            // first beep received
                                            // if it is a BLE peripheral, play beep after 2 sec. If it is a BLE central, do nothing
                                            if (bleController.getRole() == BLEController.ROLE.PERIPHERAL)
                                                receiverHandler.sendEmptyMessageDelayed(0, 2000);
                                            ++beepCount;
                                        }
                                        else if (beepCount == 1) {
                                            // second beep received, record the time difference
                                            diffTime = diff;
                                            bleController.setTimeDiffValue(diff);
                                            ++beepCount;
                                        }

                                    }

                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );
        listenThread.start();
    }


    // handler for playing beep once
    Handler receiverHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {
            // beep here
            track.write(pulse, 0, pulse.length);
            track.play();
        }
    };



    /**
     * Called from outside of the thread in order to stop the recording/playback
     * loop
     *
     */
    private void close() {
        stopped = true;
    }

    /*
     * private void emailFile(String s){
     *
     * Intent i = new Intent(Intent.ACTION_SEND); i.setType("message/rfc822");
     * i.putExtra(Intent.EXTRA_EMAIL , new String[]{"dggraham@email.wm.edu"});
     * i.putExtra(Intent.EXTRA_SUBJECT, "subject of email");
     * i.putExtra(Intent.EXTRA_TEXT , s); try {
     * context.startActivity(Intent.createChooser(i, "Send mail...")); } catch
     * (android.content.ActivityNotFoundException ex) { Toast.makeText(context,
     * "There are no email clients installed.", Toast.LENGTH_SHORT).show(); } }
     */

    // this method writes the pulse array to a file
    /*
     * private void writeStringToTextFile(String s, String f) { //File sdCard =
     * Environment.get .getExternalStorageDirectory(); //File dir = new
     * File(sdCard.getAbsolutePath()); //dir.mkdirs(); //File file = new
     * File(f); Context ctx = this.context; try { FileOutputStream f1 =
     * ctx.openFileOutput(f, Context.MODE_PRIVATE); PrintStream p = new
     * PrintStream(f1); p.print(s); p.close(); f1.close(); } catch
     * (FileNotFoundException e) {
     *
     * } catch (IOException e) {
     *
     * }
     *
     * }
     */

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                albumName);
        if (isExternalStorageWritable()) {
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    Log.e("Write Error", "Directory not created");
                }
            }
        } else {
            Log.e("Write Error", "Storage Not writable");
        }
        return file;
    }

    public void writeStringToTextFile(String text, String fileName) {

        try {
            File dir = getAlbumStorageDir("Sonar");
            File file = new File(dir, fileName);
            BufferedWriter output = new BufferedWriter(new FileWriter(file));
            output.write(text);
            output.close();
            //MediaScannerConnection.scanFile(this, new String[] { file.getAbsolutePath() }, null, null);
        } catch (IOException e) {
            Log.e("Write Error", "Failed to write content");
        }
    }

}