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

import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * A question is a specific type of {@link DataRecord}. Each time a new question is supposed to
 * be asked of the user, a new subclass of Question must be created. e.g:
 * QuestionOne extends Question --> question = "How was your day?"
 * QuestionTwo extends Question --> question = "How was your mood after school?".
 * Objects of QuestionOne and QuestionTwo will have their own stream(s).
 *
 * Created by neerajkumar on 7/13/16.
 */
public abstract class Question implements DataRecord {
    // protected UUID questionnaireID;

    private String question;
    private int ID;
    private long creationTime;

    public Question() {

    }

    /**
     *
     * @param aQuestion The question string. This will not be persisted in DB, but must be a part
     *                  of the code.
     */
    public Question(String aQuestion) {
        this.question = aQuestion;
        this.creationTime = new Date().getTime();
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public long getCreationTime() {
        return  this.creationTime;
    }

    public String getQuestion() { return this.question; }

    public void setQuestion(String aQuestion) { this.question = aQuestion; }

    @Override
    public int hashCode() {
        return this.ID;
    }
}
