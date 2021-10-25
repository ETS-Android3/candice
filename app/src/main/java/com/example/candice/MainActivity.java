package com.example.candice;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.candice.utility.FragmentChangeListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements FragmentChangeListener {

    BottomNavigationView bottomNavigationView;
    boolean areYouHome;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Candice);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        //speech stuff

        final MainFragment mainFragment = new MainFragment();
        final VoiceFragment voiceFragment = new VoiceFragment();
        final ImageFragment imageFragment = new ImageFragment();
        final HistoryFragment historyFragment = new HistoryFragment();
        final FlashcardFragment flashcardFragment = new FlashcardFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, mainFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        areYouHome = true;

        bottomNavigationView = findViewById(R.id.bottomNavigationMenu);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.homeBottomNavButton:
                    FragmentTransaction transaction1 = getSupportFragmentManager().beginTransaction();
                    transaction1.replace(R.id.frameLayout, mainFragment);
                    transaction1.addToBackStack(null);
                    transaction1.commit();
                    areYouHome = true;

                    return true;

                //The case here is if the user has selected the @id voiceBottomNavButton then they are looking to move fragments
                //and so we begin the FragmentTransaction to switch to the voiceFragment which was instantiated from above
                //They're also no longer home so this boolean is now false and if they hit the android back button, they won't exit the application
                //but instead head back to the previous fragment.
                case R.id.voiceBottomNavButton:
                    FragmentTransaction transactionVoice = getSupportFragmentManager().beginTransaction();
                    transactionVoice.replace(R.id.frameLayout, voiceFragment);
                    transactionVoice.addToBackStack(null);
                    transactionVoice.commit();
                    areYouHome = false;

                    return true;

                //For the image scanning page aka translations with camera
                //Because of the way I'm accessing the Fragments these don't need any special parameters
                //And so the backstack of it is null - for later use I could add things like String names etc.
                case R.id.imageScanBottomNavButton:

                    FragmentTransaction translationSettings = getSupportFragmentManager().beginTransaction();
                    translationSettings.replace(R.id.frameLayout, imageFragment);
                    translationSettings.addToBackStack(null);
                    translationSettings.commit();
                    areYouHome = false;

                    return true;

                //For the history page
                case R.id.historyBottomNavButton:

                    FragmentTransaction translationHistory = getSupportFragmentManager().beginTransaction();
                    translationHistory.replace(R.id.frameLayout, historyFragment);
                    translationHistory.addToBackStack(null);
                    translationHistory.commit();
                    areYouHome = false;

                    return true;

                //For the flashcard fragment
                //Right now pointing to imageFragment as a test
                case R.id.gameBottomNavButton:

                    translationSettings = getSupportFragmentManager().beginTransaction();
                    translationSettings.replace(R.id.frameLayout, flashcardFragment);
                    translationSettings.addToBackStack(null);
                    translationSettings.commit();
                    areYouHome = false;

                    return true;
            }

            return false;

        });

    }

    //This method actually replaces the fragments and replaces all existing fragments
    //This is also vital to the FragmentChangeListener (which this main activity implements)
    //And therefore must be called
    @Override
    public void replaceFragment(Fragment fragment) {

        FragmentTransaction transactionTheme = getSupportFragmentManager().beginTransaction();
        transactionTheme.replace(R.id.frameLayout, fragment);
        bottomNavigationView.getMenu().findItem(R.id.homeBottomNavButton).setChecked(true);
        transactionTheme.addToBackStack(null);
        transactionTheme.commit();
        areYouHome = true;
    }


    //Method for when the goBackArrowButton button is pressed (the main one on Android at the bottom of every phone)
    //When pressed and you're on the home screen you're finished with the application so close it
    //When pressed and it's not, go back to the mainFragment
    @Override
    public void onBackPressed() {
        final MainFragment mainFragment = new MainFragment();

        //if else statement is used for this, if the user isHome then finished
        //else return to the mainFragment
        //(!!) Look into how to get them to the previous fragment as opposed to the MainFragment
        if (areYouHome) {
            finish();

        } else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frameLayout, mainFragment);
            transaction.addToBackStack(null);
            transaction.commit();
            bottomNavigationView.getMenu().findItem(R.id.homeBottomNavButton).setChecked(true);

            areYouHome = true;
        }
    }
}
