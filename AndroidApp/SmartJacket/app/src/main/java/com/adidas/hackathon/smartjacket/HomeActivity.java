package com.adidas.hackathon.smartjacket;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.adidas.hackathon.smartjacket.ble.BleDeviceSimple;
import com.adidas.hackathon.smartjacket.ble.BleInterface;
import com.adidas.hackathon.smartjacket.fragments.HomeFragment;
import com.adidas.hackathon.smartjacket.settings.AppSharedPreferences;
import com.adidas.hackathon.smartjacket.ui.DeviceStatusLayout;
import com.adidas.hackathon.smartjacket.ui.SensorStatus;
import com.adidas.hackathon.smartjacket.util.ButtonActions;
import com.adidas.hackathon.smartjacket.util.Media;
import com.adidas.hackathon.smartjacket.util.PermissionsHandler;
import com.adidas.hackathon.smartjacket.util.PhoneCall;
import com.adidas.hackathon.smartjacket.util.Tools;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

import io.reactivex.disposables.CompositeDisposable;


public class HomeActivity extends AppCompatActivity {

    public static final String TAG = HomeActivity.class.getName();

    @BindView(R.id.device_status)
    DeviceStatusLayout deviceStatusLayout;

    private CompositeDisposable disposables = new CompositeDisposable();

    boolean hasPermissions = false;

    /**
     * Android audio manager. Used to control audio streams, volume and play system sounds
     */
    AudioManager audioManager;

    /**
     * BLE object, abstracted through an interface.
     */
    public BleInterface ble;

    /**
     * Stores an incoming message from the server (jacket).
     */
    public static String strServerMessage = "";

    /**
     * Signals an incoming message from the server (jacket).
     */
    public static boolean hasNewServerMessage = false;

