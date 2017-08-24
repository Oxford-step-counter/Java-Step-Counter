package uk.ac.ox.eng.stepcounter;

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

    private final static int FILTER_LENGTH = 35;
    private final static float FILTER_STD = 0.35f;

    private boolean active = false;
    private DataPoint dp; 

    public FilterStage(List<DataPoint> input, List<DataPoint> output) {

        inputQueue = input;
        outputQueue = output;

        window = new ArrayList<DataPoint>();
        filterCoefficients = FilterStage.generateCoefficients();

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

                if (window.size() == FILTER_LENGTH) {

                    float sum = 0;
                    for (int i = 0; i < FILTER_LENGTH; i++) {
                        sum += window.get(i).getMagnitude() * filterCoefficients.get(i);
                    }

                    DataPoint new_dp = new DataPoint(window.get(FILTER_LENGTH / 2).getTime(), sum);
                    outputQueue.add(new_dp);
                    window.remove(0);
                }

                dp = null;
            }
        }

    }

    private static ArrayList<Float> generateCoefficients() {

        // Create a window of the correct size.
        ArrayList<Float> coeff = new ArrayList<Float>();

        for (int i = 0; i < FILTER_LENGTH; i++) {
            float value = (float) Math.pow(Math.E, -0.5 * Math.pow((i - (FILTER_LENGTH - 1) / 2) / (FILTER_STD * (FILTER_LENGTH - 1) / 2), 2));
            coeff.add(value);
        }

        return coeff;
    }
}
