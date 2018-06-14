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
 * A multiple choice question gives users a specific question with a list of possible answers the
 * user needs to choose from.
 * @see <a href="https://en.wikipedia.org/wiki/Multiple_choice"> Wiki on Multiple Choice</a>.
 *
 * Created by neerajkumar on 7/13/16.
 */
public class MultipleChoice extends Question {

    private int numChoices;
    private String[] labels;
    private Integer[] selectedAnswerValues;

    public MultipleChoice () {

    }

    /**
     *
     * @param aQuestion The question to show.
     * @param aNumChoices The number of choices this MCQ will have.
     * @param aChoiceValues The label(s) for each choice.
     */
    public MultipleChoice(String aQuestion, int aNumChoices, String[] aChoiceValues) {
        super(aQuestion);
        numChoices = aNumChoices;
        labels = aChoiceValues;
    }

    /**
     *
     * @return The number of choices.
     */
    public int getNumChoices() {
        return numChoices;
    }

    /**
     *
     * @return The labels for choices.
     */
    public String[] getLabels() {
        return labels;
    }

    /**
     *
     * @return The answer selected by the user.
     */
    public Integer[] getSelectedAnswerValues() {
        return selectedAnswerValues;
    }

    /**
     * Sets the answer value.
     * @param aAnswerValue The answer selected by the user.
     */
    public void setSelectedAnswerValues(Integer[] aAnswerValue) {
        this.selectedAnswerValues = aAnswerValue;
    }
}
