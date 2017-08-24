package uk.ac.ox.eng.stepcounter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jamie Brynes on 1/22/2017.
 */

public class PostProcessStage implements Runnable {

    private List<DataPoint> inputQueue;
    private OnNewStepDetected newStepInterface;
    private OnEndOfData endOfDataInterface;

    private DataPoint current;
    private DataPoint dp;
    private boolean active;


    /*
        Section for parameter definitions
     */

    private final int timeThreshold = 200;



    public interface OnNewStepDetected {

        void incrementSteps();
    }

    public interface OnEndOfData {
        void EodCallback();
    }

    public PostProcessStage(List<DataPoint> input, OnNewStepDetected newStepCallback, OnEndOfData endOfDataCallback) {

        inputQueue = input;
        newStepInterface = newStepCallback;
        endOfDataInterface = endOfDataCallback;

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
                    endOfDataInterface.EodCallback();
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
                        newStepInterface.incrementSteps();
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
