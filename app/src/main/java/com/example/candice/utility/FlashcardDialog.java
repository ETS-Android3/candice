package com.example.candice.utility;

import static com.example.candice.FlashcardFragment.adapter;
import static com.example.candice.FlashcardFragment.flashList;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.candice.R;
import com.example.candice.database.FlashcardDatabase;
import com.example.candice.models.FlashcardModel;


//Custom Flashcard Dialog
public class FlashcardDialog extends Dialog {
    Context context;
    FlashcardDatabase flashcardDatabase;

    //Initializing the above declarations
    public FlashcardDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_flash_dialog);

        EditText questionEdit = findViewById(R.id.questionEdit);
        EditText answerEdit = findViewById(R.id.answerEdit);
        Button addFlashcard = findViewById(R.id.addFlash);

        flashcardDatabase = new FlashcardDatabase(getContext());
        addFlashcard.setOnClickListener(v -> {
            String question = questionEdit.getText().toString().trim();
            String answer = answerEdit.getText().toString().trim();

            //condition to check if question or answer is empty or not

            if (question.isEmpty() || answer.isEmpty()) {
                Toast.makeText(context, "Please fill in both sides of the flashcard", Toast.LENGTH_SHORT).show();
                return;
            } else {

                // add the data in the database
                // if data adds successfully then clear out the local list

                if (flashcardDatabase.insert(question, answer) > 1) {
                    flashList.clear();
                    Cursor cursor = flashcardDatabase.getData();
                    while (cursor.moveToNext()) {
                        flashList.add(0, new FlashcardModel(cursor.getString(0), cursor.getString(1), cursor.getString(2)));
                    }

                    // inform adapter to refresh list
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
                dismiss();
            }
        });
    }
}
