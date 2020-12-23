package com.seclab.signal.UltraSonic;

/**
 * Code adapted from https://github.com/researcher111/SonarSimple
 */

public class Result {
    public double distance;
    public double xcorrHeight;
    public short[] signal;
    public double[] xcorr;
    public boolean peakDetected;
    public int peakIndex;


    public Result(double distance, double xcorrHeight, short[] signal, double[] xcorr, boolean peakDetected, int peakIndex){
        this.distance = distance;
        this.xcorrHeight = xcorrHeight;
        this.signal = signal;
        this.xcorr = xcorr;
        this.peakDetected = peakDetected;
        this.peakIndex = peakIndex;
    }
}