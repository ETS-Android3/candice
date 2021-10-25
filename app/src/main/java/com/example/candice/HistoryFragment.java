package com.example.candice;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.candice.adapters.HistoryAdapter;
import com.example.candice.database.TranslationDatabase;
import com.example.candice.models.TranslationModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    //Declarations
    TranslationDatabase translationDatabase;
    public static List<TranslationModel> modelList;

    //On creating the view for the history fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflating the view for the history -- fragment_history.xml
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);

        //For the history
        modelList = new ArrayList<>();

        FloatingActionButton deleteMe;

        //RecyclerView from the history fragment
        RecyclerView historyRecycler = rootView.findViewById(R.id.dataRecycler);
        historyRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        HistoryAdapter adapter = new HistoryAdapter(getContext(), modelList);
        historyRecycler.setAdapter(adapter);
        translationDatabase = new TranslationDatabase(getActivity());

        //Get all the data from database
        //https://developer.android.com/reference/android/database/Cursor#getString(int)
        //"This cursor provides random read-write access to the result set returned by a database query"
        Cursor cursor = translationDatabase.getData();
        while (cursor.moveToNext()) {
            //We are then adding the data to our local list
            modelList.add(0, new TranslationModel(

                    //return ID
                    cursor.getString(0),

                    //return sourceLanguage
                    cursor.getString(1),

                    //return targetLanguage
                    cursor.getString(2),

                    //return sourceText
                    cursor.getString(3),

                    //return targetText
                    cursor.getString(4)
            ));
        }

        deleteMe = rootView.findViewById(R.id.deleteMe);
        deleteMe.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Delete")
                    .setMessage("Are you sure you want to delete all history?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        if (TranslationDatabase.deleteAll()) {
                            modelList.clear();
                            adapter.notifyDataSetChanged();
                            Toast.makeText(getContext(), "History Cleared", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();

        });

        //Finally inform the adapter to update as the data has changed
        adapter.notifyDataSetChanged();
        return rootView;

    }
}
