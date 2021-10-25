package com.example.candice.models;


//The following is the FlashcardModel which tracks via the unique ID
//Same as the TranslationModel
public class FlashcardModel {
    String ID;
    private final String Question;
    private final String Answer;

    public FlashcardModel(String ID, String question, String answer) {
        this.ID = ID;
        this.Question = question;
        this.Answer = answer;
    }

    public String getID() {
        return ID;
    }

    public String getQuestion() {
        return Question;
    }

    public String getAnswer() {
        return Answer;
    }
}
