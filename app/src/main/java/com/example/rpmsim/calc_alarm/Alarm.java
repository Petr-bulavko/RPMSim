package com.example.rpmsim.calc_alarm;

import com.example.rpmsim.entity.CountRate;

import org.apache.commons.math3.distribution.PoissonDistribution;

import java.util.ArrayList;

public class Alarm {

    //Рабочий массив
    private final ArrayList<CountRate> workArray = new ArrayList<>();
    //Фоновый массив
    private final ArrayList<Double> background = new ArrayList<>();
    //Массив сигм
    private final ArrayList<Double> arrayListSigma = new ArrayList<>();
    //Массив тревог
    private final ArrayList<Double> alarmArray = new ArrayList<>();
    //Размер фонового массива
    private double backgroundArraySize;
    //Кол-во порогов
    private double numberOfThreshold;
    //Период ложных тревог
    private double falseAlarmPeriod;
    // 1 - адаптивный, 2 - константный, 3 - вручную
    private double backgroundMode;
    //Сумма фонового массива
    private double countRateN = 0;
    //Среднее значение фонового массива
    private double countRateResult = 0;
    //Сигнал тревоги
    private boolean alarm_bool = false;
    //Сумма порогов рабочего массива
    private double sum_work_array = 0;

    public Alarm(double backgroundArraySize, double numberOfThreshold, double falseAlarmPeriod, double backgroundMode) {
        this.backgroundArraySize = backgroundArraySize;
        this.numberOfThreshold = numberOfThreshold;
        this.falseAlarmPeriod = falseAlarmPeriod;
        this.backgroundMode = backgroundMode;
    }

    public Alarm() {
    }

    public ArrayList<Double> getAlarmArray() {
        return alarmArray;
    }

    public ArrayList<Double> getArrayListSigma() {
        return arrayListSigma;
    }

    //Метод для заполнения рабочего и фонового массива
    public void fillingArrays(double detectorBackground, double workingArraySize, double backgroundArraySize) {
        for (int i = 0; i < workingArraySize; i++) {
            workArray.add(new CountRate(detectorBackground, false));
        }
        for (int i = 0; i < numberOfThreshold; i++) {
            sum_work_array += workArray.get(i).getCountRate();
        }
        for (int i = 0; i < backgroundArraySize; i++) {
            background.add(detectorBackground);
            countRateN += background.get(i);
        }
        countRateResult = countRateN / backgroundArraySize;
        arraySigma(countRateResult);
    }

    //Заполнение массива сигм
    public void arraySigma(double countRateResult) {

        double false_prob = false_prob();
        if (backgroundMode == 0)
            false_prob *= RSTable_cf_adpt_get((int) numberOfThreshold, falseAlarmPeriod);
        if (backgroundMode == 2)
            false_prob *= 0.9;

        double sigma_threshold;

        for (int i = 1; i <= numberOfThreshold; i++) {
            sigma_threshold = calcSigma((1 - false_prob), countRateResult * i);
            arrayListSigma.add(sigma_threshold);
            alarmArray.add(0.0);
        }
    }

    //Расчет сигмы
    public double calcSigma(double probability, double countRateResult) {
        int minN = (int) (countRateResult - 10 * Math.sqrt(countRateResult));
        if (minN < 1) minN = 1;
        int maxN = (int) (countRateResult + 10 * Math.sqrt(countRateResult));

        PoissonDistribution p = new PoissonDistribution(countRateResult);
        double sum_prob = 0;
        double sigma;
        for (; minN < maxN; minN++) {
            sum_prob += p.probability(minN);
            if (sum_prob > probability) {
                break;
            }
        }
        sigma = (minN - countRateResult) / Math.sqrt(countRateResult);
        return sigma;
    }

    //Расчет вектора (источник -> детектор)
    public double calcDistance(double sourceStartPointX, double detectorX, double sourceY,
                               double detectorY, double sourceZ, double detectorZ) {
        return Math.sqrt(Math.pow((sourceStartPointX - detectorX), 2)
                + Math.pow(sourceY - detectorY, 2) + Math.pow(sourceZ - detectorZ, 2));
    }

    //Расчет мощности дозы
    public double calcDoseRate(double sourceActivity, double distance, double sourceCoefficient) {
        // Так как прошу вводить в кБк то умножим на 10^3
        return sourceActivity / Math.pow(distance, 2) * sourceCoefficient * Math.pow(10, -12) * Math.pow(10, 3);
    }

    //Расчет скорости счета
    public double calcCountRate(double doseRate, double detectorSensitivity) {
        return doseRate * detectorSensitivity / Math.pow(10, -6);
    }

    //Расчет тревоги (возвращает true если тревога есть)
    public boolean calcAlarm(int backgroundMode, boolean alarm, double sumCountRate) {
        switch (backgroundMode) {
            case 0:
                alarm = alarmAdaptiveMode(new CountRate(sumCountRate, true));
                break;
            case 1:
            case 2:
                alarm = alarmHandModeAndConstantMode(new CountRate(sumCountRate, true));
                break;
            default:
                break;
        }
        return alarm;
    }

