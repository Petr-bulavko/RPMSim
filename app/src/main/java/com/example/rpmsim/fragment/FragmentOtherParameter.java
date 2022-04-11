package com.example.rpmsim.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import com.example.rpmsim.R;
import com.example.rpmsim.entity.Detector;
import com.example.rpmsim.entity.Source;

import java.util.ArrayList;

public class FragmentOtherParameter extends Fragment implements View.OnClickListener {

    EditText txtHoldingTime, txtTravelSpeedSource, txtSourcePathLength, txtNumberOfMovements, txtNumberOfThresholds, txtWorkingArray,
            txtBackgroundArray, txtUnusedThresholds, txtFalseAlarmPeriod, txtDetectionProbability, txtFalseAlarmRate;
    CheckBox txtInterrupt;
    Spinner txtBackgroundMode;

    ArrayList<Source> sources;
    ArrayList<Detector> detectors;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_other_parameter, container, false);

        txtHoldingTime = result.findViewById(R.id.holdingTime);
        txtHoldingTime.setText("100");//Время выдержки БД
        txtTravelSpeedSource = result.findViewById(R.id.travelSpeedSource);
        txtTravelSpeedSource.setText("1");//Скорость перемещения источника
        txtSourcePathLength = result.findViewById(R.id.sourcePathLength);
        txtSourcePathLength.setText("10");//Длина пути перемещения
        txtNumberOfMovements = result.findViewById(R.id.numberOfMovements);
        txtNumberOfMovements.setText("1000");//Количество перемещений
        txtNumberOfThresholds = result.findViewById(R.id.numberOfThresholds);
        txtNumberOfThresholds.setText("6");//Количество порогов
        txtWorkingArray = result.findViewById(R.id.workingArray);
        txtWorkingArray.setText("30");//Кол-во вр. инт. в рабочем массиве
        txtBackgroundArray = result.findViewById(R.id.backgroundArray);
        txtBackgroundArray.setText("60");//Кол-во вр. инт. фона
        txtUnusedThresholds = result.findViewById(R.id.unusedThresholds);
        txtUnusedThresholds.setText("0");//Количество неиспользуемых первых порогов
        txtFalseAlarmPeriod = result.findViewById(R.id.falseAlarmPeriod);
        txtFalseAlarmPeriod.setText("3600");//Период ложных тревог
        txtDetectionProbability = result.findViewById(R.id.detectionProbability);
        txtDetectionProbability.setText("95");//Вероятность обнаружения
        txtFalseAlarmRate = result.findViewById(R.id.falseAlarmRate);
        txtFalseAlarmRate.setText("1000");//Частота ложных тревог 1 на, проезды
        txtInterrupt = result.findViewById(R.id.interrupt);
        txtInterrupt.setChecked(true);//Прерывание перемещения при первом срабатывании, можно использовать проверку и менять надпись если вкл. кнопка или выкл.
        txtBackgroundMode = result.findViewById(R.id.backgroundMode);//Режим фона


        txtInterrupt.setText("Да");
        txtInterrupt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtInterrupt.isChecked()) {
                    txtInterrupt.setText("Да");
                } else {
                    txtInterrupt.setText("Нет");
                }
            }
        });

        Button button_update = result.findViewById(R.id.button_update_false_alarm);
        button_update.setOnClickListener(this);

        return result;
    }

    @Override
    public void onResume() {
        super.onResume();

        //Оооооо это работает, но только для одного фрагмента, это проблема
        //Передать значения цепочкой?
        getParentFragmentManager().setFragmentResultListener("request_sources_and_detectors", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                sources = (ArrayList<Source>) result.getSerializable("sources");
                detectors = (ArrayList<Detector>) result.getSerializable("detectors");
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        Bundle bundle = new Bundle();
        bundle.putDouble("txtHoldingTime", Double.parseDouble(txtHoldingTime.getText().toString()));
        bundle.putDouble("txtTravelSpeedSource", Double.parseDouble(txtTravelSpeedSource.getText().toString()));
        bundle.putDouble("txtSourcePathLength", Double.parseDouble(txtSourcePathLength.getText().toString()));
        bundle.putDouble("txtNumberOfMovements", Double.parseDouble(txtNumberOfMovements.getText().toString()));
        bundle.putDouble("txtNumberOfThresholds", Double.parseDouble(txtNumberOfThresholds.getText().toString()));
        bundle.putDouble("txtWorkingArray", Double.parseDouble(txtWorkingArray.getText().toString()));
        bundle.putDouble("txtBackgroundArray", Double.parseDouble(txtBackgroundArray.getText().toString()));
        bundle.putDouble("txtUnusedThresholds", Double.parseDouble(txtUnusedThresholds.getText().toString()));
        bundle.putDouble("txtFalseAlarmPeriod", Double.parseDouble(txtFalseAlarmPeriod.getText().toString()));
        bundle.putDouble("txtDetectionProbability", Double.parseDouble(txtDetectionProbability.getText().toString()));
        bundle.putDouble("txtFalseAlarmRate", Double.parseDouble(txtFalseAlarmRate.getText().toString()));
        bundle.putInt("txtBackgroundMode", txtBackgroundMode.getSelectedItemPosition());
        bundle.putSerializable("sources", sources);
        bundle.putSerializable("detectors", detectors);
        boolean interrupt = false;
        if (txtInterrupt.isChecked()) {
            interrupt = true;
        }
        bundle.putBoolean("txtInterrupt", interrupt);


        getParentFragmentManager().setFragmentResult("request_all", bundle);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onClick(View v) {
        double update = Double.parseDouble(txtFalseAlarmRate.getText().toString()) * Double.parseDouble(txtSourcePathLength.getText().toString());
        txtFalseAlarmPeriod.setText(String.format("%.0f", update));
    }
}
