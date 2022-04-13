package com.example.rpmsim.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import com.example.rpmsim.R;
import com.example.rpmsim.calc_alarm.Alarm;
import com.example.rpmsim.entity.Detector;
import com.example.rpmsim.entity.Source;

import org.apache.commons.math3.distribution.PoissonDistribution;

import java.util.ArrayList;

public class FragmentResult extends Fragment implements View.OnClickListener {

    final String LOG_TAG = "myLogs";

    private TextView numberOfAlarms, numberOfMovementsResult, detectionProbabilityResult, duration, plannedDuration, textView20, textView21;
    private ListView sigmaResult;
    private ListView detectorResult;

    private ArrayList<Detector> detectors = new ArrayList<>();
    private ArrayList<Source> sources = new ArrayList<>();
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

    ProgressBar progressBar;

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
        detectionProbabilityResult.setText("0");
        duration = result.findViewById(R.id.duration);
        duration.setText("0");
        plannedDuration = result.findViewById(R.id.plannedDuration);
        plannedDuration.setText("0");
        sigmaResult = result.findViewById(R.id.sigmaResult);
        detectorResult = result.findViewById(R.id.detectorResult);
        textView20 = result.findViewById(R.id.textView20);
        textView21 = result.findViewById(R.id.textView21);

        Button detectionResult = result.findViewById(R.id.detectionResult);

        progressBar = result.findViewById(R.id.progressBar);
        detectionResult.setOnClickListener(this);

        return result;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onClick(View v) {

        Log.d(LOG_TAG, "onClick_fragment_result");
        //Пока хз, чего он не работает
        progressBar.setVisibility(View.VISIBLE);

        // 10000мc
        double time = distance / speed * 1000;
        // 0.001м в 1мс
        double distanceInMilliSecond = speed / 1000;
        //Начало по x
        double sourceStartPointX;
        // изменение расстояния каждый 0,001м
        double sourceDistance;
        // мощность дозы
        double doseRate;
        // скорость счета
        double countRate;
        // скорость счета за 100мс
        double sumCountRate = 0;
        // просто счетчик для 100мс
        int count = 0;
        //Определение тревоги
        boolean alarm;
        //Сколько раз детектор собирает информацию в 1с
        int numberOfGapsInSecond = (int) (1000 / holdingTime);
        //Счетчик тревог
        int countAlarm = 0;
        //Типо время в мс, но хз
        int countTime = 0;
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
        //Заполнение рабочего и фоного массивов, а также расчет сигм
        alarmClass.fillingArrays(backgroundGaps, workingArray, backgroundArray);

        //Начинаем цикл проездов
        for (int i = 0; i < numberOfMovements; i++) {

            //Начальная точка
            sourceStartPointX = -distance / 2;
            alarm = false;

            //Цикл по милисекундно
            for (int j = 0; j < time; j++) {
                sourceStartPointX += distanceInMilliSecond;
                sumCountRate += calcCountRate(detectors, sources, alarmClass, sourceStartPointX);
                count++;
                if (count == holdingTime) {
                    alarm = alarmClass.calcAlarm(backgroundMode, alarm, sumCountRate);
                    sumCountRate = 0;
                    count = 0;
                    countTime++;
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
        textView20.setText("Сигма, тревоги (по врем. интервалам)");
        ArrayList<Double> sigma = alarmClass.getArrayListSigma();
        sigma_derivation(sigma, arrayList);

        //Вывод мощности дозы и скорости счета для всех добавленных детекторов
        textView21.setText("Мощность дозы и скорость счета");
        detectors_derivation();

        numberOfAlarms.setText(String.format("%d", countAlarm));
        numberOfMovementsResult.setText(String.format("%.0f", numberOfMovements));
        //Пока хз
        detectionProbabilityResult.setText("");
        //Надо еще подумать
        duration.setText(String.format("%d", countTime));
        //результат в с
        plannedDuration.setText(String.format("%.0f", time / 1000 * numberOfMovements));

        progressBar.setVisibility(View.INVISIBLE);
    }

    //Метод для вывода сигм
    @SuppressLint("DefaultLocale")
    public void sigma_derivation(ArrayList<Double> sigma, ArrayList<Double> alarm) {
        Log.d(LOG_TAG, "sigma_derivation_fragment_result");
        String[] sigma_array = new String[sigma.size()];
        for (int i = 0; i < sigma.size(); i++) {
            sigma_array[i] = String.format("σ - %.2f - кол-во тревог: %.0f", sigma.get(i), alarm.get(i));
        }
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, sigma_array);
        sigmaResult.setAdapter(adapter2);
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
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = 0; i < detectors.size(); i++) {
            for (int j = 0; j < sources.size(); j++) {

                double sumCountRateTop = 0;
                double sumCountRateBottom = 0;

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
                        doseRate = alarm.calcDoseRate(sources.get(j).getActivitySource(), sourceDistance, sources.get(j).getCoefficient()) * sourceDistance * Math.pow(10, 1);//79577471545.9477
                    } else {
                        doseRate = alarm.calcDoseRate(sources.get(j).getActivitySource(), sourceDistance, sources.get(j).getCoefficient());
                    }

                    if (sources.get(j).getNameSource().equals("Cf-252") || sources.get(j).getNameSource().equals("PuBe")){
                        sensitivity_number = detectors.get(i).getSensitivity().get(number_source) / part * Math.pow(10, -1);
                    }else {
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
                arrayList.add(String.format("%s - (%s) - %.4f | %.1f", detectors.get(i).getNameDetector(), sources.get(j).getNameSource(), doseRate * Math.pow(10, 6), countRate));
                countRate = 0;
            }
        }
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, arrayList);
        detectorResult.setAdapter(adapter1);
    }

    public double detector_geometrical(Detector detector) {
        Log.d(LOG_TAG, "detector_geometrical_fragment_result");
        if (detector.getGeometricalSizes() == 0) {
            return 1;
        } else if (detector.getGeometricalSizes() == 0.4) {
            return 8;
        } else {
            return 10;
        }
    }

    public double calcCountRate(ArrayList<Detector> detectors, ArrayList<Source> sources, Alarm alarm, double sourceStartPointX){
        PoissonDistribution poissonDistribution;
        int number_source;
        double sourceDistance;
        double sumCountRate = 0;
        double doseRate;
        double sensitivity_number;
        double sumCountRateTop;
        double sumCountRateBottom;
        double countRate = 0;

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

                    if (sources.get(j).getNameSource().equals("Cf-252") || sources.get(j).getNameSource().equals("PuBe")){
                        sensitivity_number = detectors.get(i).getSensitivity().get(number_source) / part * Math.pow(10,-6);
                    }else {
                        sensitivity_number = detectors.get(i).getSensitivity().get(number_source) / part;
                    }

                    if (sensitivity_number != 0) {
                        if (Math.ceil(part / 2) == 1) {
                            countRate = alarm.calcCountRate(doseRate, sensitivity_number);
                            poissonDistribution = new PoissonDistribution(countRate);
                            sumCountRate += poissonDistribution.sample();
                        } else {
                            countRate = alarm.calcCountRate(doseRate, sensitivity_number);
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

    @Override
    public void onResume() {
        super.onResume();
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
            sources = (ArrayList<Source>) result.getSerializable("sources");
            detectors = (ArrayList<Detector>) result.getSerializable("detectors");
        });
    }
}