    //Расчет тревоги в адаптивном режиме
    public boolean alarmAdaptiveMode(CountRate countRate) {
        //Тут удаляем элемент и проверяем у элемента false или true
        //Надо перепроверить

        workArray.remove(0);
        workArray.add(countRate);
        sum_work_array = sum_work_array + workArray.get(workArray.size() - 1).getCountRate() - workArray.get((int) (workArray.size() - numberOfThreshold - 1)).getCountRate();

        //Порог
        double threshold;
        double getCountRate = sum_work_array;
        //Максимальный фон
        double bckg = countRateResult * numberOfThreshold;
        for (int i = arrayListSigma.size() - 1; i >= 0; i--) {
            threshold = bckg + arrayListSigma.get(i) * Math.sqrt(bckg);

            if (getCountRate > threshold) {
                countAlarm(i);
                alarm_bool = true;
                //Если тревога, то всем эелементам до этого ставим false (не может перейти в фон)
                for (int j = 1; j <= i; j++) {
                    workArray.get(workArray.size() - j).setBckg(false);
                }
                break;
            }
            if (i == 0) break;
            getCountRate -= workArray.get(workArray.size() - i).getCountRate();
            bckg -= countRateResult;

        }

        if (workArray.get(workArray.size() - 1).isBckg()) {
            background.add(workArray.get(workArray.size() - 1).getCountRate());
            countRateN = countRateN - background.get(0) + background.get(background.size() - 1);
            countRateResult = countRateN / backgroundArraySize;
            background.remove(0);
        }

        return alarm_bool;
    }

    //Расчет тревоги в константном и в ручном режиме
    public boolean alarmHandModeAndConstantMode(CountRate countRate) {
        workArray.remove(0);
        workArray.add(countRate);
        sum_work_array = sum_work_array + workArray.get(workArray.size() - 1).getCountRate() - workArray.get((int) (workArray.size() - numberOfThreshold - 1)).getCountRate();

        double threshold;
        double getCountRate = sum_work_array;
        double bckg = countRateResult * numberOfThreshold;

        for (int i = arrayListSigma.size() - 1; i >= 0; i--) {
            threshold = bckg + arrayListSigma.get(i) * Math.sqrt(bckg);

            if (getCountRate > threshold) {
                countAlarm(i);
                alarm_bool = true;
                break;
            }
            if (i == 0) break;
            getCountRate -= workArray.get(workArray.size() - i).getCountRate();
            bckg -= countRateResult;
        }

        return alarm_bool;
    }

    //Записываем тревогу в массив
    public void countAlarm(int index) {
        double i = alarmArray.get(index) + 1;
        alarmArray.set(index, i);
    }

    public double false_prob() {
        return falseAlarmPeriod * RSTable_cf_get((int) numberOfThreshold, falseAlarmPeriod);
    }

