package uk.ac.ox.eng.stepcounter;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jamie Brynes on 1/22/2017.
 */

public class PreProcessStage implements Runnable {

    private List<DataPoint> inputQueue;
    private List<DataPoint> outputQueue;

    private ArrayList<DataPoint> window;
    private DataPoint dp;

    private final int interpolationTime = 10; // In ms.
    private final float timeScalingFactor = 1000000f; // Convert ns to ms.
    private int interpolationCount = 0;

    private float startTime = -1;

    private boolean active = false;

    public PreProcessStage(List<DataPoint> input, List<DataPoint> output) {

        inputQueue = input;
        outputQueue = output;
        window = new ArrayList<>();
        dp = null;
    }

    public void run() {

        active = true;

        while (active) {

            // If there is a new point, retrieve it, limit operations on inputQueue to not block other threads.
            if (!inputQueue.isEmpty()) {
                dp = inputQueue.remove(0);
            }

            //Scale time and add to window.
            if (dp != null) {

                // This signals the end of the data stream.
                if (dp.getEos()) {
                    active = false;
                    outputQueue.add(dp);
                    continue;
                }

                // Handling for the first data point in the stream.
                if (startTime == -1) {
                    startTime = dp.getTime();
                }

                dp.setTime(scaleTime(dp.getTime()));
                window.add(dp);
                dp = null;
            }

            // We have enough data points to interpolate.
            if (window.size() >= 2) {
                float time1 = window.get(0).getTime();
                float time2 = window.get(1).getTime();

                // This defines the number of points that could exist in between the points.
                int numberOfPoints = (int) Math.ceil((time2 - time1) / interpolationTime);

                for (int i = 0; i < numberOfPoints; i++) {
                    float interpTime = (float) interpolationCount * interpolationTime;

                    // Check if the next interpolated time is between these two points.
                    if (time1 <= interpTime && interpTime <= time2) {
                        DataPoint interpolated = linearInterpolate(window.get(0), window.get(1), interpTime);
                        outputQueue.add(interpolated);
                        interpolationCount += 1;
                    }
                }

                // Remove the oldest element in the list.
                window.remove(0);
            }
        }

    }

    private float scaleTime(float ogTime) {

        return (ogTime - startTime) / timeScalingFactor;
    }

    private DataPoint linearInterpolate(DataPoint dp1, DataPoint dp2, float interpTime) {

        float dt = dp2.getTime() - dp1.getTime();
        float dv = dp2.getMagnitude() - dp2.getMagnitude();
        float mag = (dv/dt) * (interpTime - dp1.getTime()) + dp1.getMagnitude();

        return new DataPoint(interpTime,mag);
    }
}
