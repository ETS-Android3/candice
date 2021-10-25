package com.example.candice;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.candice.models.TranslateViewModel;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Locale;

//Much of the information of this fragment comes from Main Fragment
//The main difference is the Voice Fragment doesn't add to the history
//As the main intention of the Fragment is for quick conversations and would overwhelm the database
public class VoiceFragment extends Fragment {


    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;

    int encodedPosition;
    Editable editableText;

    //Text to speech
    TextToSpeech textToSpeech;
    String speakMode;

    TextInputEditText source_text_voice;
    TextView targetTextView;
    Button switchButton;
    ToggleButton sourceSyncButton;
    ToggleButton targetSyncButton;
    Spinner sourceLangSelector;
    Spinner targetLangSelector;
    TextView downloadedModelsTextView;
    Button speakButton;
    ImageView speakerSource;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(false);
        return inflater.inflate(R.layout.fragment_voice, null);

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Making sure to double check there is permission for recording the voice
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkPermission();
        }


        final TranslateViewModel viewModel = ViewModelProviders.of(this).get(TranslateViewModel.class);

        switchButton = view.findViewById(R.id.buttonSwitchLang_voice);
        sourceSyncButton = view.findViewById(R.id.buttonSyncSource_voice);
        targetSyncButton = view.findViewById(R.id.buttonSyncTarget_voice);
        source_text_voice = view.findViewById(R.id.source_text_voice);
        sourceLangSelector = view.findViewById(R.id.sourceLangSelector_voice);
        targetLangSelector = view.findViewById(R.id.targetLangSelector_voice);
        targetTextView = view.findViewById(R.id.translated_text_voice);
        downloadedModelsTextView = view.findViewById(R.id.downloadedModels_voice);
        speakButton = view.findViewById(R.id.speakButton);
        speakerSource = view.findViewById(R.id.speaker_source_voice);


        // Get available language list and set up source and target language spinners
        // with default selections.

        //Once again the following was provided by Google. Anything with the adapter or viewmodel is from Google
        //Any code surrounding it will be altered or implemented by me.
        /** Reference:
         Google
         30th August 2021
         MLKit Translate
         Code Version: N/A
         Source Code
         https://github.com/googlesamples/mlkit/tree/master/android/translate */

        final ArrayAdapter<TranslateViewModel.Language> adapter =
                new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, viewModel.getAvailableLanguages());
        sourceLangSelector.setAdapter(adapter);
        targetLangSelector.setAdapter(adapter);
        sourceLangSelector.setSelection(adapter.getPosition(new TranslateViewModel.Language("en")));
        targetLangSelector.setSelection(adapter.getPosition(new TranslateViewModel.Language("es")));
        sourceLangSelector.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        setProgressText(targetTextView);
                        viewModel.sourceLang.setValue(adapter.getItem(position));

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        targetTextView.setText("");
                    }
                });


        targetLangSelector.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        setProgressText(targetTextView);
                        viewModel.targetLang.setValue(adapter.getItem(position));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        targetTextView.setText("");
                    }
                });

        switchButton.setOnClickListener(
                v -> {
                    setProgressText(targetTextView);
                    int sourceLangPosition = sourceLangSelector.getSelectedItemPosition();
                    sourceLangSelector.setSelection(targetLangSelector.getSelectedItemPosition());
                    targetLangSelector.setSelection(sourceLangPosition);
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
        source_text_voice.addTextChangedListener(
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
                    }
                });

        viewModel.translatedText.observe(
                getViewLifecycleOwner(),
                resultOrError -> {
                    if (TranslateViewModel.ResultOrError.error != null) {
                        source_text_voice.setError(TranslateViewModel.ResultOrError.error.getLocalizedMessage());
                    } else {
                        targetTextView.setText(TranslateViewModel.ResultOrError.result);
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

        ImageView clear = view.findViewById(R.id.clear);
        clear.setOnClickListener(v -> {
            source_text_voice.setText("");
            targetTextView.setText("");
            Toast.makeText(getActivity(), "Text Deleted.", Toast.LENGTH_SHORT).show();
        });


        ImageView copy = view.findViewById(R.id.copy);
        copy.setOnClickListener(v -> {
            if (targetTextView.getText().toString().length() != 0) {
                ((ClipboardManager) requireActivity().getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Copy", targetTextView.getText().toString()));
                Toast.makeText(getActivity(), "Text Copied to clipboard", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(getActivity(), "Nothing to Copy", Toast.LENGTH_SHORT).show();
        });


        ImageView paste = view.findViewById(R.id.paste_source);
        paste.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(CLIPBOARD_SERVICE);
            if (clipboard.hasPrimaryClip()) {
                try {
                    source_text_voice.setText(clipboard.getPrimaryClip().getItemAt(0).getText().toString());
                    encodedPosition = source_text_voice.length();
                    editableText = source_text_voice.getText();
                    Selection.setSelection(editableText, encodedPosition);
                    Toast.makeText(getActivity(), "Text Pasted from Clipboard", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "" + e.toString(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Nothing to paste", Toast.LENGTH_SHORT).show();
            }
        });

        speakerSource.setOnClickListener(v -> {
            speakMode = "target";
            ChangeTextToSpeech(speakMode);
        });


        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getActivity());
        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                source_text_voice.setText("");
                source_text_voice.setHint("Listening...");
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                source_text_voice.setText(data.get(0));
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        speakButton.setOnTouchListener((View v, MotionEvent motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                speechRecognizer.stopListening();
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                speechRecognizer.startListening(speechRecognizerIntent);
            }
            return false;
        });

    }

    public void onInit(int i) {
        if (i == 0) {
            if (speakMode.equals("target")) {
                try {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                    textToSpeech.speak(targetTextView.getText().toString(), 0, null);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "The text to speech feature is not available in this language", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

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

    private void setProgressText(TextView tv) {
        tv.setText(getContext().getString(R.string.translate_progress));
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);
        }
    }

    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        speechRecognizer.destroy();
        super.onDestroy();
    }

}

