package com.jamie.android.step_counter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jamie Brynes on 1/22/2017.
 */

public class FilterStage implements Runnable {

    private List<DataPoint> inputQueue;
    private List<DataPoint> outputQueue;

    private ArrayList<DataPoint> window;
    private ArrayList<Float> filterCoefficients;

    private boolean active = false;
    private DataPoint dp; 


    /*
        Section for parameter definitions
     */
    private final int windowSize = 21;

    public FilterStage(List<DataPoint> input, List<DataPoint> output) {

        inputQueue = input;
        outputQueue = output;

        window = new ArrayList<DataPoint>();
        filterCoefficients = generateCoefficients();

        dp = null;
    }

    public void run() {

        active = true;

        while (active) {

            if (!inputQueue.isEmpty()) {
                dp = inputQueue.remove(0);
            }

            if (dp != null) {


                // Special handling for final data point.
                if (dp.getEos()) {
                    active = false;
                    outputQueue.add(dp);
                    continue;
                }

                window.add(dp);

                if (window.size() == windowSize) {

                    float sum = 0;
                    for (int i = 0; i < windowSize; i++) {
                        sum += window.get(i).getMagnitude() * filterCoefficients.get(i);
                    }

                    DataPoint new_dp = new DataPoint(window.get(windowSize / 2).getTime(), sum);
                    outputQueue.add(new_dp);
                    window.remove(0);
                }

                dp = null;
            }
        }

    }

    private ArrayList<Float> generateCoefficients() {

        // Create a window of the correct size.
        ArrayList<Float> coeff = new ArrayList<Float>(windowSize);

        for (int i = 0; i < windowSize; i++) {
            coeff.set(i, (float)1/windowSize);
        }

        return coeff;
    }
}
