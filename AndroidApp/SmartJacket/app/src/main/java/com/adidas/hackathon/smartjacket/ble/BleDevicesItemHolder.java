package com.adidas.hackathon.smartjacket.ble;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.adidas.hackathon.smartjacket.R;

import butterknife.BindView;
import butterknife.ButterKnife;

class BleDevicesItemHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.ble_devices_item_container)
    ConstraintLayout container;
    @BindView(R.id.ble_devices_item_name)
    TextView nameText;
    @BindView(R.id.ble_devices_item_mac)
    TextView macText;
    @BindView(R.id.ble_devices_check_box)
    CheckBox checkBox;

    BleDevicesItemHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

}
