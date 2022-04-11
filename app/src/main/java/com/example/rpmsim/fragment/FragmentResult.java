package com.example.rpmsim.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
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

                //Записываем чувствительность
                double sensitivity_write;
                //Записываем геометрию
                double geometrical_write;

                //Считаем sumCountRate для детекторов
                for (int k = 0; k < detectors.size(); k++) {
                    sensitivity_write = detectors.get(k).getSensitivity().get(0);
                    geometrical_write = detectors.get(k).getGeometricalSizes();

                    //Точечный детектор
                    if (geometrical_write == 0 && sensitivity_write != 0) {
                        for (int s = 0; s < sources.size(); s++) {
                            sourceDistance = alarmClass.calcDistance(sourceStartPointX, detectors.get(k).getX(),
                                    sources.get(s).getCoordinateSourceY(),
                                    detectors.get(k).getY(), sources.get(s).getCoordinateSourceZ(), detectors.get(k).getZ());
                            sourceStartPointX += distanceInMilliSecond;
                            doseRate = alarmClass.calcDoseRate(sources.get(s).getActivitySource(), sourceDistance,
                                    sources.get(s).getCoefficient());
                            countRate = alarmClass.calcCountRate(doseRate, detectors.get(k).getSensitivity().get(sources.get(s).getPositionInSpinner()));

                            PoissonDistribution poissonDistribution = new PoissonDistribution(countRate);
                            sumCountRate += poissonDistribution.sample();
                        }
                        //Изотропный детектор
                    } else if (geometrical_write == 0.4 && sensitivity_write != 0) {
                        //Т.к. детектор изотропный и геометрия для него 0,4, то разбиваем этот детектор на 8 частей
                        //Делим на 16 чтобы получить расстояние до центра
                        double xz = detectors.get(k).getGeometricalSizes() / 16;

                        //Чтобы не проходить цикл 8 раз будем сразу записывать значения снизу и сверху
                        double sumCountRateTop = 0;
                        double sumCountRateBottom = 0;

                        //xz * xzi чтобы получить центр каждой части
                        int xzi = 1;

                        //Т.к. записываем значения сверху и снизу, то цикл до 4
                        for (int f = 0; f < 4; f++) {
                            for (int s = 0; s < sources.size(); s++) {
                                sourceDistance = alarmClass.calcDistance(sourceStartPointX, detectors.get(k).getX(),
                                        sources.get(s).getCoordinateSourceY(),
                                        detectors.get(k).getY(), sources.get(s).getCoordinateSourceZ(), (detectors.get(k).getZ() - xz * xzi));

                                sourceStartPointX += distanceInMilliSecond;
                                doseRate = alarmClass.calcDoseRate(sources.get(s).getActivitySource(), sourceDistance,
                                        sources.get(s).getCoefficient());

                                //Т.к. мы детектор разбиваем на 8 частей то и чувствительнось будем в 8 раз меньше для каждой части
                                countRate = alarmClass.calcCountRate(doseRate, detectors.get(k).getSensitivity().get(sources.get(s).getPositionInSpinner())) / 8;

                                PoissonDistribution poissonDistribution = new PoissonDistribution(countRate);
                                sumCountRateTop += poissonDistribution.sample();
                                sumCountRateBottom += poissonDistribution.sample();
                                sumCountRate += sumCountRateTop + sumCountRateBottom;
                            }
                            xzi += 2;
                        }

                        //Изотропный детектор
                    } else if (geometrical_write == 1 && sensitivity_write != 0) {
                        //Т.к. детектор изотропный и геометрия для него 1, то разбиваем этот детектор на 10 частей
                        //Делим на 20 чтобы получить расстояние до центра
                        double xz = detectors.get(k).getGeometricalSizes() / 20;

                        //Чтобы не проходить цикл 10 раз будем сразу записывать значения снизу и сверху
                        double sumCountRateTop = 0;
                        double sumCountRateBottom = 0;

                        //Так как делим на 10 частей, то начинаем цикл с 0 до 5
                        int xzi = 1;

                        //Т.к. записываем значения сверху и снизу, то цикл до 5
                        for (int f = 0; f < 5; f++) {
                            for (int s = 0; s < sources.size(); s++) {
                                sourceDistance = alarmClass.calcDistance(sourceStartPointX, detectors.get(k).getX(),
                                        sources.get(s).getCoordinateSourceY(),
                                        detectors.get(k).getY(), sources.get(s).getCoordinateSourceZ(), (detectors.get(k).getZ() - xz * xzi));

                                sourceStartPointX += distanceInMilliSecond;
                                doseRate = alarmClass.calcDoseRate(sources.get(s).getActivitySource(), sourceDistance,
                                        sources.get(s).getCoefficient());

                                //Т.к. мы детектор разбиваем на 10 частей то и чувствительнось будем в 10 раз меньше для каждой части
                                countRate = alarmClass.calcCountRate(doseRate, detectors.get(k).getSensitivity().get(sources.get(s).getPositionInSpinner())) / 10;

                                PoissonDistribution poissonDistribution = new PoissonDistribution(countRate);
                                sumCountRateTop += poissonDistribution.sample();
                                sumCountRateBottom += poissonDistribution.sample();
                                sumCountRate += sumCountRateTop + sumCountRateBottom;
                            }
                            xzi += 2;
                        }
                    }
                }

                count++;
                if (count == holdingTime) {
                    alarm = alarmClass.calcAlarm(backgroundMode, alarm, sumCountRate);
                    sumCountRate = 0;
                    count = 0;
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
        duration.setText(String.format("%d", countTime / 10));
        //результат в с
        plannedDuration.setText(String.format("%.0f", time / 1000 * numberOfMovements));

        progressBar.setVisibility(View.INVISIBLE);
    }

    //Метод для вывода сигм
    @SuppressLint("DefaultLocale")
    public void sigma_derivation(ArrayList<Double> sigma, ArrayList<Double> alarm) {
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
        double sourceDistance;
        double doseRate;
        double countRate;
        double sensitivity_number;
        int number_source;
        Alarm alarm = new Alarm();
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = 0; i < detectors.size(); i++) {
            sourceDistance = alarm.calcDistance(sources.get(0).getCoordinateSourceX(), detectors.get(i).getX(),
                    sources.get(0).getCoordinateSourceY(),
                    detectors.get(i).getY(), sources.get(0).getCoordinateSourceZ(), detectors.get(i).getZ());
            for (int j = 0; j < sources.size(); j++) {
                //Т.к. источник находится на постоянной точке, то лучше его перенести на 1 строку вверх
                //Пока для г и нейтрон/с расчет не верен

                number_source = sources.get(j).getPositionInSpinner();
                doseRate = alarm.calcDoseRate(sources.get(j).getActivitySource(), sourceDistance, sources.get(j).getCoefficient());
                sensitivity_number = detectors.get(i).getSensitivity().get(number_source);
                if (sensitivity_number != 0) {
                    countRate = alarm.calcCountRate(doseRate, sensitivity_number);
                } else {
                    countRate = 0;

                }
                arrayList.add(String.format("%s - (%s) - %.4f | %.1f", detectors.get(i).getNameDetector(), sources.get(j).getNameSource(), doseRate * Math.pow(10, 6), countRate));
            }
        }
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, arrayList);
        detectorResult.setAdapter(adapter1);
    }

    @Override
    public void onResume() {
        super.onResume();
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

//Если надо будет вернуть первоночальное решение
//            if (detector.getGeometricalSizes() == 0) {
//                for (int i = 0; i < time; i++) {
//
//                    //Может как то красивше сделать
//                    if (interrupt)
//                        if (alarm) {
//                            countAlarm++;
//                            break;
//                        }
//
//                    sourceDistance = alarmClass.calcDistance(sourceStartPointX, detector.getX(),
//                            source.getCoordinateSourceY(),
//                            detector.getY(), source.getCoordinateSourceZ(), detector.getZ());
//                    sourceStartPointX += distanceInMilliSecond;
//                    doseRate = alarmClass.calcDoseRate(source.getActivitySource(), sourceDistance,
//                            source.getCoefficient());
//                    countRate = alarmClass.calcCountRate(doseRate, detector.getSensitivity().get(0));
//
//                    PoissonDistribution poissonDistribution = new PoissonDistribution(countRate);
//                    sumCountRate += poissonDistribution.sample();
//
//                    count++;
//                    if (count == holdingTime) {
//                        alarm = alarmClass.calcAlarm(backgroundMode, alarm, sumCountRate);
//                        countTime++;
//                        sumCountRate = 0;
//                        count = 0;
//                    }
//                }
//            } else if (detector.getGeometricalSizes() == 0.4) {
//                //Вроде бы разбил на 8 частей
//                double xz = detector.getGeometricalSizes() / 16;
//                double sumCountRateTop = 0;
//                double sumCountRateBottom = 0;
//                int xzi = 1;
//                for (int j = 0; j < time; j++) {
//                    for (int i = 1; i <= 4; i++) {
//                        sourceDistance = alarmClass.calcDistance(sourceStartPointX, detector.getX(),
//                                source.getCoordinateSourceY(),
//                                detector.getY(), source.getCoordinateSourceZ(), (detector.getZ() - xz * xzi));
//
//                        sourceStartPointX += distanceInMilliSecond;
//                        doseRate = alarmClass.calcDoseRate(source.getActivitySource(), sourceDistance,
//                                source.getCoefficient());
//                        //Делим на 8 т.е. получаем 1 часть
//                        countRate = alarmClass.calcCountRate(doseRate, detector.getSensitivity().get(0)) / 8;
//
//                        PoissonDistribution poissonDistribution = new PoissonDistribution(countRate);
//                        sumCountRateTop += poissonDistribution.sample();
//                        sumCountRateBottom += poissonDistribution.sample();
//                        sumCountRate += sumCountRateTop + sumCountRateBottom;
//
//                        xzi += 2;
//                    }
//
//                    count++;
//                    if (count == holdingTime) {
//                        alarm = alarmClass.calcAlarm(backgroundMode, alarm, sumCountRate);
//                        countTime++;
//                        sumCountRate = 0;
//                        count = 0;
//                    }
//                    if (interrupt)
//                        if (alarm) {
//                            countAlarm++;
//                            break;
//                        }
//                }
//
//            } else if (detector.getGeometricalSizes() == 1) {
//                //Вроде бы разбил на 10 частей
//                double xz = detector.getGeometricalSizes() / 20;
//                double sumCountRateTop = 0;
//                double sumCountRateBottom = 0;
//                //Так как делим на 10 частей, то начинаем цикл с 0 до 5
//                int xzi = 1;
//                for (int j = 0; j < time; j++) {
//                    for (int i = 0; i < 5; i++) {
//                        sourceDistance = alarmClass.calcDistance(sourceStartPointX, detector.getX(),
//                                source.getCoordinateSourceY(),
//                                detector.getY(), source.getCoordinateSourceZ(), (detector.getZ() - xz * xzi));
//
//                        sourceStartPointX += distanceInMilliSecond;
//                        doseRate = alarmClass.calcDoseRate(source.getActivitySource(), sourceDistance,
//                                source.getCoefficient());
//                        //Делим на 10 т.е. получаем 1 часть
//                        countRate = alarmClass.calcCountRate(doseRate, detector.getSensitivity().get(0)) / 10;
//
//                        PoissonDistribution poissonDistribution = new PoissonDistribution(countRate);
//                        sumCountRateTop += poissonDistribution.sample();
//                        sumCountRateBottom += poissonDistribution.sample();
//                        sumCountRate += sumCountRateTop + sumCountRateBottom;
//
//                        xzi += 2;
//                    }
//                    count++;
//                    if (count == holdingTime) {
//                        alarm = alarmClass.calcAlarm(backgroundMode, alarm, sumCountRate);
//                        countTime++;
//                        sumCountRate = 0;
//                        count = 0;
//                    }
//
//                    if (interrupt)
//                        if (alarm) {
//                            countAlarm++;
//                            break;
//                        }
//                }
//
//            }
