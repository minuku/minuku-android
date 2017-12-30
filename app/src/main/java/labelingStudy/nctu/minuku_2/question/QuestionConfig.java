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

package labelingStudy.nctu.minuku_2.question;

import android.content.Context;

import com.github.dkharrat.nexusdialog.FormElementController;
import com.github.dkharrat.nexusdialog.controllers.CheckBoxController;
import com.github.dkharrat.nexusdialog.controllers.EditTextController;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import labelingStudy.nctu.minuku.manager.QuestionManager;
import labelingStudy.nctu.minukucore.exception.QuestionNotFoundException;
import labelingStudy.nctu.minukucore.model.question.FreeResponse;
import labelingStudy.nctu.minukucore.model.question.MultipleChoice;
import labelingStudy.nctu.minukucore.model.question.Question;
import labelingStudy.nctu.minukucore.model.question.Questionnaire;

/**
 * Created by shriti on 7/29/16.
 */
public class QuestionConfig {

    private static QuestionConfig instance;

    //constants for IDs and Strings and options - mcq, likert

    //missed report questions v1
    // notification title - we want to hear from you!
    public static final String MISSED_REPORT_QU_1_STRING =
            "Have you been busy for the last couple hours?";
    public static final String[] MISSED_REPORT_QU_1_VALUES =
            {"Yes", "No"};

    public static final Question MISSED_REPORT_QU_1 =
            new MultipleChoice(MISSED_REPORT_QU_1_STRING, 2, MISSED_REPORT_QU_1_VALUES);

    public static final String MISSED_REPORT_QU_2_STRING =
            "What have you been doing in the last two hours?";
    public static final Question MISSED_REPORT_QU_2 =
            new FreeResponse(MISSED_REPORT_QU_2_STRING);

    //missed report questions v2
    //notification title - we want to hear from you!
    public static final String MISSED_REPORT_QU_3_STRING = "We did not seem to have received  any logs from you today." +
            "To help us understand better, check all that apply.";
    public static final String[] MISSED_REPORT_QU_3_VALUES = {
            "I have done diabetes related activities but did not have time to log them",
            "I missed my diabetes related activities so I do not have my data"};
    public static final Question MISSED_REPORT_QU_3 = new MultipleChoice(MISSED_REPORT_QU_3_STRING,
            2,
            MISSED_REPORT_QU_3_VALUES);

    public static final String MISSED_REPORT_QU_4_STRING = "If you missed diabetes related activities, " +
            "please help us understand why did you miss them?";
    public static final Question MISSED_REPORT_QU_4 = new FreeResponse(MISSED_REPORT_QU_4_STRING);

    //mood change questions
    //notification title - tell us what happened?
    public static final String MOOD_CHANGE_QU_1_STRING =
            "There seems to be a considerable change in your mood. " +
                    "Did something happen that changed your mood?";
    public static final String[] MOOD_CHANGE_QU_1_VALUES ={"Yes", "No"};
    public static final Question MOOD_CHANGE_QU_1=
            new MultipleChoice(MOOD_CHANGE_QU_1_STRING, 2, MOOD_CHANGE_QU_1_VALUES);

    public static final String MOOD_CHANGE_QU_2_STRING =
            "If yes, what happened?";
    public static final Question MOOD_CHANGE_QU_2=
            new FreeResponse(MOOD_CHANGE_QU_2_STRING);


    /**public static final String MOOD_CHANGE_NEG_QU_STRING =
            "You went from being in a good mood to bad mood." +
            "Did something happen that changed your mood?";
    public static final Question MOOD_CHANGE_NEG_QU=
            new FreeResponse(MOOD_CHANGE_NEG_QU_STRING);

    public static final String MOOD_CHANGE_POS_QU_STRING =
            "You went from being in a bad mood to good mood." +
            "Did something happen that changed your mood?";
    public static final Question MOOD_CHANGE_POS_QU=
            new FreeResponse(MOOD_CHANGE_POS_QU_STRING);**/

    public static LinkedList<Question> questionsList = new LinkedList();
    public static LinkedList<Questionnaire> questionnaires = new LinkedList<>();
    private static List<Question> list1 = new LinkedList<>();
    private static List<Question> list2 = new LinkedList<>();
    private static List<Question> list3 = new LinkedList<>();

    public static Questionnaire missedReportQuestionnaire_1;
    public static Questionnaire missedReportQuestionnaire_2;
    public static Questionnaire moodChangeQuestionnaire;



    static {
        MISSED_REPORT_QU_1.setID(1);
        MISSED_REPORT_QU_2.setID(2);
        MISSED_REPORT_QU_3.setID(3);
        MISSED_REPORT_QU_4.setID(4);
        MOOD_CHANGE_QU_1.setID(5);
        MOOD_CHANGE_QU_2.setID(6);

        questionsList.add(MISSED_REPORT_QU_1);
        questionsList.add(MISSED_REPORT_QU_2);
        questionsList.add(MISSED_REPORT_QU_3);
        questionsList.add(MISSED_REPORT_QU_4);
        questionsList.add(MOOD_CHANGE_QU_1);
        questionsList.add(MOOD_CHANGE_QU_2);

        list1.add(MISSED_REPORT_QU_1);
        list1.add(MISSED_REPORT_QU_2);
        missedReportQuestionnaire_1 = new Questionnaire(1, list1);

        list2.add(MISSED_REPORT_QU_3);
        list2.add(MISSED_REPORT_QU_4);
        missedReportQuestionnaire_2 = new Questionnaire(2, list2);

        list3.add(MOOD_CHANGE_QU_1);
        list3.add(MOOD_CHANGE_QU_2);
        moodChangeQuestionnaire = new Questionnaire(3, list3);

        questionnaires.add(missedReportQuestionnaire_1);
        questionnaires.add(missedReportQuestionnaire_2);
        questionnaires.add(moodChangeQuestionnaire);
    }

    // make this a singleton
    private QuestionConfig() {

    }

    public static QuestionConfig getInstance() {
        if(instance == null)
            instance = new QuestionConfig();
        return instance;
    }
    // getControllerFor(MCQ, AC) throws ACNotSetEx
    // getControllerFor(FreeResponse, AC) throws ACNotSetEx

    // setup(AC)
        // create controllers for all created questions
        // calls register of QuMgr for each question<=>controller pair

    public static void setUpQuestions(Context context){
        for (Question question:QuestionConfig.questionsList) {
            try {
                QuestionManager.getInstance().registerQuestion(
                        question,
                        getControllerFor(question,context));

            } catch (QuestionNotFoundException e) {
                e.printStackTrace();
            }
        }
        for(Questionnaire questionnaire: questionnaires) {
            QuestionManager.getInstance().registerQuestionnaire(questionnaire, questionnaire.getID());
        }
    }

    public static <T extends Question, E extends FormElementController> E getControllerFor(
            T aQuestion, Context aContext) throws QuestionNotFoundException {

        if(aQuestion instanceof FreeResponse) {
            return (E)
                    new EditTextController(aContext,
                            String.valueOf(aQuestion.getID()),
                            aQuestion.getQuestion());
        } else if (aQuestion instanceof MultipleChoice) {
            MultipleChoice mcq = (MultipleChoice) aQuestion;
            return (E)
                    new CheckBoxController(aContext,
                            String.valueOf(aQuestion.getID()),
                            aQuestion.getQuestion(),
                            true /* isRequired */,
                            Arrays.asList(mcq.getLabels()),
                            true /* useItemsAsValues */);
        }
        throw new QuestionNotFoundException();
    }
}
