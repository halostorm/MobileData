package com.ustc.wsn.mobileData.bean.harTools;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import java.io.*;
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
        Complex[] data1 = new Complex[accNorm.length];
        for (int i = 0; i < accNorm.length; i++) {
            stats.addValue(accNorm[i]);
            data[i] = accNorm[i];
            data1[i] = new Complex(accNorm[i]);
        }
        double mean = (double)stats.getMean();
        double std = (double)stats.getStandardDeviation();
        double skew = (double)stats.getSkewness();
        double kurtosis = (double)stats.getKurtosis();
        double maxTopKMean = (double) new myStat().getMaxTopKMean(data, 20);
        double minTopKMean = (double) new myStat().getMinTopKMean(data, 20);

        features.append(mean);
        features.append("\t" + std);
        features.append("\t" + skew);
        features.append("\t" + kurtosis);
        features.append("\t" + minTopKMean);
        features.append("\t" + maxTopKMean);

        // 频谱特征
        Complex[] freSpectrum = new FFT().fft(data1);
        ArrayList<Double> Spectrum = new ArrayList<>();
        for (int i = 0; i <= freSpectrum.length / 2; i++) {
            Spectrum.add(freSpectrum[i].abs()/50f);
        }
        ArrayList<Double>[] SpectrumPoints = new ArrayList[11];
        double[] SpectrumFeatures = new double[11];
        for (int i = 0; i < 11; i++)
            SpectrumPoints[i] = new ArrayList<>();
        final double SampleRate = 50.0;
        final int SpectrumLength = Spectrum.size() - 1;

        final int density = (int)(SpectrumLength/(SampleRate * 0.5));

        for (int i = 1; i < SpectrumLength-1; i++) {
            int k = i/density;
            if (k< 10) SpectrumPoints[k].add(Spectrum.get(i));
            else SpectrumPoints[10].add(Spectrum.get(i));
        }

        for (int k = 0; k < SpectrumPoints.length; k++) {
            SpectrumFeatures[k] = new myStat().getMean(SpectrumPoints[k]);
            features.append("\t" + SpectrumFeatures[k]);
        }

        return features;
    }

//    public static void main(String[] args) throws IOException {
//        File f = new File("E://JAVA//data/car2.txt");
//        BufferedReader bf = new BufferedReader(new FileReader(f));
//        double[] accNorm = new double[256];
//        for(int i = 0;i<256;i++){
//            String[] data = bf.readLine().split("\t");
//            double [] accdata = new double[3];
//            double acc = 0f;
//            for(int j = 0;j<3;j++){
//                accdata[j] = Double.parseDouble(data[j+2]);
//                acc +=accdata[j]*accdata[j];
//            }
//            accNorm[i] = Math.sqrt(acc);
//            System.out.print(accNorm[i]+" ");
//            bf.readLine();
//        }
//        System.out.println();
//
//        StringBuffer feature = new HAR().featureExtract(accNorm);
//        System.out.println();
//        System.out.println(feature.toString());
//    }
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

    double getMean(ArrayList<Double> a) {
        double mean = 0;
        for (int i = 0; i < a.size(); i++) {
            mean += a.get(i) / a.size();
        }
        return mean;
    }
}

class FFT {

    // compute the FFT of x[], assuming its length is a power of 2
    public  Complex[] fft(Complex[] x) {
        int N = x.length;

        // base case
        if (N == 1) return new Complex[]{x[0]};

        // radix 2 Cooley-Tukey FFT
        if (N % 2 != 0) {
            throw new RuntimeException("N is not a power of 2");
        }

        // fft of even terms
        Complex[] even = new Complex[N / 2];
        for (int k = 0; k < N / 2; k++) {
            even[k] = x[2 * k];
        }
        Complex[] q = fft(even);

        // fft of odd terms
        Complex[] odd = even;  // reuse the array
        for (int k = 0; k < N / 2; k++) {
            odd[k] = x[2 * k + 1];
        }
        Complex[] r = fft(odd);

        // combine
        Complex[] y = new Complex[N];
        for (int k = 0; k < N / 2; k++) {
            double kth = -2 * k * Math.PI / N;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k] = q[k].add(wk.multiply(r[k]));
            y[k + N / 2] = q[k].subtract(wk.multiply(r[k]));
        }
        return y;
    }
}