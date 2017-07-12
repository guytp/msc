package org.guytp.mscexperiment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by guytp on 28/06/17.
 */

public enum CushionState {
    Happy(1), Sad(2), Calm(3), Angry(4);

    private int _state;
    private static Random _rand = new Random();

    private CushionState(int state){
        this._state = state;
    }

    public static CushionState getCushionState(int forNumber) {
        switch (forNumber) {
            case 1:
                return Happy;
            case 2:
                return Sad;
            case 3:
                return Calm;
            default:
                return Angry;
        }
    }

    public static CushionState[] randomlyOrderedStatesSets(int numberOfSets) {
        if (numberOfSets < 1)
            return null;
        CushionState[] states = new CushionState[4 * numberOfSets];
        int overallOffset = 0;
        for (int i = 0; i < numberOfSets; i++) {
            CushionState[] localStates = randomlyOrderedStates();
            for (int j = 0; j < 4; j++, overallOffset++)
                states[overallOffset] = localStates[j];
        }
        return states;
    }

    public static CushionState[] randomlyOrderedStates() {
        int[] statesAsInts = new int[4];
        CushionState[] states = new CushionState[4];
        int index = 0;
        final int max = 4;
        final int min = 1;
        while (index < 4) {
            // Generate the next random number
            int randomNum = _rand.nextInt((max - min) + 1) + min;

            // Check if we've already found this number - if so don't use it again
            Boolean preExists = false;
            for (int i = 0; i < index; i++)
                if (statesAsInts[i] == randomNum) {
                    preExists = true;
                    break;
                }
            if (preExists)
                continue;

            // Add it to the list
            statesAsInts[index] = randomNum;
            states[index] = getCushionState(randomNum);
            index++;
        }

        // Now we've added all of the random states we can return the list
        return states;
    }

    public static CushionState[] generateRandomStates(int totalRequired, double percentageChangeSameState) {
        List stateList = new ArrayList();
        while (true) {
            // If this is not first one we're adding check if we should duplicate the last
            if (stateList.size() > 0 && _rand.nextDouble() < percentageChangeSameState)
                stateList.add(stateList.get(stateList.size() - 1));

            // Otherwise add a randomly generated state
            else
                stateList.add(getCushionState((_rand.nextInt((400 - 1) + 1) + 1) / 100));

            // Are we in a good position to break?
            if (stateList.size() == totalRequired)
                break;
        }
        CushionState[] states = new CushionState[stateList.size()];
        for (int i = 0; i < stateList.size(); i++)
            states[i] = (CushionState)stateList.get(i);
        return states;
    }

    public String toString(){
        switch (_state) {
            case 1:
                return "Happy";
            case 2:
                return "Sad";
            case 3:
                return "Calm";
            default:
                return "Angry";
        }
    }

    public int rawValue() {
        return _state;
    }
}
