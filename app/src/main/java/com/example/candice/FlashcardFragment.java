package com.example.candice;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.candice.adapters.FlashcardAdapter;
import com.example.candice.database.FlashcardDatabase;
import com.example.candice.models.FlashcardModel;
import com.example.candice.utility.FlashcardDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class FlashcardFragment extends Fragment {
    public static List<FlashcardModel> flashList;
    public static FlashcardAdapter adapter;
    FlashcardDatabase databaseFlash;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_flash_card, container, false);

        databaseFlash = new FlashcardDatabase(getActivity());

        RecyclerView flashRec = view.findViewById(R.id.flashRV);
        flashRec.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FlashcardAdapter(getActivity());
        flashRec.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.flashFAB);
        flashList = new ArrayList<>();

        // Cursor object gets all the data from the database
        Cursor cursor = databaseFlash.getData();
        while (cursor.moveToNext()) {
            flashList.add(0, new FlashcardModel(cursor.getString(0), cursor.getString(1), cursor.getString(2)));
        }

        // on database load in from our adapter to refresh list
        adapter.notifyDataSetChanged();

        // floating action button click to add new flashcards
        fab.setOnClickListener(v -> {

            // custom Flash card dialog
            FlashcardDialog alertDialog = new FlashcardDialog(getContext());
            alertDialog.show();
        });
        return view;
    }
}