    /**
     * Whether the sent plan was acknowledged by the server.
     */
    public static boolean planAcknowledged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.content_main, HomeFragment.newInstance()).commit();

        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.dispose();
    }

    /**
     * Initialize: check/request permissions, get audio manager, initialize BLE.
     */
    private void init() {
        Log.i(TAG, "init");
        deviceStatusLayout.setStatus(SensorStatus.NOT_CONNECTED);

        if (ble == null) {
            Log.i(TAG, "Ble jacket is null");
            ble = new BleJacket(this);
        }

        if (audioManager == null) {
            Log.i(TAG, "Audio manager is null");
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }

        if (!hasPermissions) {
            checkPermissions();
        }

        checkBleInit();
    }

    /**
     * Request the necessary permissions for the app to run.
     */
    private void checkPermissions() {
        Log.i(TAG, "Checking permissions");
        PermissionsHandler permissionsHandler = new PermissionsHandler(this);
        disposables.add(permissionsHandler.getRxPermissions()
                .request(Manifest.permission.CALL_PHONE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_CONTACTS)
                .subscribe(granted -> {
                    if (granted) {
                        hasPermissions = true;
                    } else {
                        hasPermissions = false;
                        deviceStatusLayout.setStatus(SensorStatus.NO_BLUETOOTH_PERMISSION);
                    }
                }));
    }

    /**
     * Check if the BLE object is initialized, and do so if necessary.
     */
    private void checkBleInit() {
        Log.i(TAG, "check ble init");

        if (!Tools.hasBLE(this)) {
            Toast.makeText(this, "This device does not have a Bluetooth 4.0 capable hardware", Toast.LENGTH_SHORT).show();
            return;
        }

        Tools.checkBluetoothEnabled(this);

        if (!ble.isInitialized()) {
            Log.i(TAG, "checkBLEInit() initializing BLEJacket");
            ble.init();
        }
    }

    /**
     * Handles a stored message (JSON string) received from the server (jacket).
     */
    void handleServerMessage() throws JSONException {
        hasNewServerMessage = false;

        if (strServerMessage == null) {
            return;
        }

        JSONObject jsonObj;

        // try to parse the string into a JSON object
        try {
            jsonObj = new JSONObject(strServerMessage);
        } catch (Exception e) {
            Log.i(TAG, "handleServerMessage() ERROR: exception when parsing json string <" + strServerMessage + ">");
            return;
        }

        // when there is an acknowledgement to a sent plan
        if (jsonObj.has("ack")) {
            boolean isAck = jsonObj.getBoolean("ack");
            // if the acknowledgement was positive,
            if (isAck) {
                if (jsonObj.has("rs")) {
                    Log.i(TAG, "handleServerMessage() acknowledged, code " + jsonObj.getInt("rs"));
                } else {
                    Log.i(TAG, "handleServerMessage() acknowledged (no code)");
                }
                planAcknowledged = true;
            } else { // if the acknowledgement was negative, print the error code
                if (jsonObj.has("rs")) {
                    int errCode = jsonObj.getInt("rs");
                    Log.i(TAG, "handleServerMessage() WARNING: ack is false, error code " + errCode);
                } else {
                    Log.i(TAG, "handleServerMessage() ERROR: ack is false, but no \"rs\" element");
                }
                planAcknowledged = false;
            }
        }
        // when the message contains a "signal"
        else if (jsonObj.has("si")) {
            String signal = jsonObj.getString("si");
            if (signal.equals("btn")) {
                // when the signal came from a button, handle the signal
                if (jsonObj.has("bt")) {
                    String button = jsonObj.getString("bt");
                    handleJacketButton(button);
                } else {
                    Log.i(TAG, "handleServerMessage() ERROR: malformed signal command");
                }
            } else {
                Log.i(TAG, "handleServerMessage() WARNING: unsupported signal <" + signal + ">");
            }
        }
        // other messages are unsupported
        else {
            Log.i(TAG, "handleServerMessage() WARNING: unsupported message <" + strServerMessage + ">");
        }
    }

    /**
     * Handles a signal from a jacket button, by finding the appropriate
     * mapping between buttons and actions specified in the button actions class.
     */
    void handleJacketButton(String strBtn) {
        if (strBtn == null) {
            Log.i(TAG, "handleJacketButton() ERROR: no \"buttonActions\" element in appSettings");
            return;
        }

        Log.i(TAG, "String button is: " + strBtn);

        if (strBtn.equalsIgnoreCase(ButtonActions.ice)) {
            PhoneCall.startCall(AppSharedPreferences.getPrefContactNumber(this), this);
            Log.i(TAG, "handleJacketButton start call");
        } else if (strBtn.equalsIgnoreCase(ButtonActions.a)) {
            Media.mediaVolumeUp(audioManager);
            Log.i(TAG, "handleJacketButton volume up");
        } else if (strBtn.equalsIgnoreCase(ButtonActions.b)) {
            Media.mediaVolumeDown(audioManager);
            Log.i(TAG, "handleJacketButton volume down");
        } else if (strBtn.equalsIgnoreCase(ButtonActions.ab)) {
            Media.mediaPlayPause(audioManager);
            Log.i(TAG, "handleJacketButton media play pause");
        } else if (strBtn.equalsIgnoreCase(ButtonActions.aTob)) {
            Media.mediaNext(audioManager);
            Log.i(TAG, "handleJacketButton next song");
        } else if (strBtn.equalsIgnoreCase(ButtonActions.bToa)) {
            Media.mediaPrevious(audioManager);
            Log.i(TAG, "handleJacketButton previous song");
        } else {
            Log.i(TAG, "handleServerMessage() WARNING: unknown action. Action not defined in button actions class.");
        }
    }

    /**
     * Callback for when the BLE object's connection state has changed (or failed to do so).
     * This is in turn called by a BLE callback itself, so it should avoid lengthy operations.
     */
    public void onBLEDeviceConnectionChange(int state) {
        Log.i(TAG, "onBLEDeviceConnectionChange() state " + state);
        // debug information
        if (state == 0) {
            Log.i(TAG, "onBLEDeviceConnectionChange() device DISCONNECTED ");
            updateDeviceStatus(SensorStatus.NOT_CONNECTED);
        } else if (state == 2) {
            Log.i(TAG, "onBLEDeviceConnectionChange() device CONNECTED ");
            updateDeviceStatus(SensorStatus.CONNECTED);
        } else {
            Log.i(TAG, "onBLEDeviceConnectionChange() WARNING unknown state " + state);
        }
    }

    /**
     * Callback for when a message is received from the server (jacket).
     * This is in turn called by a BLE callback itself, so it should avoid lengthy operations.
     * Flags and stores the incoming message, discarding any previous one.
     */
    public void onServerMessageReceived(String message) throws JSONException {
        Log.i(TAG, "onServerMessageReceived() message/param <" + message + ">");
        if (hasNewServerMessage) {
            Log.i(TAG, "onServerMessageReceived() WARNING previous message will be discarded <" + strServerMessage + ">");
        }
        strServerMessage = message;
        hasNewServerMessage = true;

        handleServerMessage();
    }

    public void updateDeviceStatus(@SensorStatus int sensorStatus) {
        runOnUiThread(() -> {
            Log.i(TAG, "Update device status");
            deviceStatusLayout.setStatus(sensorStatus);
        });
    }

    /**
     * Ble
     */
    public class BleJacket implements BleInterface {

        private final String TAG = BleJacket.class.getName();

        /**
         * UUID for the jacket's BLE service.
         */
        private static final String UUID_SERVICE = "E708EB00-AD98-4158-B7C2-A748744694AB";
        /**
         * UUID for the jacket's BLE service, characteristic for timestamp.
         */
        private static final String UUID_CHARACT_TIMESTAMP = "E708EB01-AD98-4158-B7C2-A748744694AB";
        /**
         * UUID for the jacket's BLE service.
         */
        private static final String UUID_CHARACT_PLAN_NAME = "E708EB02-AD98-4158-B7C2-A748744694AB";
        /**
         * UUID for the jacket's BLE service, characteristic for plan name.
         */
        private static final String UUID_CHARACT_STATE = "E708EB03-AD98-4158-B7C2-A748744694AB";
        /**
         * UUID for the jacket's BLE service, characteristic for client command.
         */
        private static final String UUID_CHARACT_CLIENT_CMD = "E708EB04-AD98-4158-B7C2-A748744694AB";
        /**
         * UUID for the jacket's BLE service, characteristic for server message.
         */
        private static final String UUID_CHARACT_SERVER_MSG = "E708EB05-AD98-4158-B7C2-A748744694AB";

        /**
         * UUID for the client characteristic descriptor, 0x2902
         */
        private static final String UUID_DESCRIP_CCC = "00002902-0000-1000-8000-00805F9B34FB";

        /**
         * The MTU (packet size for BLE messages). It should be large enough so we
         * can send and receive large strings, like JSON-formatted plans.
         * According to the BLE specification, minimum is 23, maximum is 517.
         * This depends on whether the requested MTU size is supported by the server (jacket).
         */
        private static final int BLE_JACKET_MTU_SIZE = 517;

        /**
         * The default duration of a scan for BLE devices (milliseconds)
         */
        private static final int DEFAULT_SCAN_TIMEOUT = 5000;

        // Variables to store the jacket's current status.
        // Updated when reading the BLE service's characteristics.
        private int valTimestamp = 0;
        private String valPlanName = "";
        private int valPlanState = PLAN_STATE_NONE;
        private int valCurrPhase = 0;
        private int valProgress = 0;
        private int valCtrlMode = 0;
        private int valBattery = 42;

        private Context context;

        /**
         * Reference to the bluetooth manager.
         */
        private BluetoothManager bluetoothManager;
        /**
         * Reference to the bluetooth adapter.
         */
        private BluetoothAdapter bluetoothAdapter;
        /**
         * Reference to the bluetooth scanner.
         */
        private BluetoothLeScanner bleScanner;
        /**
         * Reference to the object that handles scan results.
         */
        private JacketScanCallback scanCallback;
        /**
         * Reference to the jacket GATT object, which handles BLE operations.
         */
        private JacketGatt jacketGatt;

        /**
         * State flag: whether the BLE object is initialized.
         */
        private boolean isInit = false;
        /**
         * State flag: whether the BLE object is scanning for devices.
         */
        private boolean isScanning = false;
        /**
         * State flag: whether the BLE object is attempting to connect to a device (jacket).
         */
        private boolean isGattConnecting = false;
        /**
         * State flag: whether the BLE object is connected (but not necessarily ready to be used).
         */
        private boolean isGattConnected = false;
        /**
         * State flag: whether the BLE object is connected and configured (ready to be used).
         */
        private boolean isGattReady = false;

        /**
         * Timer used to stop scanning after the specified timeout.
         */
        private Timer stopScanTimer;

        /**
         * Map used to store information on known devices.
         */
        private Map<String, BluetoothDevice> deviceMap;

        /**
         * MAC address for a preferred device.
         */
        private String targetMacAddress = null;


        /**
         * Constructor. Stores a reference to the parent and creates member objects.
         * The BLEJacket still needs to be initialized, see init().
         */
        BleJacket(Context context) {
            this.context = context;

            deviceMap = new HashMap<>();
            scanCallback = new JacketScanCallback();
            jacketGatt = new JacketGatt(context);

            Log.i(TAG, "BLEJacket.BLEJacket()");
        }

        /**
         * BLEInterface: initialize the object's BLE logic.
         */
        public void init() {
            // obtain the BLE objects
            bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager != null ? bluetoothManager.getAdapter() : null;
            bleScanner = bluetoothAdapter != null ? bluetoothAdapter.getBluetoothLeScanner() : null;

            // the BLEJacket object is now initialized and ready to scan/connect
            isInit = true;
            Log.i(TAG, "BLEJacket.init(): initialized");
        }


        /**
         * BLEInterface: dispose of the object.
         */
        public void dispose() {
            // stop scanning and/or disconnect
            if (isScanning) stopScanning();
            if (isGattConnecting || isGattConnected) disconnect();
        }


        /**
         * BLEInterface: check if the object has been initialized.
         */
        public boolean isInitialized() {
            return isInit;
        }


        /**
         * BLEInterface: check if the object is currently scanning for BLE devices.
         */
        public boolean isScanning() {
            return isScanning;
        }


        /**
         * BLEInterface: start scanning for BLE devices, until the interval elapses (milliseconds).
         * Results are handled via the parent's callback:
         * void onBLEDeviceDiscovered(String name, String address);
         */
        @Override
        public int startScanning(int scanInterval) {
            if (!isInit) {
                Log.i(TAG, "BLEJacket.startScanning() ERROR: BLEJacket isn't initialized.");
                return -1;
            }

            // we are scanning for devices, potentially selecting a new one,
            // so clear the default/preferred connection address
            targetMacAddress = null;
            deviceMap.clear();

            // specify and build an (empty) filter for the scan operation
            ScanFilter.Builder buildFilter = new ScanFilter.Builder();
            ArrayList<ScanFilter> scanFilters = new ArrayList<>();
            scanFilters.add(buildFilter.build());

            // specify and build settings for the scan operation
            ScanSettings.Builder builderScanSettings = new ScanSettings.Builder();
            builderScanSettings.setScanMode(ScanSettings.SCAN_MODE_BALANCED);
            builderScanSettings.setReportDelay(0);
            ScanSettings scanSettings = builderScanSettings.build();

            // start scanning; an object of nested class JacketScanCallback is passed to handle results.
            // at the parent's level results are handled in the callback:
            //   onBLEDeviceDiscovered(String name, String address);
            bleScanner.startScan(scanFilters, scanSettings, scanCallback);

            // set a timer to stop scanning after the specified interval (milliseconds)
            setTimerToStopScan(scanInterval);
            isScanning = true;
            Log.i(TAG, "BLEJacket.startScanning(): scanning started.");

            return 0;
        }


        /**
         * BLEInterface: stop scanning for BLE devices.
         */
        public int stopScanning() {
            // check if the object is initialized or already scanning
            if (!isInit) {
                Log.i(TAG, "BLEJacket.stopScanning() ERROR: BLEJacket isn't initialized.");
                return -1;
            }
            if (!isScanning) {
                Log.i(TAG, "BLEJacket.stopScanning() scanning was already stopped");
                return -1;
            }

            // flush any scan results that haven't been advertised
            bleScanner.flushPendingScanResults(scanCallback);
            bleScanner.stopScan(scanCallback);
            isScanning = false;
            return 0;
        }


        /**
         * BLEInterface: returns the number of named BLE devices found in the last scan.
         */
        public int getResultCount() {
            return deviceMap.size();
        }


        /**
         * BLEInterface: get the list of named BLE devices found in the last scan, as pairs of name and MAC Address.
         */
        public ArrayList<BleDeviceSimple> getDeviceList() {
            // return a new array list populated as a list of known devices
            // each device is represented by name and address, as a BLEDeviceSimple object
            ArrayList<BleDeviceSimple> result = new ArrayList<>();
            for (Map.Entry<String, BluetoothDevice> entry : deviceMap.entrySet()) {
                BluetoothDevice device = entry.getValue();
                result.add(new BleDeviceSimple(device.getName(), device.getAddress()));
            }
            return result;
        }


        /**
         * BLEInterface: get a specific item from the list of named BLE devices found in the last scan, as a pair of name and MAC Address.
         */
        public BleDeviceSimple getScanResult(int i) {
            BluetoothDevice device = null;

            int deviceIndex = 0;
            for (Map.Entry<String, BluetoothDevice> entry : deviceMap.entrySet()) {
                if (deviceIndex == i) {
                    device = entry.getValue();
                    break;
                }
                deviceIndex++;
            }

            if (device == null) return null;
            return new BleDeviceSimple(device.getName(), device.getAddress());
        }


        /**
         * BLEInterface: attempt a connection to a device identified by MAC address.
         * Returns immediately. The result is handled via the parent's callback:
         * void onBLEDeviceConnectionChange(int status, BLEDeviceSimple device);
         */
        public int connectTo(BleDeviceSimple device) {
            Log.i(TAG, "BLEJacket.connectTo() " + device.name() + " " + device.mac());
            updateDeviceStatus(SensorStatus.CONNECTING);

            return connectTo(device.mac());
        }


        /**
         * BLEInterface: attempt a connection to a device identified represented by a BLEDeviceSimple object.
         * Returns immediately. The result is handled via the parent's callback:
         * void onBLEDeviceConnectionChange(int status, BLEDeviceSimple device);
         */
        public int connectTo(String deviceAddress) {
            Log.i(TAG, "BLEJacket.connectTo() " + deviceAddress);
            updateDeviceStatus(SensorStatus.CONNECTING);

            // check if BLE (GATT) is trying to connect or already connected
            if (isGattConnected) {
                Log.i(TAG, "BLEJacket.connectTo(): WARNING gatt is already connected");
                return -1;
            } else if (isGattConnecting) {
                Log.i(TAG, "BLEJacket.connectTo(): WARNING gatt is already connecting");
                return -1;
            }

            // check if the device is known, as we need the complete BluetoothDevice object in order to connect
            BluetoothDevice device = deviceMap.get(deviceAddress);
            if (device == null) {
                Log.i(TAG, "BLEJacket.connectTo(): device address <" + deviceAddress + "> not in map; starting scan");
                startScanning(DEFAULT_SCAN_TIMEOUT);
                targetMacAddress = deviceAddress;
                return -2;
            }

            // connect GATT using the BluetoothDevice object
            Log.i(TAG, "BLEJacket.connectTo(): connecting to <" + deviceAddress + ">");
            jacketGatt.init(device);
            return 0;
        }

        /**
         * BLEInterface: disconnect from the currently connected BLE device.
         * Returns immediately. The result is handled via the parent's callback:
         * void onBLEDeviceConnectionChange(int status, BLEDeviceSimple device);
         */
        public int disconnect() {
            Log.i(TAG, "JacketGatt.disconnect(): disconnecting");

            if (isGattConnected || isGattConnecting) {
                jacketGatt.gatt.disconnect();
                return 0;
            } else {
                Log.i(TAG, "JacketGatt.disconnect(): already disconnected");
                return -1;
            }
        }


        /**
         * BLEInterface: whether the object is currently trying to connect to a BLE device.
         */
        public boolean isConnecting() {
            return isGattConnecting;
        }


        /**
         * BLEInterface: if the object is currently connected to a BLE device.
         */
        public boolean isConnected() {
            return isGattConnected;
        }


        /**
         * BLEInterface: if the object is currently connected and ready to be used
         * (i.e., not discovering or configuring services).
         */
        public boolean isReady() {
            return isGattReady;
        }


        /**
         * BLEInterface: get the name and MAC address of the currently connected device (or null if not connected).
         */
        public BleDeviceSimple getDevice() {
            BluetoothDevice device = jacketGatt.bluetoothDevice;
            if (device == null) return null;
            return new BleDeviceSimple(device.getName(), device.getAddress());
        }


        /**
         * BLEInterface: read the server (jacket) state, by reading and parsing the appropriate characteristics.
         */
        public int updateState() {
            return jacketGatt.updateState();
        }


        /**
         * BLEInterface: send a plan to the server (jacket) as a JSON-formatted string.
         */
        public int sendPlan(String jsonStringPlan) {
            // compose the full JSON command and send it to the server
            String strCommand = "{\"co\":\"npl\",\"pl\":" + jsonStringPlan + "}";
            return jacketGatt.sendClientCommand(strCommand);
        }


        /**
         * BLEInterface: set the jacket's tactile control to calls (NOT IMPLEMENTED).
         */
        public int setControlCalls() {
            return -1;
        }


        /**
         * BLEInterface: set the jacket's tactile control to music (NOT IMPLEMENTED).
         */
        public int setControlMusic() {
            return -1;
        }


        // BLEInterface: getters for jacket state (since the last update)
        public String getDevicePlanName() {
            return valPlanName;
        }   // name of the current plan

        public int getDevicePlanState() {
            return valPlanState;
        }  // state of the training plan

        public int getDeviceCurrPhase() {
            return valCurrPhase;
        }  // current phase in the training plan

        public int getDeviceProgress() {
            return valProgress;
        }   // current progress within a training phase

        public int getDeviceCtrlMode() {
            return valCtrlMode;
        }   // current tactile control mode

        public int getDeviceBattery() {
            return valBattery;
        }    // current battery value


        /**
         * Callback for when a BLE device is discovered during scanning.
         * Called by nested class JacketScanCallback.
         */
        private void handleScanResult(BluetoothDevice device) {
            // only store devices that have a name (there may be many unnamed BLE devices around)
            if (device.getName() != null) {
                // update our entry of the device if it exists; or create a new one
                deviceMap.put(device.getAddress(), device);
                // trigger the callback on the parent
                onBleDeviceDiscovered(device.getName(), device.getAddress());

                // if there is a preferred address and we just discovered the corresponding device, connect to it
                if (targetMacAddress != null && targetMacAddress.equalsIgnoreCase(device.getAddress())) {
                    Log.i(TAG, "JacketScanCallback.handleScanResult(): found the target, stopping scan and connecting");
                    stopScanning();
                    connectTo(device.getAddress());
                }
            }
        }

        /**
         * Callback for when a new named BLE device was discovered during a scan.
         */
        private void onBleDeviceDiscovered(String name, String address) {
            Log.i(TAG, "onBLEDeviceDiscovered() <" + name + "> <" + address + ">");
        }

        /**
         * Creates a timer to stop the scanning operation after a default timeout.
         */
        protected void setTimerToStopScan() {
            setTimerToStopScan(DEFAULT_SCAN_TIMEOUT);
        }

        /**
         * Creates a timer to stop the scanning operation after the specified timeout (milliseconds).
         */
        private void setTimerToStopScan(int scanInterval) {
            stopScanTimer = new Timer();
            stopScanTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.i(TAG, "BLEJacket.setTimerToStopScan(): timer triggered to stop scan");
                    stopScanning();
                }
            }, scanInterval);
        }


        public class JacketScanCallback extends ScanCallback {

            private final String TAG = JacketScanCallback.class.getName();

            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                Log.i(TAG, ("JacketScanCallback.onScanResult(): <" + device.getName() + "> : <" + device.getAddress() + "> type " + device.getType()));
                handleScanResult(device);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                Log.i(TAG, "JacketScanCallback.onBatchScanResults(): got " + results.size() + " results");
                for (int i = 0; i < results.size(); i++) {
                    BluetoothDevice device = results.get(i).getDevice();
                    Log.i(TAG, "  [" + i + "]: <" + device.getName() + "> : <" + device.getAddress() + "> type " + device.getType());
                    handleScanResult(device);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.i(TAG, "JacketScanCallback.onScanFailed(): error code " + errorCode);
            }

        }

        public class JacketGatt extends BluetoothGattCallback {

            private final String TAG = JacketGatt.class.getName();

            Context context;

            /**
             * Reference to the bluetooth gatt object.
             */
            BluetoothGatt gatt;
            /**
             * Reference to the bluetooth device.
             */
            BluetoothDevice bluetoothDevice;
            /**
             * Reference to the jacket's GATT service.
             */
            BluetoothGattService gattService;

            /**
             * Reference to the characteristic timestamp.
             */
            BluetoothGattCharacteristic charactTimestamp;
            /**
             * Reference to the characteristic plan name.
             */
            BluetoothGattCharacteristic charactPlanName;
            /**
             * Reference to the characteristic state.
             */
            BluetoothGattCharacteristic charactState;
            /**
             * Reference to the characteristic client command.
             */
            BluetoothGattCharacteristic charactClientCmd;
            /**
             * Reference to the characteristic server message.
             */
            BluetoothGattCharacteristic charactServerMsg;

            /**
             * Flags if a GATT operation is currently in progress.
             */
            private boolean isGattBusy = false;

            /**
             * Stores a client (app) command to be sent to the server once GATT isn't busy.
             */
            private String queuedCommand = "";


            /**
             * Constructor.
             */
            JacketGatt(Context context) {
                super();
                this.context = context;
            }


            /**
             * Initializes the GATT connection.
             */
            void init(BluetoothDevice device) {
                bluetoothDevice = device;
                gatt = device.connectGatt(context, true, this);
            }


            /**
             * Called when the state of the BLE connection changes.
             * This is also signaled to the encapsulating app via the callback onBLEDeviceConnectionChange().
             */
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                Log.i(TAG, "JacketGatt.onConnectionStateChange()");

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.i(TAG, "JacketGatt.onConnectionStateChange(): connected");
                        if (!isGattConnected) {
                            isGattConnecting = false;
                            isGattConnected = true;
                            isGattReady = false;

                            this.gatt = gatt;
                            bluetoothDevice = gatt.getDevice();
                            // GATT is connected, but not yet configured; our first step is to set the MTU
                            Log.i(TAG, "JacketGatt.onConnectionStateChange(): requesting an MTU of " + BLE_JACKET_MTU_SIZE);
                            gatt.requestMtu(BLE_JACKET_MTU_SIZE);
                        }
                    } else {
                        Log.i(TAG, "JacketGatt.onConnectionStateChange(): disconnected");
                        isGattConnecting = false;
                        isGattConnected = false;
                        isGattReady = false;
                        isGattBusy = false;
                    }
                } else {
                    Log.i(TAG, "JacketGatt failed to change connection state; new state is: ");
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.i(TAG, "connected");
                        isGattConnected = true;
                        isGattBusy = false;
                    } else {
                        Log.i(TAG, "disconnected");
                        isGattConnected = false;
                        isGattReady = false;
                        isGattBusy = false;
                    }
                }

                onBLEDeviceConnectionChange(newState);
            }

            /**
             * Called when the MTU has changed. As a follow-up in our case, we proceed with discovering services.
             */
            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "JacketGatt.onMtuChanged() MTU successfully changed to " + mtu);
                } else {
                    Log.i(TAG, "JacketGatt.onMtuChanged() WARNING MTU change failed, current mtu " + mtu);
                }
                if (!isGattReady) {
                    Log.i(TAG, "JacketGatt discovering services");
                    // start discovering services, which calls onServicesDiscovered() when done
                    gatt.discoverServices();
                }
            }


            /**
             * Called when the BLE GATT service discovery has finished.
             * In our case, this will retrieve references to the service's characteristics; and attempt to
             * write the client config descriptor of the server message characteristic, to enable notifications.
             */
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                Log.i(TAG, "JacketGatt.onServicesDiscovered()");

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "JacketGatt.onServicesDiscovered(): success");

                    // store a reference to the Jacket service
                    gattService = gatt.getService(UUID.fromString(UUID_SERVICE));
                    if (gattService != null) {
                        Log.i(TAG, "JacketGatt got service " + gattService.getUuid().toString());

                        // store a reference to the timestamp characteristic
                        charactTimestamp = gattService.getCharacteristic(UUID.fromString(UUID_CHARACT_TIMESTAMP));
                        if (charactTimestamp != null) {
                            Log.i(TAG, "JacketGatt got characteristic timestamp " + charactTimestamp.getUuid().toString());
                        } else {
                            Log.i(TAG, "JacketGatt ERROR getting characteristic timestamp");
                        }

                        // store a reference to the plan name characteristic
                        charactPlanName = gattService.getCharacteristic(UUID.fromString(UUID_CHARACT_PLAN_NAME));
                        if (charactPlanName != null) {
                            Log.i(TAG, "JacketGatt got characteristic plan name " + charactPlanName.getUuid().toString());
                        } else {
                            Log.i(TAG, "JacketGatt ERROR getting characteristic plan name");
                        }

                        // store a reference to the jacket state characteristic
                        charactState = gattService.getCharacteristic(UUID.fromString(UUID_CHARACT_STATE));
                        if (charactState != null) {
                            Log.i(TAG, "JacketGatt got characteristic state " + charactState.getUuid().toString());
                        } else {
                            Log.i(TAG, "JacketGatt ERROR getting characteristic state");
                        }

                        // store a reference to the client command characteristic
                        charactClientCmd = gattService.getCharacteristic(UUID.fromString(UUID_CHARACT_CLIENT_CMD));
                        if (charactClientCmd != null) {
                            Log.i(TAG, "JacketGatt got characteristic client command " + charactState.getUuid().toString());
                        } else {
                            Log.i(TAG, "JacketGatt ERROR getting characteristic client command");
                        }

                        // store a reference to the server message characteristic
                        charactServerMsg = gattService.getCharacteristic(UUID.fromString(UUID_CHARACT_SERVER_MSG));
                        if (charactServerMsg != null) {
                            Log.i(TAG, "JacketGatt got characteristic server message " + charactState.getUuid().toString());

                            // configure the characteristic as notify
                            gatt.setCharacteristicNotification(charactServerMsg, true);

                            // enable remote notifications by writing the "client characteristic confguration" descriptor
                            Log.i(TAG, "JacketGatt getting descriptor");
                            BluetoothGattDescriptor descClientCharacteristicConfig = charactServerMsg.getDescriptor(UUID.fromString(UUID_DESCRIP_CCC));
                            if (descClientCharacteristicConfig == null) {
                                Log.i(TAG, "JacketGatt ERROR server message descriptor is null");
                            } else {
                                Log.i(TAG, "JacketGatt setting descriptor");
                                // set the descriptor's value
                                boolean result = descClientCharacteristicConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                Log.i(TAG, "JacketGatt set descriptor value: " + result);
                                Log.i(TAG, "JacketGatt writing client characteristic config descriptor");
                                // request writing the descriptor
                                // this will be the last step in configuring the BLE connection to the jacket
                                // which will trigger onDescriptorWrite() when done
                                result = gatt.writeDescriptor(descClientCharacteristicConfig);
                                Log.i(TAG, "JacketGatt write descriptor request result: " + result);
                            }
                        } else {
                            Log.i(TAG, "JacketGatt ERROR getting characteristic server message");
                        }

                    } else {
                        Log.i(TAG, "JacketGatt.onServicesDiscovered(): ERROR getting service");
                    }
                } else {
                    Log.i(TAG, "JacketGatt.onServicesDiscovered(): ERROR failed discovering services");
                }
            }


            /**
             * Called when a descriptor has been read; unused in our case.
             */
            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                Log.i(TAG, "JacketGatt onDescriptorRead");
            }


            /**
             * Called when a descriptor has been written.
             * In our case, as a result of writing the "client characteristic configuration"
             * to enable notifications for the jacket's state BLE characteristic.
             * after this, the object is ready to be used for interacting with the jacket.
             */
            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                Log.i(TAG, "JacketGatt.onDescriptorWrite() status " + status);
                isGattReady = true;
            }


            /**
             * Attempts to read BLE characteristics that convey the Jacket's state.
             */
            int updateState() {
                // if GATT is still being configured, return
                if (!isGattReady) {
                    Log.i(TAG, "JacketGatt.updateState() WARNING gatt isn't ready, dropping request");
                    return -1;
                }

                synchronized (this) {
                    // updates happen periodically, so they can be dropped if GATT is busy
                    if (isGattBusy) {
                        Log.i(TAG, "JacketGatt.updateState() WARNING gatt is busy, dropping request");
                        return -1;
                    }

                    isGattBusy = true;
                    // start by reading the "state" characteristic;
                    // once complete, this will trigger onCharacteristicRead(), which will handle things from there
                    gatt.readCharacteristic(jacketGatt.charactState);
                }
                return 0;
            }


            /**
             * Called when an observed characteristic (on the server/jacket) is read.
             * In our case, this is used to sequentially read the jacket's state-related characteristics.
             */
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                if (characteristic == charactState) {
                    valPlanState = charactState.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    valCurrPhase = charactState.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
                    valProgress = charactState.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 2);
                    valCtrlMode = charactState.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 3);
                    valBattery = charactState.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 4);

                    // request reading the current plan's name
                    // this will trigger this callback again; and move on to the next case
                    gatt.readCharacteristic(charactPlanName);
                } else if (characteristic == charactPlanName) {
                    valPlanName = charactPlanName.getStringValue(0);

                    // request reading the current timestamp
                    // this will trigger this callback again; and move on to the next case
                    gatt.readCharacteristic(charactTimestamp);
                } else if (characteristic == charactTimestamp) {
                    valTimestamp = charactTimestamp.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);

                    isGattBusy = false;
                    // now that GATT is free, handle any pending commands to be sent to the server (jacket)
                    if (queuedCommand.length() > 0) {
                        String command = queuedCommand;
                        queuedCommand = "";
                        sendClientCommand(command);
                    }
                }
            }


            /**
             * Send a client command as a JSON-formatted string to the server (jacket).
             */
            int sendClientCommand(String jsonString) {
                // if GATT is still being configured, return
                if (!isGattReady) {
                    Log.i(TAG, "JacketGatt.sendClientCommand() ERROR gatt isn't ready, dropping plan");
                    return -1;
                }

                Log.i(TAG, "BLEJacket.sendClientCommand()");
                Log.i(TAG, "--jsonString------------------");
                Log.i(TAG, jsonString);
                // create a JSON string without newline characters because the Arduino-side BLE library
                // splits strings on newline and this causes incomplete JSON chunks to be received
                String jsonTight = jsonString.replace("\n", "");
                Log.i(TAG, "--jsonTight------------------");
                Log.i(TAG, jsonTight);
                Log.i(TAG, "--------------------");

                synchronized (this) {
                    // if GATT is busy: store the command for sending later (when the current GATT op is complete)
                    if (isGattBusy) {
                        queuedCommand = jsonTight;
                        Log.i(TAG, "JacketGatt.sendClientCommand() WARNING gatt is busy, queuing");
                        return -1;
                    }

                    isGattBusy = true;
                    Log.i(TAG, "JacketGatt.sendClientCommand() writing");
                    // set the (local) value of the client command characteristic and request a write (update) of the characteristic
                    // once complete, this will trigger onCharacteristicWrite()
                    jacketGatt.charactClientCmd.setValue(jsonTight);
                    jacketGatt.gatt.writeCharacteristic(jacketGatt.charactClientCmd);
                }
                return 0;
            }


            /**
             * Called when a characteristic (on the server/jacket) is written.
             * In our case, this is used to confirm that a message was successfully sent to the server.
             */
            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                Log.i(TAG, "JacketGatt.onCharacteristicWrite()");
                if (characteristic == charactClientCmd) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.i(TAG, "JacketGatt.onCharacteristicWrite() client command successfully written");
                    } else {
                        Log.i(TAG, "JacketGatt.onCharacteristicWrite() ERROR writing client command, status: " + status);
                    }
                }
                isGattBusy = false;
            }


            /**
             * Called when an observed characteristic (on the server/jacket) changes its value.
             * In our case, this is only used to read incoming messages from the server
             * and notify the app via onServerMessageReceived().
             */
            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                Log.i(TAG, "JacketGatt.onCharacteristicChanged()");
                // if the server message characteristic changed, trigger the parent's callback
                if (characteristic == charactServerMsg) {
                    String message = charactServerMsg.getStringValue(0);
                    Log.i(TAG, "JacketGatt.onCharacteristicChanged() - serverMessage <" + message + ">");
                    try {
                        onServerMessageReceived(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }


            /**
             * Unused.
             */
            @Override
            public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                Log.i(TAG, "JacketGatt onPhyRead");
            }


            /**
             * Unused.
             */
            @Override
            public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                Log.i(TAG, "JacketGatt onPhyUpdate");
            }


            /**
             * Unused.
             */
            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                Log.i(TAG, "JacketGatt onReadRemoteRssi");
            }


            /**
             * Unused.
             */
            @Override
            public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
                Log.i(TAG, "JacketGatt onReliableWriteCompleted");
            }
        }

    }

}
