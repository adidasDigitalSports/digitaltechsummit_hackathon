package com.adidas.hackathon.smartjacket.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.adidas.hackathon.smartjacket.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceStatusLayout extends LinearLayout {

    public static final String TAG = DeviceStatusLayout.class.getName();

    @BindView(R.id.progress_spinner)
    ProgressBar progressSpinner;
    @BindView(R.id.ble_connection_status)
    TextView bleConnectionStatusTextView;
    @BindView(R.id.bluetooth_status_icon)
    ImageView bleStatusImageView;

    @SensorStatus
    private int sensorStatus = SensorStatus.NOT_CONNECTED;


    public DeviceStatusLayout(Context context) {
        this(context, null);
    }

    public DeviceStatusLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DeviceStatusLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateAndBind(context);
    }

    private void inflateAndBind(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View rootView = inflater.inflate(R.layout.device_status, this, true);
        ButterKnife.bind(this, rootView);
    }

    @SuppressLint("SwitchIntDef")
    public void setStatus(@SensorStatus int status) {
        sensorStatus = status;
        switch (status) {
            case SensorStatus.BLUETOOTH_OFF:
                progressSpinner.setVisibility(INVISIBLE);
                bleStatusImageView.setImageResource(R.drawable.ic_bluetooth_not_connected);
                break;
            case SensorStatus.NO_BLUETOOTH_PERMISSION:
                progressSpinner.setVisibility(INVISIBLE);
                bleStatusImageView.setImageResource(R.drawable.ic_bluetooth_not_connected);
                break;
            case SensorStatus.NOT_CONNECTED:
                progressSpinner.setVisibility(INVISIBLE);
                bleConnectionStatusTextView.setText(R.string.home_not_connected);
                bleStatusImageView.setImageResource(R.drawable.ic_bluetooth_not_connected);
                break;
            case SensorStatus.CONNECTED:
                progressSpinner.setVisibility(INVISIBLE);
                bleConnectionStatusTextView.setText(R.string.home_connected);
                bleStatusImageView.setImageResource(R.drawable.ic_bluetooth_connected);
                break;
            case SensorStatus.CONNECTING:
                progressSpinner.setVisibility(VISIBLE);
                bleConnectionStatusTextView.setText(R.string.home_connecting);
                bleStatusImageView.setImageResource(R.drawable.ic_bluetooth_not_connected);
                break;
        }
    }

    @SensorStatus
    public int getSensorStatus() {
        return sensorStatus;
    }

}
