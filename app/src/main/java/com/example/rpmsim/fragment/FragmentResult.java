package com.example.rpmsim.fragment;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rpmsim.R;
import com.example.rpmsim.calc_alarm.Alarm;
import com.example.rpmsim.database.Constants;
import com.example.rpmsim.database.DatabaseHelper;
import com.example.rpmsim.entity.Detector;
import com.example.rpmsim.entity.Shield;
import com.example.rpmsim.entity.Source;

import org.apache.commons.math3.distribution.PoissonDistribution;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class FragmentResult extends Fragment implements View.OnClickListener {

    final String LOG_TAG = "myLogs";

    private Cursor cursor;
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;

    private TextView numberOfAlarms, numberOfMovementsResult, detectionProbabilityResult, duration, plannedDuration, txtSigma, txtDoseRateAndCountRate, txtBackground;
    private ListView sigmaResult;
    private ListView detectorResult;

    private ArrayList<Detector> detectors;
    private ArrayList<Source> sources;
    private ArrayList<Shield> shields;
    ArrayList<Double> coef;
    //Время выдержки БД, мс
    private double holdingTime;
    // м/с
    private double speed;
    // м
    private double distance;
    //Количество перемещений
    private double numberOfMovements;
    //Количество порогов
    private double numberOfThresholds;
    //Кол-во вр. инт. в рабочем массиве
    private double workingArray;
    //Кол-во вр. инт. фона
    private double backgroundArray;
    //Количество неиспользуемых первых порогов
    private double unusedThresholds;
    //Период ложных тревог
    private double falseAlarmPeriod;
    //Вероятность обнаружения
    private double detectionProbability;
    //Частота ложных тревог 1 на, проезды
    private double falseAlarmRate;
    //Прерывание перемещения при первом срабатывании
    private boolean interrupt;
    // 1 - адаптивный, 2 - константный, 3 - вручную
    private int backgroundMode;
    //Счетчик тревог
    private int countAlarm;
    //Типо время в мс, но хз
    private int countTime;
    private double time;
    private double bckg;
    private int numberOfGapsInSecond;
    private String[] sigma_array;
    private ArrayList<String> txtDetectors;
    private ArrayAdapter<String> adapter_sigma;
    private ArrayAdapter<String> adapter_detectors;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_result, container, false);
        Log.d(LOG_TAG, "onCreateView_fragment_result");
        numberOfAlarms = result.findViewById(R.id.numberOfAlarms);
        numberOfAlarms.setText("0");
        numberOfMovementsResult = result.findViewById(R.id.numberOfMovementsResult);
        numberOfMovementsResult.setText("0");
        detectionProbabilityResult = result.findViewById(R.id.detectionProbabilityResult);
        detectionProbabilityResult.setText("0.0");
        duration = result.findViewById(R.id.duration);
        duration.setText("0");
        plannedDuration = result.findViewById(R.id.plannedDuration);
        plannedDuration.setText("0");
        txtBackground = result.findViewById(R.id.textView23);
        txtBackground.setText("0");
        sigmaResult = result.findViewById(R.id.sigmaResult);
        detectorResult = result.findViewById(R.id.detectorResult);
        txtSigma = result.findViewById(R.id.textView20);
        txtDoseRateAndCountRate = result.findViewById(R.id.textView21);

        Button detectionResult = result.findViewById(R.id.detectionResult);
        detectionResult.setOnClickListener(this);

        databaseHelper = new DatabaseHelper(getActivity());
        databaseHelper.create_db();
        return result;
    }

    @SuppressLint({"DefaultLocale", "Range"})
    @Override
    public void onClick(View v) {
        detectors = FragmentDetector.getDetectors();
        sources = FragmentAddSource.getSources();
        shields = FragmentAddMaterial.getShields();
        coef = new ArrayList<>();

        for (int j = 0; j < sources.size(); j++) {
            for (int i = 0; i < shields.size(); i++) {
                cursor = db.rawQuery("select * from " + Constants.TABLE_SHIELD_SOURCE + " where " + Constants.COLUMN_ID_SHIELD_SOURCE +
                        "=" + shields.get(i).getId() + " and " + Constants.COLUMN_ID_SOURCE_SHIELD + "=" + (sources.get(j).getPositionInSpinner() + 1), null);
                while (cursor.moveToNext()) {
                    coef.add(cursor.getDouble(cursor.getColumnIndex(Constants.COLUMN_SOURCE_SHIELD_VALUE)));
                }
            }
        }

        Log.d(LOG_TAG, "onClick_fragment_result");
        // 10000мc
        time = distance / speed * 1000;
        // 0.001м в 1мс
        double distanceInMilliSecond = speed / 1000;
        //Начало по x
        double sourceStartPointX;
        // скорость счета за 100мс
        double sumCountRate = 0;
        // просто счетчик для 100мс
        int count = 0;
        //Определение тревоги
        boolean alarm;
        //Сколько раз детектор собирает информацию в 1с
        numberOfGapsInSecond = (int) (1000 / holdingTime);
        countAlarm = 0;
        countTime = 0;
        //Вероятность порога
        double alarm_period = 1 / falseAlarmPeriod / numberOfGapsInSecond;

        Alarm alarmClass = new Alarm(backgroundArray, numberOfThresholds, alarm_period, backgroundMode);
        //Сумма фоновых значений детекторов
        double sumBackground = 0;
        for (int i = 0; i < detectors.size(); i++) {
            sumBackground += detectors.get(i).getBackground();
        }
        //Т.к. фон у нас в имп/с, то фон делим на число проходов (сколько раз детектор собирает информацию в с)
        double backgroundGaps = sumBackground / numberOfGapsInSecond;
        //
        double backgroundInMillis = sumBackground * 0.001;
        //Заполнение рабочего и фоного массивов, а также расчет сигм
        alarmClass.fillingArrays(backgroundGaps, workingArray, backgroundArray);

        //Начинаем цикл проездов
        for (int i = 0; i < numberOfMovements; i++) {

            //Начальная точка
            sourceStartPointX = -distance / 2;
            alarm = false;

            //Цикл по милисекундно
            for (int j = 0; j < time; j++) {
                sumCountRate += calcCountRate(detectors, sources, alarmClass, sourceStartPointX, backgroundInMillis);
                sourceStartPointX += distanceInMilliSecond;
                count++;
                if (count == holdingTime) {
                    alarm = alarmClass.calcAlarm(backgroundMode, alarm, sumCountRate);
                    sumCountRate = 0;
                    count = 0;
                    countTime += holdingTime;
                }
                if (interrupt) if (alarm) break;
            }
        }

        //Я хз, на счет тревоги
        ArrayList<Double> arrayList = alarmClass.getAlarmArray();
        for (int i = 0; i < arrayList.size(); i++) {
            countAlarm += arrayList.get(i);
        }
        //Вывод сигм
        txtSigma.setText("Сигма, тревоги (по врем. интервалам)");
        ArrayList<Double> sigma = alarmClass.getArrayListSigma();
        sigma_derivation(sigma, arrayList);

        //Вывод мощности дозы и скорости счета для всех добавленных детекторов
        txtDoseRateAndCountRate.setText("Мощность дозы и скорость счета");
        detectors_derivation();

        //Кол-во тревог
        numberOfAlarms.setText(String.format("%d", countAlarm));
        //Кол-во перемещений
        numberOfMovementsResult.setText(String.format("%.0f", numberOfMovements));
        detectionProbabilityResult.setText(String.format(Locale.ROOT,"%.1f", 0.0));
        //Надо еще подумать (длительность)
        duration.setText(String.format("%d", countTime / 1000));
        //результат в с (планируемая длительность)
        plannedDuration.setText(String.format("%.0f", time / 1000 * numberOfMovements));
        //отличие от изначального фона
        bckg = alarmClass.getCountRateResult();
        txtBackground.setText(String.format(Locale.ROOT,"%.1f", bckg * numberOfGapsInSecond));
    }

    //Метод для вывода сигм
    @SuppressLint("DefaultLocale")
    public void sigma_derivation(ArrayList<Double> sigma, ArrayList<Double> alarm) {
        Log.d(LOG_TAG, "sigma_derivation_fragment_result");
        sigma_array = new String[sigma.size()];
        for (int i = 0; i < sigma.size(); i++) {
            sigma_array[i] = String.format(Locale.ROOT,"σ - %.2f - кол-во тревог: %.0f", sigma.get(i), alarm.get(i));
        }
        adapter_sigma = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, sigma_array);
        sigmaResult.setAdapter(adapter_sigma);
    }

    //Метод для вывода значений детектора
    @SuppressLint("DefaultLocale")
    public void detectors_derivation() {
        Log.d(LOG_TAG, "detectors_derivation_fragment_result");
        double sourceDistance;
        double doseRate = 0;
        double countRate = 0;
        double sensitivity_number;
        int number_source;
        Alarm alarm = new Alarm();
        txtDetectors = new ArrayList<>();
        for (int i = 0; i < detectors.size(); i++) {
            for (int j = 0; j < sources.size(); j++) {

                double sumCountRateTop;
                double sumCountRateBottom;

                double part = detector_geometrical(detectors.get(i));
                double xz = detectors.get(i).getGeometricalSizes() / (part * 2);
                int xzi = 1;

                for (int d = 0; d < Math.ceil(part / 2); d++) {
                    sourceDistance = alarm.calcDistance(sources.get(0).getCoordinateSourceX(), detectors.get(i).getX(),
                            sources.get(0).getCoordinateSourceY(),
                            detectors.get(i).getY(), sources.get(0).getCoordinateSourceZ(), (detectors.get(i).getZ() - xz * xzi));

                    number_source = sources.get(j).getPositionInSpinner();

                    if (sources.get(j).getNameSource().equals("Cf-252") || sources.get(j).getNameSource().equals("PuBe")) {
                        //Считает нормально, умножаем на 10^(10) так как коэф. полностью не выводится и делим на 10^(3)
                        doseRate = alarm.calcDoseRate(sources.get(j).getActivitySource(), sourceDistance, sources.get(j).getCoefficient()) * sourceDistance * Math.pow(10, 4);//79577471545.9477
                    } else {
                        doseRate = alarm.calcDoseRate(sources.get(j).getActivitySource(), sourceDistance, sources.get(j).getCoefficient()) * Math.pow(10, 3);
                    }
                    if(coef != null)
                    doseRate = calc_shield(doseRate, j, sourceDistance);

                    if (sources.get(j).getNameSource().equals("Cf-252") || sources.get(j).getNameSource().equals("PuBe")) {
                        sensitivity_number = detectors.get(i).getSensitivity().get(number_source) / part * Math.pow(10, -1);
                    } else {
                        sensitivity_number = detectors.get(i).getSensitivity().get(number_source) / part;
                    }

                    if (sensitivity_number != 0) {
                        if (Math.ceil(part / 2) == 1) {
                            countRate = alarm.calcCountRate(doseRate, sensitivity_number);
                        } else {
                            sumCountRateTop = alarm.calcCountRate(doseRate, sensitivity_number);
                            sumCountRateBottom = alarm.calcCountRate(doseRate, sensitivity_number);
                            countRate += sumCountRateTop + sumCountRateBottom;
                        }

                    } else {
                        countRate = 0;
                    }
                    xzi += 2;
                }
                txtDetectors.add(String.format(Locale.ROOT,"%s - (%s) - %.4f | %.1f", detectors.get(i).getNameDetector(), sources.get(j).getNameSource(), doseRate * Math.pow(10, 6), countRate));
                countRate = 0;
            }
        }
        adapter_detectors = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, txtDetectors);
        detectorResult.setAdapter(adapter_detectors);
    }

    public double calc_shield(Double doseRate, int i, double sourceDistance) {
        for (int j = 0; j < shields.size(); j++) {
                //Делим на 10 чтобы получить см
                doseRate *= Math.exp(-coef.get(i) * shields.get(j).getThickness() / 10) * compton_correction_coefficient_get(sources.get(j).getNameSource(), sourceDistance);;
        }
        return doseRate;
    }

    public double detector_geometrical(Detector detector) {
        if (detector.getGeometricalSizes() == 0) {
            return 1;
        } else if (detector.getGeometricalSizes() == 0.4) {
            return 8;
        } else {
            return 10;
        }
    }

    public double calcCountRate(ArrayList<Detector> detectors, ArrayList<Source> sources, Alarm alarm, double sourceStartPointX, double backgroundInMillis) {
        PoissonDistribution poissonDistribution;
        int number_source;
        double sourceDistance;
        double sumCountRate = 0;
        double doseRate;
        double sensitivity_number;
        double sumCountRateTop;
        double sumCountRateBottom;
        double countRate;

        for (int i = 0; i < detectors.size(); i++) {
            for (int j = 0; j < sources.size(); j++) {
                double part = detector_geometrical(detectors.get(i));
                double xz = detectors.get(i).getGeometricalSizes() / (part * 2);
                int xzi = 1;

                for (int d = 0; d < Math.ceil(part / 2); d++) {
                    sourceDistance = alarm.calcDistance(sourceStartPointX, detectors.get(i).getX(),
                            sources.get(0).getCoordinateSourceY(),
                            detectors.get(i).getY(), sources.get(0).getCoordinateSourceZ(), (detectors.get(i).getZ() - xz * xzi));
                    number_source = sources.get(j).getPositionInSpinner();
                    if (sources.get(j).getNameSource().equals("Cf-252") || sources.get(j).getNameSource().equals("PuBe")) {
                        doseRate = alarm.calcDoseRate(sources.get(j).getActivitySource(), sourceDistance, sources.get(j).getCoefficient()) * sourceDistance * Math.pow(10, 7);//79577471545.9477
                    } else {
                        doseRate = alarm.calcDoseRate(sources.get(j).getActivitySource(), sourceDistance, sources.get(j).getCoefficient());
                    }
                    if(coef != null)
                    doseRate = calc_shield(doseRate, j, sourceDistance);


                    if (sources.get(j).getNameSource().equals("Cf-252") || sources.get(j).getNameSource().equals("PuBe")) {
                        sensitivity_number = detectors.get(i).getSensitivity().get(number_source) / part * Math.pow(10, -6);
                    } else {
                        sensitivity_number = detectors.get(i).getSensitivity().get(number_source) / part;
                    }

                    if (sensitivity_number != 0) {
                        if (Math.ceil(part / 2) == 1) {
                            //Добавляем фоновое значение за 1мс
                            countRate = alarm.calcCountRate(doseRate, sensitivity_number) + backgroundInMillis;
                            poissonDistribution = new PoissonDistribution(countRate);
                            sumCountRate += poissonDistribution.sample();
                        } else {
                            //Доп расчет по горизонтали и вертикали для чувствительности (грубо говоря область видимость детектора)
                            double v_cos = -sourceStartPointX / Math.sqrt(Math.pow(sourceStartPointX - detectors.get(j).getY(), 2) + Math.pow(sources.get(0).getCoordinateSourceX() - detectors.get(i).getX(), 2));
                            double g_cos = -sourceStartPointX / Math.sqrt(Math.pow(sourceStartPointX - detectors.get(j).getY(), 2) + Math.pow(sources.get(0).getCoordinateSourceZ() - (detectors.get(i).getZ() - xz * xzi), 2));
                            //Добавляем фоновое значение за 1мс
                            countRate = alarm.calcCountRate(doseRate, sensitivity_number) * v_cos * g_cos + backgroundInMillis / 10;
                            poissonDistribution = new PoissonDistribution(countRate);
                            sumCountRateTop = poissonDistribution.sample();
                            sumCountRateBottom = poissonDistribution.sample();
                            sumCountRate += sumCountRateTop + sumCountRateBottom;
                        }
                    } else {
                        continue;
                    }
                    xzi += 2;
                }
            }
        }
        return sumCountRate;
    }

    public double compton_correction_coefficient_get(String name, Double _distance){
        double result = 1;
        if (name.equals("Cs-137"))
            result = 1.0 + 0.009154 * _distance + 0.0000356409 * _distance * _distance;//1.0 + 0.92*µ*d + 0.36*µ*µ*d*d;
        else if (name.equals("Co-60"))
            result = 1.0 + 0.0056518 * _distance + 0.000011852632 * _distance * _distance;//1.0 + 0.77*µ*d + 0.22*µ*µ*d*d;
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        db = databaseHelper.open();
        Log.d(LOG_TAG, "onResume_fragment_result");
        //Значения передаю цепочкой, но пока хз как лучше и как оно работает
        getParentFragmentManager().setFragmentResultListener("request_all", this, (requestKey, result) -> {
            holdingTime = result.getDouble("txtHoldingTime");
            speed = result.getDouble("txtTravelSpeedSource");
            distance = result.getDouble("txtSourcePathLength");
            numberOfMovements = result.getDouble("txtNumberOfMovements");
            numberOfThresholds = result.getDouble("txtNumberOfThresholds");
            workingArray = result.getDouble("txtWorkingArray");
            backgroundArray = result.getDouble("txtBackgroundArray");
            unusedThresholds = result.getDouble("txtUnusedThresholds");
            falseAlarmPeriod = result.getDouble("txtFalseAlarmPeriod");
            detectionProbability = result.getDouble("txtDetectionProbability");
            falseAlarmRate = result.getDouble("txtFalseAlarmRate");
            interrupt = result.getBoolean("txtInterrupt");
            backgroundMode = result.getInt("txtBackgroundMode");
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null){
            numberOfAlarms.setText(String.format("%d", savedInstanceState.getInt("countAlarm")));
            numberOfMovementsResult.setText(String.format(Locale.ROOT,"%.0f", savedInstanceState.getDouble("numberOfMovements")));
            detectionProbabilityResult.setText(String.format(Locale.ROOT,"%.1f", savedInstanceState.getDouble("detectionProbability")));
            duration.setText(String.format("%.0f", savedInstanceState.getDouble("duration")));
            plannedDuration.setText(String.format(Locale.ROOT,"%.0f", savedInstanceState.getDouble("plannedDuration")));
            txtBackground.setText(String.format(Locale.ROOT,"%.1f", savedInstanceState.getDouble("background")));
            adapter_sigma = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, savedInstanceState.getStringArray("sigma"));
            sigmaResult.setAdapter(adapter_sigma);
            adapter_detectors = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, savedInstanceState.getStringArrayList("detectors"));
            detectorResult.setAdapter(adapter_detectors);
            txtSigma.setText("Сигма, тревоги (по врем. интервалам)");
            txtDoseRateAndCountRate.setText("Мощность дозы и скорость счета");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("countAlarm", countAlarm);
        outState.putDouble("numberOfMovements", numberOfMovements);
        outState.putDouble("detectionProbability", 0);
        outState.putDouble("duration", countTime / 1000);
        outState.putDouble("plannedDuration", time / 1000 * numberOfMovements);
        outState.putDouble("background", bckg * numberOfGapsInSecond);
        outState.putStringArray("sigma", sigma_array);
        outState.putStringArrayList("detectors", txtDetectors);
    }
}