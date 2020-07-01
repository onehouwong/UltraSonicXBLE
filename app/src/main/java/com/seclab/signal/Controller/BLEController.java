package com.seclab.signal.Controller;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.UUID;

public class BLEController {

    private String TAG = "BLEController";
    public BluetoothLeAdvertiser advertiser;
    public String DEFAULT_UUID = "5921174c-bb48-11ea-b3de-0242ac130004";
    public String DEVICE_NAME = "Test Signal";


    public BLEController() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        adapter.setName(DEVICE_NAME);
        advertiser = adapter.getBluetoothLeAdvertiser();
    }

    public void advertise() {

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
                .setConnectable(false)
                .build();

        ParcelUuid pUuid = new ParcelUuid( UUID.fromString(DEFAULT_UUID));


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

    public void stopAdvertising() {
        advertiser.stopAdvertising(new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
            }
        });
    }


    @TargetApi(26)
    public void advertiseCustom() {


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


    @TargetApi(26)
    public void stopAdvertisingCustom() {
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
}
