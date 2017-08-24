package uk.ac.ox.eng.stepcounter;

import java.util.List;

/**
 * Created by Jamie Brynes on 1/22/2017.
 */

public class DetectionStage implements Runnable {

    private List<DataPoint> inputQueue;
    private List<DataPoint> outputQueue;
    private DataPoint dp;

    private boolean active;

    private int count;
    private float mean;
    private float std;

    /*
        Section for defining parameters.
     */
    private final float threshold = 1.2f;

    public DetectionStage(List<DataPoint> input, List<DataPoint> output) {

        inputQueue = input;
        outputQueue = output;
        active = false;
        count = 0;
        mean = 0f;
        std = 0f;
        dp = null;
    }

    public void run() {

        active = true;

        while (active) {

            if (!inputQueue.isEmpty()) {
                dp = inputQueue.remove(0);
            }

            if (dp != null) {

                // Special handing for end of stream.
                if (dp.getEos()) {
                    active = false;
                    outputQueue.add(dp);
                    continue;
                }

                // Update calculations of std and mean.
                count++;
                float o_mean = mean;
                switch(count) {
                    case 1:
                        mean = dp.getMagnitude();
                        std = 0f;
                        break;
                    case 2:
                        mean = (mean + dp.getMagnitude()) / 2;
                        std = (float)Math.sqrt(Math.pow(dp.getMagnitude() - mean,2) + Math.pow(o_mean - mean,2)) / 2;
                        break;
                    default:
                        mean = (dp.getMagnitude() + (count - 1) * mean) / count;
                        std = (float)Math.sqrt(((count - 2) * Math.pow(std,2) / (count - 1)) + Math.pow(o_mean - mean, 2) +  Math.pow(dp.getMagnitude() - mean,2) / count);
                }

                // Once we have enough data points to have a reasonable mean/standard deviation, start detecting
                if (count > 15) {
                    if ((dp.getMagnitude() - mean) > std * threshold) {
                        // This is a peak
                        outputQueue.add(new DataPoint(dp.getTime(),dp.getMagnitude()));
                    }
                }

                dp = null;
            }
        }

    }
}
