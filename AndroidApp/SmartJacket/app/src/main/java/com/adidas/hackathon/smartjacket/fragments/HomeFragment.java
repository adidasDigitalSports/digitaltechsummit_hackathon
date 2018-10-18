package com.adidas.hackathon.smartjacket.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.adidas.hackathon.smartjacket.HomeActivity;
import com.adidas.hackathon.smartjacket.R;
import com.adidas.hackathon.smartjacket.settings.AppSharedPreferences;
import com.adidas.hackathon.smartjacket.ui.controls.AdidasTextView;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeFragment extends Fragment {

    public static final String TAG = HomeFragment.class.getName();

    @BindView(R.id.home_screen_headline)
    AdidasTextView headlineTextView;
    @BindView(R.id.home_screen_jacket)
    ImageView jacket;
    @BindView(R.id.home_screen_jacket_name)
    AdidasTextView jacketName;
    @BindView(R.id.home_screen_jacket_address)
    AdidasTextView jacketAdress;
    @BindView(R.id.home_screen_jacket_connect_button)
    ImageView jacketConnectButton;

    private HomeActivity homeActivity;

    private Fragment currentFragment;
    private String currentTag;

    public static HomeFragment newInstance() {
        Bundle args = new Bundle();

        HomeFragment homeFragment = new HomeFragment();
        homeFragment.setArguments(args);
        return homeFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, rootView);

        homeActivity = (HomeActivity) getActivity();

        headlineTextView.setText(R.string.home_instruction_two);

        if (DeviceScanFragment.selectedBleDevices != null && DeviceScanFragment.selectedBleDevices.size() > 0) {
            headlineTextView.setText(R.string.home_instruction_three);
        }
        jacketName.setText(AppSharedPreferences.getBleDeviceName(Objects.requireNonNull(getContext())));
        jacketAdress.setText(AppSharedPreferences.getBleDeviceMac(getContext()));

        return rootView;
    }

    @OnClick({R.id.home_screen_jacket, R.id.home_screen_jacket_connect_button, R.id.home_screen_phone, R.id.home_screen_phone_headline, R.id.home_screen_plans, R.id.home_screen_plans_headline})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_screen_jacket:
                currentFragment = DeviceScanFragment.newInstance();
                currentTag = DeviceScanFragment.TAG;
                addFragment();
                break;
            case R.id.home_screen_jacket_connect_button:
                if (!AppSharedPreferences.getBleDeviceMac(Objects.requireNonNull(getContext())).equalsIgnoreCase(getResources().getString(R.string.mac))) {
                    if (!homeActivity.ble.isConnected()) {
                        homeActivity.ble.connectTo(AppSharedPreferences.getBleDeviceMac(getContext()));
                    } else {
                        homeActivity.ble.disconnect();
                    }
                } else {
                    Toast.makeText(getContext(), "Please select your smart jacket first.", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.home_screen_phone:
                currentFragment = FavContactFragment.newInstance();
                currentTag = FavContactFragment.TAG;
                addFragment();
                break;
            case R.id.home_screen_phone_headline:
                currentFragment = FavContactFragment.newInstance();
                currentTag = FavContactFragment.TAG;
                addFragment();
                break;
            case R.id.home_screen_plans:
                currentFragment = PlanFragment.newInstance();
                currentTag = PlanFragment.TAG;
                addFragment();
                break;
            case R.id.home_screen_plans_headline:
                currentFragment = PlanFragment.newInstance();
                currentTag = PlanFragment.TAG;
                addFragment();
                break;
        }
    }

    /**
     * Add the fragment to the back stack.
     */
    public void addFragment() {
        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_main, currentFragment, currentTag)
                .addToBackStack(null)
                .commit();
    }

}
