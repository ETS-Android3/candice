package com.example.candice.adapters;

import static com.example.candice.HistoryFragment.modelList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.candice.R;
import com.example.candice.database.TranslationDatabase;
import com.example.candice.models.TranslationModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    //Get the correct context
    //https://developer.android.com/reference/android/content/Context
    Context context;

    //Initialize the TranslationDatabase
    TranslationDatabase translationDatabase;

    //Initial setup for the adapter, setting the context to this
    //Along with having translationDatabase pass with the parameters of the context
    public HistoryAdapter(Context context, List<TranslationModel> modelList) {
        this.context = context;
        translationDatabase = new TranslationDatabase(context);
    }

    //Make sure the translated items layout is bound with the recyclerview
    //Then return the translated items
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_row, parent, false);
        return new ViewHolder(view);

    }

    //Below we bind the data to the TranslationModel
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        TranslationModel model = modelList.get(position);
        holder.targetText.setText(model.getTargetText());
        holder.targetLanguage.setText(model.getTargetLanguage());
        holder.sourceText.setText(model.getSourceText());
        holder.sourceLanguage.setText(model.getSourceLanguage());

        //Below manages a longclick for each individual history item
        holder.itemView.setOnLongClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Delete Item")
                    .setMessage("Are you sure to delete this item?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        if (translationDatabase.deleteItem(model.getID())) {
                            Toast.makeText(context, "Deleted Item", Toast.LENGTH_SHORT).show();
                            modelList.remove(position);
                            notifyDataSetChanged();
                        } else {
                            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
            return false;
        });

    }

    //Getting the model list size
    @Override
    public int getItemCount() {
        return modelList.size();
    }

    //The viewholder for the textviews within the HistoryFragment, setting up their ID's to be used within the RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView sourceLanguage, targetLanguage, sourceText, targetText;
        FloatingActionButton deleteMe;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sourceLanguage = itemView.findViewById(R.id.source_lang);
            targetLanguage = itemView.findViewById(R.id.target_lang);
            sourceText = itemView.findViewById(R.id.source_text);
            targetText = itemView.findViewById(R.id.target_text);
            deleteMe = itemView.findViewById(R.id.deleteMe);
        }
    }
}
