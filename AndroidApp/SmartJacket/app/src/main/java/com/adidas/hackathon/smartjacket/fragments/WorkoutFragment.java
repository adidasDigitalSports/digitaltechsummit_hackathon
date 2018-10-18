package com.adidas.hackathon.smartjacket.fragments;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adidas.hackathon.smartjacket.ble.BleInterface;
import com.adidas.hackathon.smartjacket.HomeActivity;
import com.adidas.hackathon.smartjacket.R;
import com.adidas.hackathon.smartjacket.settings.AppSharedPreferences;
import com.adidas.hackathon.smartjacket.ui.controls.AdidasTextView;
import com.adidas.hackathon.smartjacket.util.Plans;
import com.adidas.hackathon.smartjacket.util.UIUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;


public class WorkoutFragment extends Fragment {

    public static final String TAG = WorkoutFragment.class.getName();

    @BindView(R.id.workout_headline)
    AdidasTextView headlineTextView;
    @BindView(R.id.plan_name)
    AdidasTextView planNameTextView;
    @BindView(R.id.curr_phase)
    AdidasTextView currPhaseTextView;
    @BindView(R.id.plan_progress)
    AdidasTextView planProgressTextView;
    @BindView(R.id.workout_chart)
    LineChart workoutChart;

    private Handler handler = new Handler();

    private HomeActivity homeActivity;
    private int trainingPlan;

    List<Entry> entriesTrainingPlan = new ArrayList<>();
    List<Entry> entriesProgress = new ArrayList<>();

    LineDataSet lineDataSetTrainingPlan;
    LineDataSet lineDataSetProgress;

    List<ILineDataSet> dataSets;

    LineData lineData;

    public static WorkoutFragment newInstance() {
        Bundle args = new Bundle();

        WorkoutFragment fragment = new WorkoutFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_workout, container, false);
        ButterKnife.bind(this, rootView);

        homeActivity = (HomeActivity) getActivity();

        String jsonPlan = AppSharedPreferences.getTrainingPlan(Objects.requireNonNull(getContext()));
        trainingPlan = jsonPlan.equalsIgnoreCase(Plans.jsonStringPlanOne) ? 1 : 2;
        drawChart(0);

        startCheckingForServerUpdates();

        return rootView;
    }

    private void startCheckingForServerUpdates() {
        Log.i(TAG, "Start checking for server updates");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (homeActivity.ble.isConnected()) {
                    Log.i(TAG, "Read device status.");
                    if (homeActivity.ble.isReady()) {
                        homeActivity.ble.updateState();
                        updateInterface();
                    }
                    handler.postDelayed(this, BleInterface.DEVICE_UPDATE_INTERVAL);
                }
            }
        }, BleInterface.DEVICE_UPDATE_INTERVAL);
    }

    private void drawChart(int currentPhase) {
        entriesTrainingPlan = new ArrayList<>();
        entriesProgress = new ArrayList<>();

        if (currentPhase < 1) {
            setupDataTrainingPlan();
        }
        setupDataProgress(currentPhase);
        setupDataSets(currentPhase);
        setupChart();
    }

    private void setupDataTrainingPlan() {
        if (trainingPlan == 1) {
            for (Point point : Plans.planOne) {
                entriesTrainingPlan.add(new Entry(point.x, point.y));
            }
        } else {
            for (Point point : Plans.planTwo) {
                entriesTrainingPlan.add(new Entry(point.x, point.y));
            }
        }
        setupLineDataSetTrainingPlan();
    }

    private void setupDataProgress(int currentPhase) {
        if (currentPhase > 0) {
            if (trainingPlan == 1) {
                entriesProgress.add(new Entry(Plans.planOne[(currentPhase * 2) - 2].x, Plans.planOne[(currentPhase * 2) - 2].y + 3));
                entriesProgress.add(new Entry(Plans.planOne[(currentPhase * 2) - 1].x, Plans.planOne[(currentPhase * 2) - 1].y + 3));
            } else {
                entriesProgress.add(new Entry(Plans.planTwo[(currentPhase * 2) - 2].x, Plans.planTwo[(currentPhase * 2) - 2].y + 1));
                entriesProgress.add(new Entry(Plans.planTwo[(currentPhase * 2) - 1].x, Plans.planTwo[(currentPhase * 2) - 1].y + 1));
            }
            setupLineDataSetProgress();
        }
    }

    private void setupLineDataSetTrainingPlan() {
        lineDataSetTrainingPlan = new LineDataSet(entriesTrainingPlan, "");
        lineDataSetTrainingPlan.setColor(UIUtils.getColor(getContext(), R.color.secondary));
        lineDataSetTrainingPlan.setDrawCircles(false);
        lineDataSetTrainingPlan.setDrawValues(false);
        lineDataSetTrainingPlan.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        lineDataSetTrainingPlan.setLineWidth(1.5f);
    }

    private void setupLineDataSetProgress() {
        lineDataSetProgress = new LineDataSet(entriesProgress, "");
        lineDataSetProgress.setColor(UIUtils.getColor(getContext(), R.color.secondary));
        lineDataSetProgress.setDrawCircles(false);
        lineDataSetProgress.setDrawValues(false);
        lineDataSetProgress.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        lineDataSetProgress.setLineWidth(3.0f);
    }

    private void setupDataSets(int currentPhase) {
        dataSets = new ArrayList<>();
        dataSets.add(lineDataSetTrainingPlan);
        if (currentPhase > 0) {
            dataSets.add(lineDataSetProgress);
        }
    }

    private void setupChart() {
        workoutChart.getAxisLeft().setAxisMinimum(0);
        workoutChart.getAxisLeft().setAxisMaximum(102);
        workoutChart.getAxisRight().setAxisMinimum(0);
        workoutChart.getAxisRight().setAxisMaximum(102);

        workoutChart.getLegend().setEnabled(false);
        workoutChart.getDescription().setEnabled(false);

        lineData = new LineData(dataSets);
        workoutChart.setData(lineData);
        workoutChart.invalidate();
    }

    private void updateInterface() {
        Log.i(TAG, "Update Interface");
        int currentPhase = homeActivity.ble.getDeviceCurrPhase();
        int progress = homeActivity.ble.getDeviceProgress();

        if (homeActivity.ble.getDevicePlanState() == BleInterface.PLAN_STATE_READY) {
            headlineTextView.setText(R.string.workout_press_jacket_label);
        }
        if (homeActivity.ble.getDevicePlanState() == BleInterface.PLAN_STATE_STARTED) {
            headlineTextView.setText(R.string.workout_ongoing);
        }
        if (homeActivity.ble.getDevicePlanState() == BleInterface.PLAN_STATE_PAUSED) {
            headlineTextView.setText(R.string.workout_paused);
        }
        if (homeActivity.ble.getDevicePlanState() == BleInterface.PLAN_STATE_ENDED) {
            headlineTextView.setText(R.string.workout_finished);
        }

        planNameTextView.setText(homeActivity.ble.getDevicePlanName());
        currPhaseTextView.setText(String.valueOf(currentPhase));
        planProgressTextView.setText(String.valueOf(progress));

        drawChart(currentPhase);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

}
