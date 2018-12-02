package com.ustc.wsn.mobileData.bean.harTools;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.ArrayList;

/**
 * Created by halo on 2018/12/2.
 */

public class HAR {
    public StringBuffer featureExtract(float[] accNorm) {
        StringBuffer features = new StringBuffer();
        //时域特征
        DescriptiveStatistics stats = new DescriptiveStatistics();
        double[] data = new double[accNorm.length];
        for (int i = 0; i < accNorm.length; i++) {
            stats.addValue(accNorm[i]);
            data[i] = accNorm[i];
        }
        float mean = (float)stats.getMean();
        float std = (float)stats.getStandardDeviation();
        float skew = (float)stats.getSkewness();
        float kurtosis = (float)stats.getKurtosis();
        float maxTopKMean = (float) new myStat().getMaxTopKMean(data, 20);
        float minTopKMean = (float) new myStat().getMinTopKMean(data, 20);

        features.append(mean);
        features.append("\t" + std);
        features.append("\t" + skew);
        features.append("\t" + kurtosis);
        features.append("\t" + maxTopKMean);
        features.append("\t" + minTopKMean);

        // 频谱特征
        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] freSpectrum = fft.transform(data, TransformType.FORWARD);
        ArrayList<Double> Spectrum = new ArrayList<>();
        for (int i = 0; i <= freSpectrum.length / 2; i++) {
            Spectrum.add(freSpectrum[i].abs());
        }
        ArrayList<Double>[] SpectrumPoints = new ArrayList[11];
        float[] SpectrumFeatures = new float[11];
        for (int i = 0; i < 11; i++)
            SpectrumPoints[i] = new ArrayList<>();
        final double SampleRate = 50.0;
        final int SpectrumLength = Spectrum.size() - 1;

        final double density = (SampleRate * 0.5) / SpectrumLength;

        for (int i = 1; i <= SpectrumLength; i++) {
            double frequency = density * i;
            int k = (int) Math.floor(frequency);
            if (k < 10) SpectrumPoints[k].add(Spectrum.get(i));
            else SpectrumPoints[10].add(Spectrum.get(i));
        }

        for (int k = 0; k < SpectrumPoints.length; k++) {
            SpectrumFeatures[k] = new myStat().getMean(SpectrumPoints[k]);
            features.append("\t" + SpectrumFeatures[k]);
        }
        return features;
    }
}

class myStat {
    int partionMin(double a[], int first, int end) {
        int i = first;
        double main = a[end];
        for (int j = first; j < end; j++) {
            if (a[j] < main) {
                double temp = a[j];
                a[j] = a[i];
                a[i] = temp;
                i++;
            }
        }
        a[end] = a[i];
        a[i] = main;
        return i;
    }

    int partionMax(double a[], int first, int end) {
        int i = first;
        double main = a[end];
        for (int j = first; j < end; j++) {
            if (a[j] > main) {
                double temp = a[j];
                a[j] = a[i];
                a[i] = temp;
                i++;
            }
        }
        a[end] = a[i];
        a[i] = main;
        return i;
    }

    void getTopKMinBySort(double a[], int first, int end, int k) {
        if (first < end) {
            int partionIndex = partionMin(a, first, end);
            if (partionIndex == k - 1) return;
            else if (partionIndex > k - 1) getTopKMinBySort(a, first, partionIndex - 1, k);
            else getTopKMinBySort(a, partionIndex + 1, end, k);
        }
    }

    void getTopKMaxBySort(double a[], int first, int end, int k) {
        if (first < end) {
            int partionIndex = partionMax(a, first, end);
            if (partionIndex == k - 1) return;
            else if (partionIndex > k - 1) getTopKMaxBySort(a, first, partionIndex - 1, k);
            else getTopKMaxBySort(a, partionIndex + 1, end, k);
        }
    }

    double getMinTopKMean(double[] a, int k) {
        double mean = 0;
        getTopKMinBySort(a, 0, a.length - 1, k);
        for (int i = 0; i < k; i++) {
            mean += a[i] / k;
        }
        return mean;
    }

    double getMaxTopKMean(double[] a, int k) {
        double mean = 0;
        getTopKMaxBySort(a, 0, a.length - 1, k);
        for (int i = 0; i < k; i++) {
            mean += a[i] / k;
        }
        return mean;
    }

    float getMean(ArrayList<Double> a) {
        float mean = 0;
        for (int i = 0; i < a.size(); i++) {
            mean += a.get(i) / a.size();
        }
        return mean;
    }
}