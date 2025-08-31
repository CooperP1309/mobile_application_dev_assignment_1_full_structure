package com.example.assignment1.home.dishes;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment1.R;
import com.example.assignment1.home.Model;

import java.util.ArrayList;
import java.util.List;

public class UpdateAdapter extends RecyclerView.Adapter<UpdateAdapter.MyViewHolder>{

    private List<Model> modelList;
    int selectedIndex;
    private NormalAdapter.OnSelectionChangedListener selectionChangedListener;

    // constructor injection of the model list
    public UpdateAdapter (List<Model> theModelList){
        modelList = theModelList;
    }

    public void setOnSelectionChangedListener(NormalAdapter.OnSelectionChangedListener listener) {
        this.selectionChangedListener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //infalte rowlayout and return a row in the list
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.rowlayout, parent, false);
        selectedIndex = RecyclerView.NO_POSITION;

        return new MyViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // onBindViewHolder is called for each row (viewholder) when scrolled into view
        // or when the recycler view sets this adapter

        // I.e. this method handles each row when recycler is setup
        // and when a view is scrolled into frame

        //assign data to row/holder at a particular position
        Model tempModel = modelList.get(position);
        holder.textView.setText(tempModel.getText());

        // drawing the UI from the current modelList state
        holder.itemView.setBackgroundColor(tempModel.isSelected() ? Color.GRAY : Color.WHITE);

        //on NORMAL click
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();

            if (pos == RecyclerView.NO_POSITION) {      // case for no selected position
                return;
            }

            // unselecting of all other positions previous position (if necessary)
            List<Integer> selectedPos = getSelectedPositions();
            for (int i: selectedPos) {
                modelList.get(i).setSelected(false);
                notifyItemChanged(i);     // This method must be called in OnBind...
            }

            // selecting of currently clicked position
            modelList.get(pos).setSelected(true);
            selectedIndex = pos;    // updating selected pos
            notifyItemChanged(pos); // commiting colour update

            if (selectionChangedListener != null) {
                selectionChangedListener.onSelectionChanged(hasSelected());
            }
        });
    }

    @Override
    public int getItemCount() { //count the number of rows
        //return data.length; // previous implementation
        return modelList.size();
    }

    public String getSelected() {

        String selectedString = "";

        for (int i=0; i< modelList.size(); i++) {       // for each model in the list
            if (modelList.get(i).isSelected()) {        // if it is selected
                // add it to results string
                selectedString = selectedString + modelList.get(i).getText() + "\n";
            }
        }

        return selectedString;
    }

    public boolean hasSelected() {
        for (int i=0; i< modelList.size(); i++) {
            if (modelList.get(i).isSelected()) {
                return true;
            }
        }

        return false;
    }

    public List<Integer> getSelectedPositions() {
        List<Integer> selectedPositions = new ArrayList<Integer>();

        for (int i=0; i< modelList.size(); i++) {       // for each model in the list
            if (modelList.get(i).isSelected()) {        // if it is selected
                selectedPositions.add(i);               // add to list
            }
        }

        return selectedPositions;
    }

    //viewHolder class to display a row
    public static class MyViewHolder extends RecyclerView.ViewHolder  {
        private TextView textView;

        public MyViewHolder(View view) { //constructor
            super(view);

            this.textView = view.findViewById(R.id.label);
        }
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(boolean hasSelected);
    }
}