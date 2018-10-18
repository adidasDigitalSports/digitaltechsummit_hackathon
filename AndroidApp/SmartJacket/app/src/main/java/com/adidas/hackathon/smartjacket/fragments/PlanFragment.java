package com.adidas.hackathon.smartjacket.fragments;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.adidas.hackathon.smartjacket.ble.BleInterface;
import com.adidas.hackathon.smartjacket.HomeActivity;
import com.adidas.hackathon.smartjacket.util.Plans;
import com.adidas.hackathon.smartjacket.R;
import com.adidas.hackathon.smartjacket.settings.AppSharedPreferences;
import com.adidas.hackathon.smartjacket.ui.controls.AdidasButton;
import com.adidas.hackathon.smartjacket.ui.controls.AdidasTextView;
import com.adidas.hackathon.smartjacket.util.UIUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PlanFragment extends Fragment {

    public static final String TAG = PlanFragment.class.getName();

    @BindView(R.id.plan_one)
    LinearLayout planOneLayout;
    @BindView(R.id.plan_two)
    LinearLayout planTwoLayout;
    @BindView(R.id.chart_headline)
    AdidasTextView chartHeadline;
    @BindView(R.id.checkbox_plan_one)
    ImageView checkboxPlanOne;
    @BindView(R.id.checkbox_plan_two)
    ImageView checkboxPlanTwo;
    @BindView(R.id.training_plan_chart)
    LineChart trainingPlanChart;
    @BindView(R.id.choose_selected_plan_button)
    AdidasButton selectPlanButton;

    private HomeActivity homeActivity;
    private String jsonStringPlan;

    private Handler handler = new Handler();

    public static PlanFragment newInstance() {
        Bundle args = new Bundle();

        PlanFragment fragment = new PlanFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_plan, container, false);
        ButterKnife.bind(this, rootView);

        homeActivity = (HomeActivity) getActivity();

        jsonStringPlan = Plans.jsonStringPlanOne;
        planOneLayout.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.secondary_30_transparent));
        checkboxPlanOne.setImageResource(R.drawable.ic_checkbox_checked);
        setupLineChart(1);

        return rootView;
    }

    @OnClick({R.id.plan_one, R.id.plan_two, R.id.choose_selected_plan_button})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.plan_one:
                setupLineChart(1);
                jsonStringPlan = Plans.jsonStringPlanOne;
                planOneLayout.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.secondary_30_transparent));
                checkboxPlanOne.setImageResource(R.drawable.ic_checkbox_checked);
                planTwoLayout.setBackgroundColor(Color.TRANSPARENT);
                checkboxPlanTwo.setImageResource(R.drawable.checkbox_unchecked);
                chartHeadline.setText(R.string.plan_one);
                break;
            case R.id.plan_two:
                setupLineChart(2);
                jsonStringPlan = Plans.jsonStringPlanTwo;
                planOneLayout.setBackgroundColor(Color.TRANSPARENT);
                checkboxPlanOne.setImageResource(R.drawable.checkbox_unchecked);
                planTwoLayout.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.secondary_30_transparent));
                checkboxPlanTwo.setImageResource(R.drawable.ic_checkbox_checked);
                chartHeadline.setText(R.string.plan_two);
                break;
            case R.id.choose_selected_plan_button:
                if (homeActivity.ble.isConnected() && homeActivity.ble.isReady()) {
                    homeActivity.ble.sendPlan(jsonStringPlan);
                    selectPlanButton.setEnabled(false);
                    Animation rotation = AnimationUtils.loadAnimation(getContext(), R.anim.alternating_transparency_animation);
                    selectPlanButton.startAnimation(rotation);
                    checkForServerAcknowledgement();
                } else {
                    Toast.makeText(getContext(), "Please connect to your smart jacket first.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void setupLineChart(int trainingPlan) {
        LineDataSet dataSet;
        List<Entry> entries = new ArrayList<>();

        if (trainingPlan == 1) {
            for (Point point : Plans.planOne) {
                entries.add(new Entry(point.x, point.y));
            }
        } else {
            for (Point point : Plans.planTwo) {
                entries.add(new Entry(point.x, point.y));
            }
        }
        dataSet = new LineDataSet(entries, ""); // add entries to dataset
        dataSet.setColor(UIUtils.getColor(getContext(), R.color.secondary));
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSet.setLineWidth(1.5f);

        trainingPlanChart.getAxisLeft().setAxisMinimum(0);
        trainingPlanChart.getAxisLeft().setAxisMaximum(102);
        trainingPlanChart.getAxisRight().setAxisMinimum(0);
        trainingPlanChart.getAxisRight().setAxisMaximum(102);

        trainingPlanChart.getLegend().setEnabled(false);
        trainingPlanChart.getDescription().setEnabled(false);

        LineData lineData = new LineData(dataSet);
        trainingPlanChart.setData(lineData);

        trainingPlanChart.invalidate();
    }

    private void checkForServerAcknowledgement() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Performing check for server acknowledgment");
                if (HomeActivity.planAcknowledged) {
                    selectPlanButton.clearAnimation();
                    AppSharedPreferences.saveTrainingPlan(PlanFragment.this.getContext(), jsonStringPlan);
                    HomeActivity.planAcknowledged = false;
                    PlanFragment.this.addWorkoutFragment();
                } else {
                    Log.i(TAG, "Check again.");
                    handler.postDelayed(this, BleInterface.DEVICE_UPDATE_INTERVAL);
                }
            }
        }, BleInterface.DEVICE_UPDATE_INTERVAL);
    }

    /**
     * Add the fragment to the back stack.
     */
    private void addWorkoutFragment() {
        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_main, WorkoutFragment.newInstance(), WorkoutFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (handler != null) {
            Log.i(TAG, "On pause removes callbacks");
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        homeActivity.ble.stopScanning();
    }

}
