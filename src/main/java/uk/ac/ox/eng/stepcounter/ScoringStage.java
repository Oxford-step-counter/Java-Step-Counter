package uk.ac.ox.eng.stepcounter;

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
    private static final int WINDOW_SIZE = 11;

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

                if (window.size() == WINDOW_SIZE) {

                    // Calculate score and append to the output window.
                    float score = scorePeak(window);
                    outputQueue.add(new DataPoint(window.get(WINDOW_SIZE / 2).getTime(),score));
                    // Pop out the oldest point.
                    window.remove(0);
                }
                dp = null;
            }

        }

    }

    private float scorePeak(ArrayList<DataPoint> data) {
        int midpoint = (int) data.size() / 2;
        float diffLeft = 0f;
        float diffRight = 0f;

        for(int i = 0; i < midpoint; i++) {
            diffLeft += data.get(midpoint).getMagnitude() - data.get(i).getMagnitude();
        }

        for (int j = midpoint + 1; j < data.size(); j++) {
            diffRight += data.get(midpoint).getMagnitude() - data.get(j).getMagnitude();
        }

        return (diffRight + diffLeft) / (WINDOW_SIZE - 1);
    }
}
