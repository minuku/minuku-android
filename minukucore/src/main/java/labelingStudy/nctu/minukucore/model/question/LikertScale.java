/*
 * Copyright (c) 2016.
 *
 * DReflect and Minuku Libraries by Shriti Raj (shritir@umich.edu) and Neeraj Kumar(neerajk@uci.edu) is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * Based on a work at https://github.com/Shriti-UCI/Minuku-2.
 *
 *
 * You are free to (only if you meet the terms mentioned below) :
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 *
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package labelingStudy.nctu.minukucore.model.question;

/**
 * A Likert scale question gives user a question and expects an answer on a gradient.
 * @see <a href="https://en.wikipedia.org/wiki/Likert_scale">Wiki on Likert Scale</a> for more info.
 *
 * Created by neerajkumar on 7/13/16.
 */
public abstract class LikertScale extends Question {

    private static int numSteps;
    private static String[] stepValues;

    private int answerValue;

    public LikertScale() {

    }

    /**
     *
     * @param aQuestion The question to show.
     * @param aNumSteps The number of steps on the likert scale.
     * @param aStepValues Label to be shown for each tick on the scale.
     */
    public LikertScale(String aQuestion, int aNumSteps, String[] aStepValues) {
        super(aQuestion);
        stepValues = aStepValues;
        numSteps = aNumSteps;
    }

    /**
     *
     * @return Number of steps on the likert scale.
     */
    public int getNumSteps() {
        return numSteps;
    }

    public void setNumSteps(int steps) {
        numSteps = steps;
    }

    /**
     *
     * @return The array of labels for the scale.
     */
    public String[] getStepValues() {
        return stepValues;
    }

    public void setStepValues(String[] values) {
        stepValues = values;
    }

    /**
     *
     * @return The answer selected by the user.
     */
    public int getAnswerValue() {
        return answerValue;
    }

    /**
     * Sets the answer value.
     * @param aAnswerValue The answer selected by the user.
     */
    public void setAnswerValue(int aAnswerValue) {
        this.answerValue = aAnswerValue;
    }
}
