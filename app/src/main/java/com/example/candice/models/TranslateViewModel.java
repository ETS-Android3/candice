
package com.example.candice.models;

import android.app.Application;
import android.util.LruCache;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.candice.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

//Outside of any small changes this class was provided by Google.
//More specifically their https://github.com/googlesamples/mlkit --> MLKit Samples/Translate (Apache License 2.0).
//Code has been lambda'd based on Android Studio's recommendation, this helps with code bloat amongst other things:
//https://www.theserverside.com/blog/Coffee-Talk-Java-News-Stories-and-Opinions/Benefits-of-lambda-expressions-in-Java-makes-the-move-to-a-newer-JDK-worthwhile

/**
 * Reference:
 * Google
 * 30th August 2021
 * MLKit Translate
 * Code Version: N/A
 * Source Code
 * https://github.com/googlesamples/mlkit/tree/master/android/translate
 */
public class TranslateViewModel extends AndroidViewModel {

    // This specifies the number of translators instance we want to keep in our LRU cache.
    // Each instance of the translator is built with different options based on the source
    // language and the target language, and since we want to be able to manage the number of
    // translator instances to keep around, an LRU cache is an easy way to achieve this.
    private static final int NUM_TRANSLATORS = 3;

    private final RemoteModelManager modelManager;
    private final LruCache<TranslatorOptions, Translator> translators =
            new LruCache<TranslatorOptions, Translator>(NUM_TRANSLATORS) {
                @Override
                public Translator create(TranslatorOptions options) {
                    return Translation.getClient(options);
                }

                @Override
                public void entryRemoved(
                        boolean evicted, TranslatorOptions key, Translator oldValue, Translator newValue) {
                    oldValue.close();
                }
            };
    public MutableLiveData<Language> sourceLang = new MutableLiveData<>();
    public MutableLiveData<Language> targetLang = new MutableLiveData<>();
    public MutableLiveData<String> sourceText = new MutableLiveData<>();
    public MediatorLiveData<ResultOrError> translatedText = new MediatorLiveData<>();
    public MutableLiveData<List<String>> availableModels = new MutableLiveData<>();

    public TranslateViewModel(@NonNull Application application) {
        super(application);
        modelManager = RemoteModelManager.getInstance();

        // Create a translation result or error object.
        final OnCompleteListener<String> processTranslation =
                task -> {
                    if (task.isSuccessful()) {
                        translatedText.setValue(new ResultOrError(task.getResult(), null));
                    } else {
                        translatedText.setValue(new ResultOrError(null, task.getException()));
                    }
                    // Update the list of downloaded models as more may have been
                    // automatically downloaded due to requested translation.
                    fetchDownloadedModels();
                };

        // Start translation if any of the following change: input text, source lang, target lang.
        translatedText.addSource(
                sourceText,
                s -> translate().addOnCompleteListener(processTranslation));

        Observer<Language> languageObserver =
                language -> translate().addOnCompleteListener(processTranslation);
        translatedText.addSource(sourceLang, languageObserver);
        translatedText.addSource(targetLang, languageObserver);

        // Update the list of downloaded models.
        fetchDownloadedModels();
    }

    // Gets a list of all available translation languages.
    public List<Language> getAvailableLanguages() {
        List<Language> languages = new ArrayList<>();
        List<String> languageIds = TranslateLanguage.getAllLanguages();
        for (String languageId : languageIds) {
            languages.add(new Language(TranslateLanguage.fromLanguageTag(languageId)));
        }
        return languages;
    }

    private TranslateRemoteModel getModel(String languageCode) {
        return new TranslateRemoteModel.Builder(languageCode).build();
    }

    // Updates the list of downloaded models available for local translation.
    private void fetchDownloadedModels() {
        modelManager
                .getDownloadedModels(TranslateRemoteModel.class)
                .addOnSuccessListener(
                        remoteModels -> {
                            List<String> modelCodes = new ArrayList<>(remoteModels.size());
                            for (TranslateRemoteModel model : remoteModels) {
                                modelCodes.add(model.getLanguage());
                            }
                            Collections.sort(modelCodes);
                            availableModels.setValue(modelCodes);
                        });
    }

    // Starts downloading a remote model for local translation.
    public void downloadLanguage(Language language) {
        TranslateRemoteModel model = getModel(TranslateLanguage.fromLanguageTag(language.getCode()));
        modelManager
                .download(model, new DownloadConditions.Builder().build())
                .addOnCompleteListener(
                        task -> fetchDownloadedModels());
    }

    // Deletes a locally stored translation model.
    public void deleteLanguage(Language language) {
        TranslateRemoteModel model = getModel(TranslateLanguage.fromLanguageTag(language.getCode()));
        modelManager
                .deleteDownloadedModel(model)
                .addOnCompleteListener(
                        task -> fetchDownloadedModels());
    }

    public Task<String> translate() {
        final String text = sourceText.getValue();
        final Language source = sourceLang.getValue();
        final Language target = targetLang.getValue();
        if (source == null || target == null || text == null || text.isEmpty()) {
            return Tasks.forResult("");
        }
        String sourceLangCode = TranslateLanguage.fromLanguageTag(source.getCode());
        String targetLangCode = TranslateLanguage.fromLanguageTag(target.getCode());
        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(sourceLangCode)
                        .setTargetLanguage(targetLangCode)
                        .build();
        return translators
                .get(options)
                .downloadModelIfNeeded()
                .continueWithTask(
                        task -> {
                            if (task.isSuccessful()) {
                                return translators.get(options).translate(text);
                            } else {
                                Exception e = task.getException();
                                if (e == null) {
                                    e = new Exception(getApplication().getString(R.string.unknown_error));
                                }
                                return Tasks.forException(e);
                            }
                        });
    }

    //Holds the data for either a result or error
    public static class ResultOrError {
        public static String result;
        public static Exception error;

        ResultOrError(@Nullable String result, @Nullable Exception error) {
            ResultOrError.result = result;
            ResultOrError.error = error;
        }
    }

    public static class Language implements Comparable<Language> {

        public final String code;

        public Language(String code) {
            this.code = code;
        }

        public String getDisplayName() {
            return new Locale(code).getDisplayName();
        }

        public String getCode() {
            return code;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }

            if (!(o instanceof Language)) {
                return false;
            }

            Language otherLang = (Language) o;
            return otherLang.code.equals(code);
        }

        @NonNull
        @Override
        public String toString() {
            return code + " - " + getDisplayName();
        }

        @Override
        public int hashCode() {
            return code.hashCode();
        }

        @Override
        public int compareTo(@NonNull Language o) {
            return this.getDisplayName().compareTo(o.getDisplayName());
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Each new instance of a translator needs to be closed appropriately. Here we utilize the
        // ViewModel's onCleared() to clear our LruCache and close each Translator instance when
        // this ViewModel is no longer used and destroyed.
        translators.evictAll();
    }
}
