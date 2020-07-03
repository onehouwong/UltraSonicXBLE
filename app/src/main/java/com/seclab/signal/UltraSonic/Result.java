package com.seclab.signal.UltraSonic;


public class Result {
    public double distance;
    public double xcorrHeight;
    public short[] signal;
    public double[] xcorr;
    public double timeStamp;
    public double elapseTime;
    public int peakIndex;


    public Result(double distance, double xcorrHeight, short[] signal, double[] xcorr, double elapseTime, double timeStamp, int peakIndex){
        this.distance = distance;
        this.xcorrHeight = xcorrHeight;
        this.signal = signal;
        this.xcorr = xcorr;
        this.elapseTime = elapseTime;
        this.timeStamp = timeStamp;
        this.peakIndex = peakIndex;
    }
}