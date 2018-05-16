package com.ustc.wsn.mobileData.bean.math;

/**
 * Created by halo on 2018/5/9.
 */

import android.util.Log;

import com.ustc.wsn.mobileData.bean.PhoneState;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;

import java.util.ArrayList;

public class LinearFit {

    private ArrayList<float[]> fitData;
    private int degree;
    private float Slope;
    private float symbol;
    private int PhoneStateDuringPath;

    public LinearFit(ArrayList<float[]> pathData, int degree, int PhoneStateDuringPath) {
        fitData = pathData;
        this.degree = degree;
        this.PhoneStateDuringPath = PhoneStateDuringPath;
    }

    public void Fitting() {

        if (PhoneStateDuringPath == PhoneState.USER_STATIC_STATE || PhoneStateDuringPath == PhoneState.UNKONW_STATE) {
            PolynomialCurveFitter polynomialCurveFitter = PolynomialCurveFitter.create(degree);

            ArrayList weightedObservedPoints = new ArrayList();

            float symbolProbability = 0;
            float SIZE = fitData.size();

            for (int i = 0; i < fitData.size(); i++) {
                if (i > 0) {
                    if (fitData.get(i - 1)[0] < fitData.get(i)[0]) {
                        symbolProbability += 1f / SIZE;
                    } else {
                        symbolProbability -= 1f / SIZE;
                    }
                }
                WeightedObservedPoint weightedObservedPoint = new WeightedObservedPoint(1, fitData.get(i)[0], fitData.get(i)[1]);

                weightedObservedPoints.add(weightedObservedPoint);

            }
            Log.d("TAG", "symbolProbability\t" + symbolProbability);
            if (symbolProbability > 0.5f) {//  if P(x[i] < x[i+1])>0.5, symbol = 1, or symbol = -1
                symbol = 1f;
            } else {
                symbol = -1f;
            }
            double[] fitResult = polynomialCurveFitter.fit(weightedObservedPoints);

            float[] fitParams = new float[fitResult.length];
            for (int i = 0; i < fitResult.length; i++) {
                fitParams[i] = (float) fitResult[i];
            }
            this.Slope = fitParams[1];//斜率
        }
    }

    public float[] getUnitVector() {
        if (PhoneStateDuringPath == PhoneState.USER_STATIC_STATE || PhoneStateDuringPath == PhoneState.UNKONW_STATE) {
            float[] vector = {0, 0, 0};

            if (this.Slope > 0) {//1-3象限
                vector[0] = (float) (1 / Math.sqrt(1 + (this.Slope * this.Slope))) * symbol;
                vector[1] = (float) (Math.sqrt(1 - vector[0] * vector[0])) * symbol;
            } else {//2-4象限
                vector[0] = (float) (1 / Math.sqrt(1 + (this.Slope * this.Slope))) * symbol;
                vector[1] = -(float) (Math.sqrt(1 - vector[0] * vector[0])) * symbol;
            }
            return vector;
        }
        float[] unUsefulFit = {-100, -100, -100};
        return unUsefulFit;
    }
}
