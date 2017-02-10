package com.jamie.android.step_counter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jamie Brynes on 1/22/2017.
 */

public class ScoringStage implements Runnable {

    private List<DataPoint> inputQueue;
    private List<DataPoint> outputQueue;

    private ArrayList<DataPoint> window;

    private boolean active;
    private DataPoint dp;


    /*
        Section for parameter definitions
     */
    private final int windowSize = 11;

    public ScoringStage(List<DataPoint> input, List<DataPoint> output) {

        inputQueue = input;
        outputQueue = output;

        window = new ArrayList<>();
        active = false;
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

                    // Calculate score and append to the output window.
                    float score = scorePeak(window);
                    outputQueue.add(new DataPoint(window.get(windowSize / 2).getTime(),score));
                    // Pop out the oldest point.
                    window.remove(0);
                }
                dp = null;
            }

        }

    }

    private float scorePeak(ArrayList<DataPoint> data) {
        //TODO: Implement this.
        return 0f;
    }
}
