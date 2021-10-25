package com.example.candice.models;

//Translation History Model
//ID is used to get the unique ID of the item where as the other fields contain data for the desired text
//i.e. Source Language = what language the source was in
//This is a middleman class essentially, this passes information to and from the TranslationDatabase to X Fragment
public class TranslationModel {
    String ID;
    private final String sourceLanguage;
    private final String targetLanguage;
    private final String sourceText;
    private final String targetText;

    public TranslationModel(String ID, String sourceLanguage, String targetLanguage, String sourceText, String targetText) {
        this.ID = ID;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
        this.sourceText = sourceText;
        this.targetText = targetText;
    }

    public String getID() {
        return ID;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public String getSourceText() {
        return sourceText;
    }

    public String getTargetText() {
        return targetText;
    }

}
