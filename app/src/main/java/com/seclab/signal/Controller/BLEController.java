package com.seclab.signal.Controller;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;


import com.seclab.signal.BLE.DistanceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BLEController {

    private String TAG = "BLEController";
    public BluetoothLeAdvertiser advertiser;
    public String DEFAULT_UUID = "5921174c-bb48-11ea-b3de-0242ac130004";
    public String Characteristic_UUID = "5921174c-bb48-11ea-b3de-0242ac130001";
    public String Characteristic_UUID2 = "5921174c-bb48-11ea-b3de-0242ac130002";

    public BluetoothGattServer bluetoothGattServer;

    public String DEVICE_NAME = "Test";
    public BluetoothLeScanner scanner;
    public Context context;
    public BluetoothGatt receiver;

    public boolean connected = false;
    public ROLE role;

    public long latestTime = 0;
    public SonarController sonarController;


    public BLEController(Context c, SonarController sc) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        adapter.setName(DEVICE_NAME);
        advertiser = adapter.getBluetoothLeAdvertiser();
        scanner = adapter.getBluetoothLeScanner();
        context = c;
        sonarController = sc;

        initGATT();
    }

    public void setTimeDiffValue(double t) {

        BluetoothGattCharacteristic c = receiver.getService(UUID.fromString(DEFAULT_UUID)).getCharacteristic(UUID.fromString(Characteristic_UUID));
        c.setValue("" + t);
        boolean suc = receiver.writeCharacteristic(c);
    }

    public void initGATT() {
        BluetoothGattService service = new BluetoothGattService(UUID.fromString(DEFAULT_UUID), 0);
        BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(UUID.fromString(Characteristic_UUID),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);

        BluetoothGattCharacteristic characteristic2 = new BluetoothGattCharacteristic(UUID.fromString(Characteristic_UUID2),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);

        characteristic.setValue("-1");
        characteristic2.setValue("-1");

        service.addCharacteristic(characteristic);
        service.addCharacteristic(characteristic2);


        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(context.BLUETOOTH_SERVICE);
        bluetoothGattServer = bluetoothManager.openGattServer(context, new BluetoothGattServerCallback() {
            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                super.onConnectionStateChange(device, status, newState);

                if (receiver == null && newState == BluetoothProfile.STATE_CONNECTED) {
                    device.connectGatt(context, false, new BluetoothGattCallback() {
                        @Override
                        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                            super.onConnectionStateChange(gatt, status, newState);

                            if (!connected) {
                                // connected
                                connected = true;
                                receiver = gatt;

                                if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                                    receiver.discoverServices();
                                }
                            }

                            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                                connected = false;
                            }

                        }
                    });

                }
            }

            @Override
            public void onServiceAdded(int status, BluetoothGattService service) {
                super.onServiceAdded(status, service);
            }

            @Override
            public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
//                sonarController.startSonar();
            }

            @Override
            public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                     BluetoothGattCharacteristic characteristic,
                                                     boolean preparedWrite, boolean responseNeeded,
                                                     int offset, byte[] value) {
                super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);

                if (characteristic.getUuid().toString().equalsIgnoreCase(Characteristic_UUID)) {

                    while (sonarController.getSonsys().diffTime == 0) {

                    }

                    double selfDiffTime = sonarController.getSonsys().diffTime;
                    double peerDiffTime = Double.parseDouble(new String(value));

                    // calculate distance
                    double soundSpeed = 340;

                    double distance = Math.abs(selfDiffTime - peerDiffTime) * soundSpeed / 2;
                    Log.i(TAG, "Distance = " + distance);

                }
            }
        });

        bluetoothGattServer.addService(service);
    }


    public ROLE getRole () {return this.role;}


    /**
     * Start BLE Scanning, as a BLE central
     */
    public void scan() {
        role = ROLE.CENTRAL;

        List<ScanFilter> scanFilter = new ArrayList<>();

        scanFilter.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(DEFAULT_UUID)).build());

        ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();

        ScanCallback callback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, final ScanResult result) {
                super.onScanResult(callbackType, result);

                double distance = DistanceUtil.calculateDistance(result.getRssi(), result.getTxPower());
                Log.i("Distance", "distance = " + distance + "\tRSSI = " + result.getRssi());

                // discover device, connect
                // TODO
                if (false && receiver == null) {
                    result.getDevice().connectGatt(context, false, new BluetoothGattCallback() {
                        @Override
                        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                            super.onConnectionStateChange(gatt, status, newState);

                            if (!connected) {
                                // connected

                                scanner.stopScan(new ScanCallback() {
                                    @Override
                                    public void onScanResult(int callbackType, ScanResult result) {
                                        super.onScanResult(callbackType, result);
                                    }
                                });

                                connected = true;
                                receiver = gatt;
                                Log.i(TAG, "Connected to: " + result.getDevice().getName());

                                // try to discover service
                                if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                                    receiver.discoverServices();
                                }
                            }

                            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                                connected = false;
                            }

                        }

                        @Override
                        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                            super.onServicesDiscovered(gatt, status);

                            List<BluetoothGattService> services = gatt.getServices();
                            for (BluetoothGattService service : services) {
                                if (service.getUuid().toString().equalsIgnoreCase(DEFAULT_UUID)) {
                                    // target service discovered
                                    for (BluetoothGattCharacteristic c : service.getCharacteristics()) {
                                        if (c.getUuid().toString().equalsIgnoreCase(Characteristic_UUID)) {
                                            // target characteristic discovered
//                                            boolean succ = gatt.readCharacteristic(c);
                                            sonarController.startSonar();
                                            latestTime = System.nanoTime();
                                        }
                                    }
                                }
                            }
                        }
                    });
                }

            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                latestTime = System.nanoTime();
                Log.i(TAG, "onBatchScanResults, Time=" + latestTime);
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.i(TAG, "onScanFailed");
                super.onScanFailed(errorCode);
            }
        };


        scanner.startScan(scanFilter, settings, callback);
    }


    /**
     * Start BLE advertising, as a BLE peripheral
     */
    public void advertise() {

        role = ROLE.PERIPHERAL;

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_POWER )
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
                .setConnectable(true)
//                .setTimeout(1000)
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

        role = ROLE.PERIPHERAL;

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

        ParcelUuid pUuid = new ParcelUuid( UUID.fromString(DEFAULT_UUID));


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

    public enum ROLE {
        PERIPHERAL,
        CENTRAL
    }
}
