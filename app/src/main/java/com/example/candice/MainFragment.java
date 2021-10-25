package com.example.candice;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.content.Context.INPUT_METHOD_SERVICE;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.candice.database.TranslationDatabase;
import com.example.candice.models.TranslateViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class MainFragment extends Fragment {

    int encodedPosition;
    Editable editableText;

    //Text to speech
    TextToSpeech textToSpeech;
    String speakMode;

    TextView targetTextView;
    Button switchButton;
    ToggleButton sourceSyncButton;
    ToggleButton targetSyncButton;
    TextInputEditText srcTextView;
    Spinner sourceLangSelector;
    Spinner targetLangSelector;
    TextView downloadedModelsTextView;
    Button translateButton;
    ImageView speakerSource;

    //History
    boolean firstTime = true;
    int targetPosition, sourcePosition;
    TranslationDatabase translationDatabase;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(false);
        return inflater.inflate(R.layout.fragment_main, null);

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final TranslateViewModel viewModel = ViewModelProviders.of(this).get(TranslateViewModel.class);

        switchButton = view.findViewById(R.id.buttonSwitchLang);
        sourceSyncButton = view.findViewById(R.id.buttonSyncSource);
        targetSyncButton = view.findViewById(R.id.buttonSyncTarget);
        srcTextView = view.findViewById(R.id.source_text);
        sourceLangSelector = view.findViewById(R.id.sourceLangSelector);
        targetLangSelector = view.findViewById(R.id.targetLangSelector);
        targetTextView = view.findViewById(R.id.translated_text);
        downloadedModelsTextView = view.findViewById(R.id.downloadedModels);
        translateButton = view.findViewById(R.id.translateButton);
        speakerSource = view.findViewById(R.id.speaker_source);

        translationDatabase = new TranslationDatabase(getActivity());

        // Get available language list and set up source and target language spinners
        // with default selections.
        // Some of the following code (mostly related to the ArrayAdapters) were provided by Google.
        // More specifically their https://github.com/googlesamples/mlkit --> ML Kit samples (Apache License 2.0).
        // Outside of that the code for things like onItemSelected/onClickListener is code that is the best way to write it

        /** Reference:
         Google
         30th August 2021
         MLKit Translate
         Code Version: N/A
         Source Code: https://github.com/googlesamples/mlkit/tree/master/android/translate */
        final ArrayAdapter<TranslateViewModel.Language> adapter =
                new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, viewModel.getAvailableLanguages());
        sourceLangSelector.setAdapter(adapter);
        targetLangSelector.setAdapter(adapter);
        sourceLangSelector.setSelection(adapter.getPosition(new TranslateViewModel.Language("en")));
        targetLangSelector.setSelection(adapter.getPosition(new TranslateViewModel.Language("es")));

        //Spinner selector for source language
        sourceLangSelector.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        setProgressText(targetTextView);
                        viewModel.sourceLang.setValue(adapter.getItem(position));

                        //For the history, this sets the position of the Array ready to be used for later
                        sourcePosition = position;
                        MessageString("sourceLangSelector onItemSelected");
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        targetTextView.setText("");
                    }
                });

        //Spinner selector for target language
        targetLangSelector.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        setProgressText(targetTextView);
                        viewModel.targetLang.setValue(adapter.getItem(position));

                        //For the history, same as above in setting the position, this time for Target Language
                        targetPosition = position;
                        MessageString("targetLangSelector onItemSelected");
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        targetTextView.setText("");
                    }
                });

        //Switch button
        switchButton.setOnClickListener(
                v -> {
                    setProgressText(targetTextView);
                    int sourceLangPosition = sourceLangSelector.getSelectedItemPosition();
                    sourceLangSelector.setSelection(targetLangSelector.getSelectedItemPosition());
                    targetLangSelector.setSelection(sourceLangPosition);

                    //Grabbing position for the history
                    //Basically recognizing if there's a switch in languages so switch
                    //sourcePosition and targetPosition
                    int a = sourcePosition;
                    sourcePosition = targetPosition;
                    targetPosition = a;
                });

        // Set up toggle buttons to delete or download remote models locally.
        sourceSyncButton.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    TranslateViewModel.Language language =
                            adapter.getItem(sourceLangSelector.getSelectedItemPosition());
                    if (isChecked) {
                        viewModel.downloadLanguage(language);
                    } else {
                        viewModel.deleteLanguage(language);
                    }
                });

        targetSyncButton.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    TranslateViewModel.Language language =
                            adapter.getItem(targetLangSelector.getSelectedItemPosition());
                    if (isChecked) {
                        viewModel.downloadLanguage(language);
                    } else {
                        viewModel.deleteLanguage(language);
                    }
                });

        // Translate input text as it is typed
        srcTextView.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        setProgressText(targetTextView);
                        viewModel.sourceText.postValue(s.toString());

                        //For the history, a new if else statement
                        //Firstly this recognizes if the text box is empty, this means its a new translation so go ahead
                        //And save it to the history (once done)
                        //if it isn't a new translation, its simply updating the text instead.
                        if (s.toString().isEmpty()) {
                            MessageString("New Translation - 1st Time");
                            firstTime = true;
                        } else {
                            if (firstTime) {
                                MessageString("New Translation");
                                firstTime = false;
                            } else {
                                MessageString("Update Data");
                            }
                            MessageString(s.toString());
                            MessageString(targetTextView.getText().toString());
                        }
                    }
                });

        //Again mostly provided by Google (reference above), this just initiates the result or error method
        viewModel.translatedText.observe(
                getViewLifecycleOwner(),
                resultOrError -> {
                    if (TranslateViewModel.ResultOrError.error != null) {
                        srcTextView.setError(TranslateViewModel.ResultOrError.error.getLocalizedMessage());
                    } else {
                        targetTextView.setText(TranslateViewModel.ResultOrError.result);

                        //Get the text and send it as string for the history
                        MessageString(targetTextView.getText().toString());
                    }
                });

        // Update sync toggle button states based on downloaded models list.
        viewModel.availableModels.observe(
                getViewLifecycleOwner(),
                translateRemoteModels -> {
                    String output =
                            getContext().getString(R.string.downloaded_models_label, translateRemoteModels);
                    downloadedModelsTextView.setText(output);
                    sourceSyncButton.setChecked(
                            translateRemoteModels.contains(
                                    adapter.getItem(sourceLangSelector.getSelectedItemPosition()).getCode()));
                    targetSyncButton.setChecked(
                            translateRemoteModels.contains(
                                    adapter.getItem(targetLangSelector.getSelectedItemPosition()).getCode()));
                });

        translateButton.setOnClickListener(v -> {
            dismissKeyboard(view);

            MessageString(srcTextView.getText().toString() + " Source Text");
            MessageString(targetTextView.getText().toString() + " Target Text");
            MessageString(adapter.getItem(targetPosition).toString() + " Target Language");
            MessageString(adapter.getItem(sourcePosition).toString() + " Source Language");

            //Due to the way the history handles the data, we grab the text when the Translate button is hit
            //So the first two (srcTextView and targetTextView) are simple as you only need to get the text and
            //add them to a string. The second two were a little harder to figure out as you have the get the position
            //of the ArrayAdapter then add them as a string
            String sourceLanguage = adapter.getItem(sourcePosition).toString();
            String targetLanguage = adapter.getItem(targetPosition).toString();
            String targetText = targetTextView.getText().toString();
            String sourceText = srcTextView.getText().toString();

            //Once we have the data and declared them as strings, they are ready to be used.
            //This is then inserted into the DB
            long res = translationDatabase.insert(sourceLanguage, targetLanguage, sourceText, targetText);
            if (res > 0)
                MessageString("Done");
            else
                MessageString("Failed");
        });


        //The following onClickListener is for the "Share" feature which sends an intent
        //It'll grab the information in targetTextView and add an intent to Share
        //https://developer.android.com/training/sharing/send
        ImageView share = view.findViewById(R.id.share);
        share.setOnClickListener(v -> {
            String share_text = targetTextView.getText().toString();
            if (share_text.isEmpty()) {
                Toast.makeText(getActivity(), "Nothing to share", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent("android.intent.action.SEND");
                intent.setType("text/plain");
                intent.putExtra("android.intent.extra.SUBJECT", "Translated Text");
                intent.putExtra("android.intent.extra.TEXT", share_text);
                startActivity(Intent.createChooser(intent, "Share via"));
            }
        });

        //Clear button - this simply sets the text of both textView's to nothing, simplest way to do such a thing
        ImageView clear = view.findViewById(R.id.clear);
        clear.setOnClickListener(v -> {
            srcTextView.setText("");
            targetTextView.setText("");
            Toast.makeText(getActivity(), "Text Deleted.", Toast.LENGTH_SHORT).show();
        });

        //Copy button - this uses the clipboard framework to get the text (as long as it's not 0 which means nothing has been entered)
        //Then adds it to the clipboard manager ready to be used elsewhere
        //https://developer.android.com/guide/topics/text/copy-paste
        ImageView copy = view.findViewById(R.id.copy);
        copy.setOnClickListener(v -> {
            if (targetTextView.getText().toString().length() != 0) {
                ((ClipboardManager) requireActivity().getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Copy", targetTextView.getText().toString()));
                Toast.makeText(getActivity(), "Text Copied to clipboard", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(getActivity(), "Nothing to Copy", Toast.LENGTH_SHORT).show();
        });


        //The same as below, it basically looks at the clipboard manager and if there's a primary clip (i.e. something
        //that has already been copied previously) then it'll paste. The additional requirements are that we have to update the
        //encoded position and editable text so that the methods above when translating don't break and we recognize there's something
        //new to be translated.
        //https://developer.android.com/guide/topics/text/copy-paste
        ImageView paste = view.findViewById(R.id.paste_source);
        paste.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(CLIPBOARD_SERVICE);
            if (clipboard.hasPrimaryClip()) {
                try {
                    srcTextView.setText(clipboard.getPrimaryClip().getItemAt(0).getText().toString());
                    encodedPosition = srcTextView.length();
                    editableText = srcTextView.getText();
                    Selection.setSelection(editableText, encodedPosition);
                    Toast.makeText(getActivity(), "Text Pasted from Clipboard", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "" + e.toString(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Nothing to paste", Toast.LENGTH_SHORT).show();
            }
        });

        try {
            Bundle args = getArguments();
            if (args != null) {
                String str = args.getString("key");
                srcTextView.setText(str);

            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        //For the speaker button, initializes the ChangeTextToSpeech method
        //Text to speech documentation goes over things clearly:
        //https://developer.android.com/reference/android/speech/tts/TextToSpeech
        //https://android-developers.googleblog.com/2009/09/introduction-to-text-to-speech-in.html
        speakerSource.setOnClickListener(v -> {
            speakMode = "target";
            ChangeTextToSpeech(speakMode);
        });
    }


    //Simple method to just dismiss the keyboard once something has been done
    //Used above when hitting the "Translate" button
    public void dismissKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    //If the init is "target" then go ahead and run the textToSpeech method, otherwise show an error
    public void onInit(int i) {
        if (i == 0) {
            if (speakMode.equals("target")) {
                try {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                    textToSpeech.speak(targetTextView.getText().toString(), 0, null);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "There was an error with Text to Speech", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    //So if the init is initialized (from above), get the text and speak the target text
    //Otherwise there was an error and the text was actually empty.
    private void ChangeTextToSpeech(String origin) {
        if (origin.equals("target")) {
            if (!targetTextView.getText().toString().isEmpty()) {
                textToSpeech = new TextToSpeech(getActivity(), this::onInit);
                onInit(0);

            } else {
                Toast.makeText(getActivity(), "There is nothing to speak", Toast.LENGTH_SHORT).show();
            }
        }


    }

    //Another simple method, just sets the progress of the target text view to "Translating..." as it goes
    private void setProgressText(TextView tv) {
        tv.setText(getContext().getString(R.string.translate_progress));
    }

    //Simple String method
    private void MessageString(String message) {
        Log.e("Translation Message", message);
    }

    //Contains the activities lifecycle, so just resume where we left off
    @Override
    public void onResume() {
        super.onResume();
    }


    //Making sure to destroy any textToSpeech which may be bound otherwise errors and crashes galore.
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        super.onDestroy();
    }

}

