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

package labelingStudy.nctu.minuku.manager;

import com.github.dkharrat.nexusdialog.FormElementController;

import java.util.HashMap;
import java.util.Map;

import labelingStudy.nctu.minukucore.model.question.Question;
import labelingStudy.nctu.minukucore.model.question.Questionnaire;

/**
 * Created by shriti on 7/29/16.
 */
public class QuestionManager {

    protected Map<Question, FormElementController> questionMap;
    protected Map<Integer, Questionnaire> questionnaireMap;
    private static QuestionManager instance;

    private QuestionManager () {
        this.questionMap = new HashMap<>();
        this.questionnaireMap = new HashMap<>();
    }

    public static QuestionManager getInstance() {
        if (instance == null) {
            instance = new QuestionManager();
        }
        return instance;
    }

    public FormElementController getControllerforQuestion(Question question) {
        //get ID for question and return controller for the sames
        return questionMap.get(question);
    }

    public boolean questionExists(Question question) {
        return questionMap.containsKey(question);
    }

    public void registerQuestion(Question question, FormElementController formElementController) {
        questionMap.put(question, formElementController);
    }

    public void registerQuestionnaire(Questionnaire questionnaire, int id) {
        questionnaireMap.put(id, questionnaire);
    }

    public Map getQuestionFormControllerMap() {
        return this.questionMap;
    }

    public Questionnaire getQuestionnaireForID(int id) {
        return questionnaireMap.get(id);
    }

}
