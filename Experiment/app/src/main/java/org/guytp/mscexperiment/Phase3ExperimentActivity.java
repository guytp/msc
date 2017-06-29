package org.guytp.mscexperiment;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Phase3ExperimentActivity extends KioskActivity {

    private final String[] _unsortedEmotionWords = {
            "Excited", "Happy", "Surprised", "Delighted", "Cheerful", // High Energy, Pleasant
            "Relaxed", "Calm", "Serene", "Content", "Pleased",  // Low Energy, Pleasant
            "Sad", "Depressed", "Gloomy", "Bored", "Tired", // Low Energy, Unpleasant
            "Afraid", "Angry", "Annoyed", "Frustrated", "Terrified" // High Energy, Unpleasant
            };
            // Mix of Russell and Schubert : https://www.researchgate.net/figure/228583824_fig10_Figure-10-Ratings-of-emotion-words-in-a-two-dimensional-emotion-space-according-to

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phase3_experiment);

        // Setup that phase 3 experiment has started
        ExperimentData.getInstance().addTimeMarker("Phase3Experiment", "Show");
    }

    // TODO: Handle Phase3Experiment.Finish timestamp
}
