package com.example.assignment1.home.dishes;

import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment1.R;
import com.example.assignment1.home.Model;

import java.util.HashSet;
import java.util.List;

public class DishesAdapter extends RecyclerView.Adapter<DishesAdapter.MyViewHolder>{

    private List<DishModel> modelList;
    private OnSelectionChangedListener selectionChangedListener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionChangedListener = listener;
    }

    // constructor injection of the model list
    public DishesAdapter(List<DishModel> theModelList){
        modelList = theModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //infalte rowlayout and return a row in the list
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.dishrowlayout, parent, false);
        return new MyViewHolder(rowItem);
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //assign data to row/holder at a particular position
        DishModel tempModel = modelList.get(position);

        if (tempModel.isSelected()) {       // needed for automatic selecting (in update forms)
            holder.itemView.setBackgroundColor(Color.GRAY);
        }

        if (!tempModel.isSelectable()) {    // non-selectable model is a Label/group title row
            Log.i("DishAdpt","Setting as non-selectable: " + tempModel.getText());
            holder.textView.setTypeface(null, Typeface.BOLD);
            holder.textView.setTextSize(25);
            holder.imageView.setVisibility(View.GONE);

            // setting larger layout margins
            ViewGroup.MarginLayoutParams layoutParams =
                    (ViewGroup.MarginLayoutParams) holder.textView.getLayoutParams();
            layoutParams.setMargins(0, 30, 0, 30);
        }
        else {
            holder.textView.setTypeface(null, Typeface.NORMAL);
            holder.textView.setTextSize(14);

            // only assign image if model has an image
            if (tempModel.hasImage()) {
                Log.i("DishAdapt", "Found image in Dish object: " +
                        tempModel.getText());
                holder.imageView.setVisibility(View.VISIBLE);
                holder.imageView.setImageURI(tempModel.getImage());
            }
            else {
                holder.imageView.setVisibility(View.GONE);
                Log.i("DishAdapt", "No image found in Dish object: " +
                        tempModel.getText());
            }
        }

        holder.textView.setText(tempModel.getText());

        // on LONG click
        holder.itemView.setOnClickListener(v1 -> {

            //if the item was selected, it becomes unselected, change background from gray to white
            if (modelList.get(position).isSelected()) {
                holder.itemView.setBackgroundColor(Color.WHITE);
                modelList.get(position).setSelected(false); //item become unslected
            }
            else if (modelList.get(position).isSelectable() && !modelList.get(position).isSelected()){ //otherwise, the item was not selected, after the click, it is selected, change background to gray
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

    // Call this from outside the adapter to select an item
    public void setSelectedPosition(int position) {
        int oldPosition = selectedPosition;
        selectedPosition = position;

        // Refresh old and new so UI updates correctly
        notifyItemChanged(oldPosition);
        notifyItemChanged(selectedPosition);
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
                Log.i("DishAdapt","Found model:" + i + " to be selected");
                return true;
            }
        }

        return false;
    }

    public void setSelectedFromIdArray(int[] idList) {

        // getIdFromModelText() contains another for-loop. Thus, to avoid
        // a triple nested for-loop idList will be pushed into a hashSet
        HashSet<Integer> idHash = new HashSet<Integer>();
        for (int i=0; i<idList.length;i++) {
            idHash.add(idList[i]);
        }

        // for each model in the adapter, retrieve the id
        for (DishModel tempModel: modelList) {
            int modelId = getIdFromModel(tempModel);

            // set model as selected if its ID is in the HashSet
            if (idHash.contains(modelId)) {
                Log.i("DishesAdapter","Setting this model as true: " + tempModel.getText());
                tempModel.setSelected(true);
            }
        }

        // refresh adapter to show changes
        notifyDataSetChanged();
        Log.i("DishesAdapter", "Changes notified");
    }

    public int getIdFromModel(DishModel model) {

        if (!model.isSelectable()) {        // do not run on label models (E.g. Drinks, Mains..)
            return -1;
        }

        String modelText = model.getText();
        //Log.i("DishesAdapter", "Getting the ID from model text: " + modelText);
        String idStr = "";

        // id is delimited by the first occurence of a comma
        for (int i =0; i<modelText.indexOf(',');i++){
            idStr+=modelText.charAt(i);
        }
        idStr = idStr.trim();   // remove and whitespaces

        return Integer.parseInt(idStr);
    }

    //viewHolder class to display a row
    public static class MyViewHolder extends RecyclerView.ViewHolder  {
        private TextView textView;
        private ImageView imageView;

        public MyViewHolder(View view) { //constructor
            super(view);

            this.textView = view.findViewById(R.id.label);
            this.imageView = view.findViewById(R.id.imageViewDish);
        }
    }

    // interface allows fragments to see if a change has been made
    // specifically used for changing visibility of a remove button
    public interface OnSelectionChangedListener {
        void onSelectionChanged(boolean hasSelected);
    }
}

