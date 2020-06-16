package com.roeico7.dogadopt.Translate;

import android.util.Log;

import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateRemoteModel;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

public class Translator {
    private String text;
    private Listener listener;
    private DownloadListener downloadListener;



    public interface Listener{
        void textTranslated(String translatedText);
    }

    public interface DownloadListener {
        void downloadFinished();
        void downloadStart();
        void currentModel(Boolean isDownloaded);
    }



    public Translator(String text, Listener listener) {
        this.text = text;
        this.listener = listener;
        downloadTranslatorAndTranslate();
    }



    public Translator(DownloadListener listener) {
        this.downloadListener = listener;
    }



    public void translateText(FirebaseTranslator langTranslator) {
            langTranslator.translate(text)
                    .addOnSuccessListener(
                            translatedText -> listener.textTranslated(translatedText))
                    .addOnFailureListener(
                            e -> {
                                e.printStackTrace();
                            });

    }




    public void downloadTranslatorAndTranslate() {
        //create translator for source and target languages
        FirebaseTranslatorOptions options;
        if(Translate.shared.checkDisplayLanguage()) {
            options =
                    new FirebaseTranslatorOptions.Builder()
                            .setSourceLanguage(FirebaseTranslateLanguage.EN)
                            .setTargetLanguage(FirebaseTranslateLanguage.HE)
                            .build();
        } else {
            options =
                    new FirebaseTranslatorOptions.Builder()
                            .setSourceLanguage(FirebaseTranslateLanguage.HE)
                            .setTargetLanguage(FirebaseTranslateLanguage.EN)
                            .build();
        }


        final FirebaseTranslator langTranslator =
                FirebaseNaturalLanguage.getInstance().getTranslator(options);

        //download language models if needed
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        langTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        v -> {
                            Log.d("translator", "downloaded lang  model");
                            //after making sure language models are available
                            //perform translation
                            translateText(langTranslator);
                        })
                .addOnFailureListener(
                        e -> {

                        });
    }





    public void downloadLanguage() {
            //create translator for source and target languages
        FirebaseTranslatorOptions options;
        if(Translate.shared.checkDisplayLanguage()) {
            options =
                    new FirebaseTranslatorOptions.Builder()
                            .setSourceLanguage(FirebaseTranslateLanguage.EN)
                            .setTargetLanguage(FirebaseTranslateLanguage.HE)
                            .build();
        } else {
            options =
                    new FirebaseTranslatorOptions.Builder()
                            .setSourceLanguage(FirebaseTranslateLanguage.HE)
                            .setTargetLanguage(FirebaseTranslateLanguage.EN)
                            .build();
        }


        final FirebaseTranslator langTranslator =
                FirebaseNaturalLanguage.getInstance().getTranslator(options);

        //download language models if needed
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        langTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        v -> {
                            Log.d("translator", "downloaded lang  model");
                            //after making sure language models are available
                            //perform translation
                            downloadListener.downloadFinished();
                        })
                .addOnFailureListener(
                        e -> {

                        });
    }


    public void checkLanguageDownloaded() {
        FirebaseModelManager modelManager = FirebaseModelManager.getInstance();

        // Get translation models stored on the device.
        modelManager.getDownloadedModels(FirebaseTranslateRemoteModel.class)
                .addOnSuccessListener(models -> {
                    if(models.size()==2) {
                        downloadListener.currentModel(true);
                    } else {
                        downloadListener.currentModel(false);
                    }
                })
                .addOnFailureListener(e -> {

                });
    }



}
