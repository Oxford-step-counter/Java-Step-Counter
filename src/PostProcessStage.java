package com.jamie.android.step_counter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jamie Brynes on 1/22/2017.
 */

public class PostProcessStage implements Runnable {

    private List<DataPoint> inputQueue;
    private OnNewStepDetected callbackInterface;

    private DataPoint current;
    private DataPoint dp;
    private boolean active;


    /*
        Section for parameter definitions
     */

    private final int timeThreshold = 150;



    public interface OnNewStepDetected {

        void incrementSteps();
    }

    public PostProcessStage(List<DataPoint> input, OnNewStepDetected callback) {

        inputQueue = input;
        callbackInterface = callback;

        active = false;
        current = null;
        dp = null;
    }

    public void run() {

        active = true;

        while (active) {

            if (!inputQueue.isEmpty()) {
                dp = inputQueue.remove(0);
            }

            if (dp != null) {

                if (dp.getEos()) {
                    active = false;
                    continue;
                }

                // First point handler.
                if (current == null) {
                    current = dp;
                }
                else {

                    // If the time difference exceeds the threshold, we have a confirmed step
                    if ((dp.getTime() - current.getTime()) > timeThreshold) {
                        current = dp;
                        callbackInterface.incrementSteps();
                    } else {
                        // Keep the point with the largest magnitude.
                        if (dp.getMagnitude() > current.getMagnitude()) {
                            current = dp;
                        }
                    }

                }

                dp = null;
            }


        }

    }
}
