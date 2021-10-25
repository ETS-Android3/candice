package com.example.candice.adapters;

import static com.example.candice.FlashcardFragment.flashList;

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
import com.example.candice.database.FlashcardDatabase;
import com.wajahatkarim3.easyflipview.EasyFlipView;

//First of all credit goes to wajahatkarim3 with their Easyflipview available on Github:
//https://github.com/wajahatkarim3/EasyFlipView
//Their in depth documentation helped a lot when creating this Adapter


public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.ViewHolder> {
    // This manages the Recyclerview
    private ItemClickListener mClickListener;

    //Declaring the Database
    FlashcardDatabase flashcardDatabase;
    Context context;

    // The below is to initialize the FlashcardDatabase
    public FlashcardAdapter(Context context) {

        flashcardDatabase = new FlashcardDatabase(context);
        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // First of all this binds the view with the custom flashcard layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.flashcard_card_layout, parent, false);

        // Then return the above with a new flashcard
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        //This puts the user's question into the Flashcard Database
        holder.questionTextView.setText(flashList.get(position).getQuestion());
        //Then puts the answer into the database
        holder.answerTextView.setText(flashList.get(position).getAnswer());

        //Then the setOnLongClickListener will delete any flashcards made
        holder.itemView.setOnLongClickListener(v -> {

            //AlertDialog will pop up to confirm
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Delete")
                    .setMessage("Are you sure you want to delete this flashcard?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Firstly we grab the object and position in the database
                        // and if successful then its removed
                        if (flashcardDatabase.Delete(flashList.get(position).getID())) {
                            Toast.makeText(context, "Flashcard Deleted", Toast.LENGTH_SHORT).show();
                            flashList.remove(position);
                            notifyDataSetChanged();
                        } else {
                            //Catch for an error
                            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
            return false;
        });
    }

    //This gets the flashcard database list
    @Override
    public int getItemCount() {
        return flashList.size();
    }


    //The following is a ViewHolder for the flashcards themselves
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //As mentioned above, the EasyFlipView from Github declared along with the two TextViews
        EasyFlipView easyFlipView;
        TextView questionTextView;
        TextView answerTextView;

        public ViewHolder(@NonNull View itemView) {

            //Binding them all to the view
            super(itemView);
            easyFlipView = itemView.findViewById(R.id.easyFlip);
            questionTextView = itemView.findViewById(R.id.questionText);
            answerTextView = itemView.findViewById(R.id.answerText);
            itemView.setOnClickListener(this);
        }

        //When a user clicks on the card it should flip
        @Override
        public void onClick(View v) {
            easyFlipView.flipTheView();
            if (mClickListener != null) {
                mClickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }


    //Simple itemclicklistener - this was to prevent bugs in which a user clicks but it doesn't register the request
    //Still doesn't work well
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    //Interface that simply distinguishes between the RecyclerView or an ItemClick
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}