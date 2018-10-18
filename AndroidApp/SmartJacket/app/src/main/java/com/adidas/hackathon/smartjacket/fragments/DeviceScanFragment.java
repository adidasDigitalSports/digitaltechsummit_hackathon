package com.adidas.hackathon.smartjacket.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adidas.hackathon.smartjacket.ble.BleDeviceSimple;
import com.adidas.hackathon.smartjacket.ble.BleDevicesRecyclerAdapter;
import com.adidas.hackathon.smartjacket.ble.BleInterface;
import com.adidas.hackathon.smartjacket.HomeActivity;
import com.adidas.hackathon.smartjacket.R;
import com.adidas.hackathon.smartjacket.settings.AppSharedPreferences;
import com.adidas.hackathon.smartjacket.ui.DividerDecoration;
import com.adidas.hackathon.smartjacket.ui.controls.AdidasButton;
import com.adidas.hackathon.smartjacket.util.PermissionsHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.CompositeDisposable;

public class DeviceScanFragment extends Fragment {

    public static final String TAG = DeviceScanFragment.class.getName();

    @BindView(R.id.ble_devices_scan_button)
    AdidasButton scanButton;
    @BindView(R.id.ble_devices_recycler)
    RecyclerView recyclerView;

    private boolean scanning;
    private Handler handler = new Handler();
    private Runnable runnable;

    ArrayList<BleDeviceSimple> foundBleDevices = new ArrayList<>();
    public static List<BleDeviceSimple> selectedBleDevices;
    private BleDevicesRecyclerAdapter recyclerAdapter;
    private CompositeDisposable disposables = new CompositeDisposable();

    HomeActivity homeActivity;

    public DeviceScanFragment() {
    }

    public static DeviceScanFragment newInstance() {
        Bundle args = new Bundle();

        DeviceScanFragment fragment = new DeviceScanFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_device_scan, container, false);
        ButterKnife.bind(this, rootView);

        scanButton.setEnabled(false);

        PermissionsHandler permissionsHandler = new PermissionsHandler(getContext());
        disposables.add(permissionsHandler.getRxPermissions()
                .request(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(granted -> {
                    if (!granted) {
                        Objects.requireNonNull(getActivity()).onBackPressed();
                    } else {
                        Context context = getContext();
                        recyclerAdapter = new BleDevicesRecyclerAdapter(this);
                        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
                        recyclerView.setAdapter(recyclerAdapter);
                        assert context != null;
                        recyclerView.addItemDecoration(new DividerDecoration(context, ContextCompat.getColor(context, R.color.medium_grey_40_transparent)));

                        selectedBleDevices = new ArrayList<>();

                        homeActivity = (HomeActivity) getActivity();
                        if (homeActivity != null) {
                            homeActivity.ble.init();
                            scanButton.setEnabled(true);
                        } else {
                            getActivity().onBackPressed();
                        }
                    }
                }));
        return rootView;
    }

    @SuppressLint("SetTextI18n")
    @OnClick(R.id.ble_devices_scan_button)
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ble_devices_scan_button:
                if (!scanning) {
                    scanButton.setText(R.string.stop_scanning);
                    startScanning();
                } else {
                    homeActivity.ble.stopScanning();
                    handler.removeCallbacks(runnable);
                    scanButton.setText(R.string.scan);
                }
                scanning = !scanning;
                break;
        }
    }

    private void startScanning() {
        homeActivity.ble.startScanning(BleInterface.DEVICE_SCAN_INTERVAL);
        updateUi();
    }

    private void updateUi() {
        runnable = () -> {
            try {
                foundBleDevices = homeActivity.ble.getDeviceList();
                if (foundBleDevices.size() != 0) {
                    for (int i = 0; i < foundBleDevices.size(); i++) {
                        recyclerAdapter.addDevice(new BleDeviceSimple(foundBleDevices.get(i).name(), foundBleDevices.get(i).mac()));
                    }
                }
            } finally {
                handler.postDelayed(runnable, 2000);
            }
        };
        runnable.run();
    }

    public void addPinnedDevice(BleDeviceSimple bleDevice) {
        selectedBleDevices.add(bleDevice);
    }

    public void removePinnedDevice(BleDeviceSimple bleDevice) {
        Iterator<BleDeviceSimple> iterator = selectedBleDevices.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().mac().equals(bleDevice.mac())) {
                iterator.remove();
                break;
            }
        }
    }

    @Override
    public void onPause() {
        Log.i(TAG, "on pause");
        super.onPause();
        if (runnable != null && handler != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "on destroy");
        super.onDestroy();
        if (selectedBleDevices.size() > 0) {
            AppSharedPreferences.saveBleDeviceName(Objects.requireNonNull(getContext()), selectedBleDevices.get(0).name());
            AppSharedPreferences.saveBleDeviceMac(getContext(), selectedBleDevices.get(0).mac());
        }
        disposables.dispose();
        homeActivity.ble.stopScanning();
    }

}
