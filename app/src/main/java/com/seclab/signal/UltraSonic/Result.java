package com.seclab.signal.UltraSonic;


public class Result {
    public double distance;
    public double xcorrHeight;
    public short[] signal;
    public double[] xcorr;
    public long timeStamp;
    public long elapseTime;


    public Result(double distance, double xcorrHeight, short[] signal, double[] xcorr, long elapseTime, long timeStamp){
        this.distance = distance;
        this.xcorrHeight = xcorrHeight;
        this.signal = signal;
        this.xcorr = xcorr;
        this.elapseTime = elapseTime;
        this.timeStamp = timeStamp;
    }
}