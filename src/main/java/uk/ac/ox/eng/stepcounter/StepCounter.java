package uk.ac.ox.eng.stepcounter;

import java.util.ArrayList;
import java.lang.Math;
import java.util.Collections;
import java.util.List;

/**
 * Created by Jamie Brynes on 1/22/2017.
 */
public class StepCounter {


    /**
     * This is the interface for getting updates when we get a step occuring. Simple listener/broadcaster pattern.
     */
    public interface OnStepUpdateListener {

        /**
         * This method will be called by a background thread and hence any UI changes done in the implementation of this MUST be done in the runInUiThread() way.
         */
        void onStepUpdate(int steps);

    }

    public interface OnFinishedProcessingListener {

        /**
         * This method will get called when the remaining data points finish processing. The owner of the object can make UI adjustments as necessary.
         */
        void onFinishedProcessing();

    }

    private float samplingFreq;
    private int steps;
    private boolean active;

    private List<DataPoint> rawData;
    private List<DataPoint> ppData;
    private List<DataPoint> smoothData;
    private List<DataPoint> peakScoreData;
    private List<DataPoint> peakData;

    private Runnable preProcessStage;
    private Runnable filterStage;
    private Runnable scoringStage;
    private Runnable detectionStage;
    private Runnable postProcessStage;

    private ArrayList<OnStepUpdateListener> callbacks;
    private PostProcessStage.OnNewStepDetected newStepCallback;
    private PostProcessStage.OnEndOfData eodCallback;
    private OnFinishedProcessingListener finishCallback;


    /**
     * [Constructor for the StepCounter module.]
     * @param  samplingFreq [This parameter describes the sampling frequency of the sensor.]
     * @return              [Instance of Step Counter]
     */
    public StepCounter(float samplingFreq) {

        this.samplingFreq = samplingFreq;
        newStepCallback = new PostProcessStage.OnNewStepDetected() {
            @Override
            public void incrementSteps() {
                incSteps();
            }
        };

        eodCallback = new PostProcessStage.OnEndOfData() {
            @Override
            public void EodCallback() {
                if (finishCallback != null) {
                    finishCallback.onFinishedProcessing();
                }
            }
        };

        // Initialize callback list.
        callbacks = new ArrayList<>();

    }

    /**
     * This function describes the set-up required for a new data recording.
     */
    private void setUp() {
        this.steps = 0;
        this.active = false;

        // Initialize thread-safe lists.
        rawData = Collections.synchronizedList(new ArrayList<DataPoint>());
        ppData = Collections.synchronizedList(new ArrayList<DataPoint>());
        smoothData = Collections.synchronizedList(new ArrayList<DataPoint>());
        peakScoreData = Collections.synchronizedList(new ArrayList<DataPoint>());
        peakData = Collections.synchronizedList(new ArrayList<DataPoint>());

    }

    /**
     * [This function starts the Step Counter algorithm.]
     */
    public void start(){

        if (!active) {
            // Reset threads and stages.
            setUp();
            active = true;
            new Thread( new PreProcessStage(rawData, ppData)).start();
            new Thread( new FilterStage(ppData, smoothData)).start();
            new Thread( new ScoringStage(smoothData, peakScoreData)).start();
            new Thread( new DetectionStage(peakScoreData, peakData)).start();
            new Thread( new PostProcessStage(peakData, newStepCallback, eodCallback)).start();
        }
    }


    /**
     * [This function stops the Step Counter algorithm. Current behavior is to finish processing all remaining samples before ending the threads.]
     */
    public void stop() {

        if (active) {
            //Signal that this is the end of the data stream. This is a special data point that says 'end of stream.'
            active = false;
            DataPoint dp = new DataPoint(0f,0f);
            dp.setEos(true);
            rawData.add(dp);
        }

    }


    /**
     * [This function allows the user to add a callback for when we get a new step!]]
     * @param listener [Implementation of the OnStepUpdateListener]
     */
    public synchronized void addOnStepUpdateListener(OnStepUpdateListener listener) {
        callbacks.add(listener);
    }


    public synchronized void setOnFinishedProcessingListener(OnFinishedProcessingListener listener){
        finishCallback = listener;
    }


    /**
     * [This function allows for callbacks to listeners when steps is updated.]
     */
    public synchronized void incSteps() {
        steps++;
        for (OnStepUpdateListener listener : callbacks) {
            listener.onStepUpdate(steps);
        }
    }


    /**
     * [This function is the public interface to add a new accelerometer sample to the step counter algorithm.]
     * @param time   [The timestamp of the sample in nanoseconds.]
     * @param sample [An array of accelerometer values [x,y,z] in m/s^2.]
     */
    public void processSample(long time, float[] sample) {

        if (active) {
            float magnitude = 0;
            for (float m : sample) {
                magnitude += m * m;
            }
            magnitude = (float) Math.sqrt(magnitude);

            rawData.add(new DataPoint(time, magnitude));
        }
    }


    /**
     * [Getter function for the number steps.]
     * @return [Returns the number of steps.]
     */
    public synchronized int getSteps() {
        return this.steps;
    }


    /**
     * [Getter function for the sampling frequency.]
     * @return [Returns the sampling frequency.]
     */
    public float getSamplingFreq() {
        return samplingFreq;
    }


    /**
     * [Setter function for the sampling frequency.]
     * @param samplingFreq [The sampling frequency to set.]
     */
    public void setSamplingFreq(float samplingFreq) {
        this.samplingFreq = samplingFreq;
    }

}