    double[] RSTable_probability = new double[]{1.0E-1, 1.0E-2, 1.0E-3, 1.0E-4, 1.0E-5, 1.0E-6};
    double[][] RSTable_probability_cf = {
            {1.0000000000, 0.8182015129, 0.7549599374, 0.7246142777, 0.7052370319, 0.6867390464,
                    0.6767244593, 0.6667098721, 0.6566952849, 0.6466806977, 0.6404952664, 0.6343098350,
                    0.6281244037, 0.6219389723, 0.6157535410, 0.6118356763, 0.6079178117, 0.6039999471,
                    0.6000820824, 0.5961642178},

            {1.0000000000, 0.7055209829, 0.6058924289, 0.5509657321, 0.5303148426, 0.5178006792,
                    0.5096536644, 0.5015066496, 0.4933596348, 0.4852126200, 0.4823046500, 0.4793966800,
                    0.4764887099, 0.4735807399, 0.4706727699, 0.4691108408, 0.4675489118, 0.4659869828,
                    0.4644250538, 0.4628631248},

            {1.0000000000, 0.6422818792, 0.5164597949, 0.4334239130, 0.4162679426, 0.3999164229, 0.3909415013,
                    0.3819665797, 0.3729916580, 0.3640167364, 0.3610164526, 0.3580161688, 0.3550158849,
                    0.3520156011, 0.3490153173, 0.3462525340, 0.3434897508, 0.3407269675,
                    0.3379641843, 0.3352014011},

            {1.0000000000, 0.5798107256, 0.4583541147, 0.3684843625, 0.3411284336, 0.3209919665, 0.3087374640,
                    0.2964829616, 0.2842284592, 0.2719739568, 0.2681292204, 0.2642844841, 0.2604397477,
                    0.2565950114, 0.2527502750, 0.2491838601, 0.2456174452, 0.2420510303, 0.2384846153,
                    0.2349182004},

            {1.0000000000, 0.4973544974, 0.3966244726, 0.3393501805, 0.3186440678, 0.2589531680, 0.2467875830,
                    0.2346219979, 0.2224564128, 0.2102908277, 0.2054603850, 0.2006299422, 0.1957994994, 0.1909690566,
                    0.1861386139, 0.1845845154, 0.1830304169, 0.1814763184, 0.1799222199, 0.1783681214},

            {1.0000000000, 0.5208333333, 0.3968253968, 0.2923976608, 0.2717391304, 0.2525252525, 0.2334080239,
                    0.2142907953, 0.1951735667, 0.1760563380, 0.1736319557, 0.1712075733, 0.1687831909, 0.1663588086,
                    0.1639344262, 0.1603020599, 0.1566696936, 0.1530373273, 0.1494049610, 0.1457725948}
    };
    double[][] RSTable_probability_cf_adpt = {
            {1.0000000000, 1.0000000000, 1.0000000000, 1.0000000000, 1.0000000000, 1.0000000000, 1.0000000000,
                    1.0000000000, 1.0000000000, 1.0000000000, 1.0000000000, 1.0000000000, 1.0000000000,
                    1.0000000000, 1.0000000000, 1.0000000000, 1.0000000000, 1.0000000000, 1.0000000000,
                    1.0000000000},

            {1.0000000000, 0.8300000000, 0.8300000000, 0.8300000000, 0.8300000000, 0.8300000000, 0.8300000000,
                    0.8300000000, 0.8300000000, 0.8300000000, 0.8300000000, 0.8300000000, 0.8300000000, 0.8300000000,
                    0.8300000000, 0.8300000000, 0.8300000000, 0.8300000000, 0.8300000000, 0.8300000000},

            {1.0000000000, 0.9110613900, 0.8973829201, 0.9186407219, 0.8573496513, 0.8114335534, 0.8109546382,
                    0.8104757230, 0.8099968078, 0.8095178926, 0.7988621377, 0.7882063827, 0.7775506277, 0.7668948728,
                    0.7562391178, 0.7531773513, 0.7501155849, 0.7470538184, 0.7439920519, 0.7409302855},

            {1.0000000000, 0.7986394558, 0.7310087173, 0.7106537530, 0.6881594373, 0.6231422505, 0.6059308144,
                    0.5887193783, 0.5715079422, 0.5542965061, 0.5425089349, 0.5307213636, 0.5189337923,
                    0.5071462211, 0.4953586498, 0.4857686271, 0.4761786045, 0.4665885819, 0.4569985592, 0.4474085366},

            {1.0000000000, 0.9156626506, 0.8000000000, 0.7037037037, 0.6495726496, 0.5801526718, 0.5634928822,
                    0.5468330926, 0.5301733031, 0.5135135135, 0.5002225755, 0.4869316375, 0.4736406995, 0.4603497615,
                    0.4470588235, 0.4249036960, 0.4027485685, 0.3805934409, 0.3584383134, 0.3362831858},

            {1.0000000000, 0.7699115044, 0.6350364964, 0.5800000000, 0.5087719298, 0.4887640449, 0.4539224313,
                    0.4190808177, 0.3842392040, 0.3493975904, 0.3243634331, 0.2993292759, 0.2742951186, 0.2492609614,
                    0.2242268041, 0.2129721769, 0.2017175497, 0.1904629224, 0.1792082952, 0.1679536680}

    };

    public double RSTable_cf_get(int _interval_count, double _probability) {
        double result;
        int ind = (_interval_count) - 1;
        if (ind < 0) ind = 0;
        else if (ind > 19) ind = 19;
        double[] pCf = RSTable_probability_cf[ind];

        int ind1 = 5;
        int ind2 = 5;
        for (int i = 0; i < 6; i++) {
            if (_probability >= RSTable_probability[i]) {
                ind2 = i;
                ind1 = (ind2 > 0) ? (ind2 - 1) : 0;
                break;
            }
        }
        if (ind1 == ind2)
            result = pCf[ind1];
        else
            result = pCf[ind1] + (pCf[ind2] - pCf[ind1]) * (_probability - RSTable_probability[ind1]) / (RSTable_probability[ind2] - RSTable_probability[ind1]);

        return result;
    }

    public double RSTable_cf_adpt_get(int _interval_count, double _probability) {
        double result;
        int ind = (_interval_count) - 1;
        if (ind < 0) ind = 0;
        else if (ind > 19) ind = 19;
        double[] pCf = RSTable_probability_cf_adpt[ind];
        int ind1 = 5;
        int ind2 = 5;
        for (int i = 0; i < 6; i++) {
            if (_probability >= RSTable_probability[i]) {
                ind2 = i;
                ind1 = (ind2 > 0) ? (ind2 - 1) : 0;
                break;
            }
        }
        if (ind1 == ind2)
            result = pCf[ind1];
        else
            result = pCf[ind1] + (pCf[ind2] - pCf[ind1]) * (_probability - RSTable_probability[ind1]) / (RSTable_probability[ind2] - RSTable_probability[ind1]);

        return result;
    }
}
