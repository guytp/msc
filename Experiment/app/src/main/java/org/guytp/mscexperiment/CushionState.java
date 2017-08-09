package org.guytp.mscexperiment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by guytp on 28/06/17.
 */

public enum CushionState {
    Happy(1), Sad(2), Calm(3), Angry(4), None(0);

    private int _state;
    private static Random _rand = new Random();

    private CushionState(int state){
        this._state = state;
    }

    public static CushionState getCushionState(int forNumber) {
        switch (forNumber) {
            case 0:
                return None;
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

    public static CushionState[] generateRandomSamePairs(int pairsRequired, double percentageChangeSameState) {
        // First for each pair we want to generate as many randomly ordered states as possible
        int groupingsRequired = pairsRequired / 4;
        if (pairsRequired % 4 != 0)
            groupingsRequired++;
        List firstInPairList = new ArrayList();
        for (int i = 0; i < groupingsRequired; i++) {
            CushionState[] groupOfStates = randomlyOrderedStates();
            for (int j = 0; j < groupOfStates.length; j++)
                firstInPairList.add(groupOfStates[j]);
        }

        // Now we have the first of each pair we need to go through the number of pairs required and create second pairs
        // with random percentage of same
        CushionState[] states = new CushionState[pairsRequired * 2];
        for (int i = 0; i < pairsRequired; i++) {
            CushionState firstInPair = (CushionState)firstInPairList.get(i);
            CushionState secondInPair;
            Boolean generateSame = _rand.nextDouble() <= percentageChangeSameState;
            if (generateSame)
                secondInPair = firstInPair;
            else {
                // We need to guarantee they are not the same now
                final int max = 4;
                final int min = 1;
                while (true) {
                    int stateNumber = _rand.nextInt((max - min) + 1) + min;
                    CushionState candidateSecond = getCushionState(stateNumber);
                    if (candidateSecond != firstInPair) {
                        secondInPair = candidateSecond;
                        break;
                    }
                }
            }
            states[i * 2] = firstInPair;
            states[(i * 2) + 1] = secondInPair;
        }

        // Now return our values to the caller
        return states;
    }

    public String toString(){
        switch (_state) {
            case 0:
                return "None";
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
