package com.example.assignment1.home.dishes;

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

public class NormalAdapter extends RecyclerView.Adapter<NormalAdapter.MyViewHolder>{

    private List<Model> modelList;
    private OnSelectionChangedListener selectionChangedListener;

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionChangedListener = listener;
    }

    // constructor injection of the model list
    public NormalAdapter (List<Model> theModelList){
        modelList = theModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //infalte rowlayout and return a row in the list
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.rowlayout, parent, false);
        return new MyViewHolder(rowItem);
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //assign data to row/holder at a particular position
        Model tempModel = modelList.get(position);
        holder.textView.setText(tempModel.getText());

        //on NORMAL click
       // holder.itemView.setOnClickListener(v -> {
       //     Toast.makeText(v.getContext(), "position : " + position + " text : " + tempModel.getText(), Toast.LENGTH_SHORT).show();
       // });

        // on LONG click
        holder.itemView.setOnClickListener(v1 -> {

            //if the item was selected, it becomes unselected, change background from gray to white
            if (modelList.get(position).isSelected()) {
                holder.itemView.setBackgroundColor(Color.WHITE);
                modelList.get(position).setSelected(false); //item become unslected
            }
            else { //otherwise, the item was not selected, after the click, it is selected, change background to gray
                holder.itemView.setBackgroundColor(Color.GRAY);
                modelList.get(position).setSelected(true); //mark the item selected
            }

            // this public interface holds a single bool that tells fragments
            // if the adapted has at least one row selected
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

    //viewHolder class to display a row
    public static class MyViewHolder extends RecyclerView.ViewHolder  {
        private TextView textView;

        public MyViewHolder(View view) { //constructor
            super(view);

            this.textView = view.findViewById(R.id.label);
        }
    }

    // interface allows fragments to see if a change has been made
    // specifically used for changing visibility of a remove button
    public interface OnSelectionChangedListener {
        void onSelectionChanged(boolean hasSelected);
    }
}

