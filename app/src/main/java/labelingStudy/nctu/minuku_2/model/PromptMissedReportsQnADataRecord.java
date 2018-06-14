package labelingStudy.nctu.minuku_2.model;

import java.util.List;

import labelingStudy.nctu.minukucore.model.question.Question;

/**
 * Created by shriti on 11/9/16.
 */

public class PromptMissedReportsQnADataRecord extends Question{

    //storing only the question and response as a string, regardless of the type of question
    public List<String> answer;

    public PromptMissedReportsQnADataRecord() {
    }

    public PromptMissedReportsQnADataRecord(String aQuestion) {
        super(aQuestion);
    }

    public List<String> getAnswer() {

        return answer;
    }

    public void setAnswer(List<String> answer) {

        this.answer = answer;
    }

}
