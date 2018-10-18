package com.adidas.hackathon.smartjacket.ble;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.adidas.hackathon.smartjacket.R;
import com.adidas.hackathon.smartjacket.fragments.DeviceScanFragment;
import com.adidas.hackathon.smartjacket.ui.ripple.RippleHelper;
import com.adidas.hackathon.smartjacket.util.UIUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A recycler view adapter that displays BLE devices.
 */
public class BleDevicesRecyclerAdapter extends RecyclerView.Adapter {

    private List<BleDeviceSimple> devices;
    private DeviceScanFragment deviceScanFragment;

    public BleDevicesRecyclerAdapter(Fragment fragment) {
        if (fragment.getClass().equals(DeviceScanFragment.class)) {
            this.deviceScanFragment = (DeviceScanFragment) fragment;
        }

        this.devices = new ArrayList<>();
    }

    public void addDevice(@NonNull BleDeviceSimple device) {
        for (int i = 0; i < devices.size(); i++) {
            BleDeviceSimple tempDevice = devices.get(i);
            if (tempDevice.mac().equals(device.mac())) {
                notifyItemChanged(i);
                return;
            }
        }

        BleDeviceSimple newDevice = new BleDeviceSimple();
        newDevice.setMac(device.mac());
        newDevice.setName(device.name());
        newDevice.setConnected(device.connected());
        this.devices.add(newDevice);
        notifyItemChanged(this.devices.size() - 1);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BleDevicesItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.ble_devices_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BleDevicesItemHolder) {
            BleDevicesItemHolder itemHolder = (BleDevicesItemHolder) holder;
            Context context = itemHolder.container.getContext();
            BleDeviceSimple device = devices.get(position);

            itemHolder.nameText.setText(device.name() == null ? "N/A" : device.name());
            itemHolder.macText.setText(device.mac() == null ? "" : device.mac());

            if (deviceScanFragment != null) {
                itemHolder.checkBox.setOnCheckedChangeListener(null);
                itemHolder.checkBox.setChecked(device.selected());
                itemHolder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        device.setSelected(true);
                        deviceScanFragment.addPinnedDevice(device);
                    } else {
                        device.setSelected(false);
                        deviceScanFragment.removePinnedDevice(devices.get(position));
                    }
                });
            }
            RippleHelper.applyRippleSimple(UIUtils.getColor(context, R.color.white_40_transparent), itemHolder.container);
        }
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }
}
