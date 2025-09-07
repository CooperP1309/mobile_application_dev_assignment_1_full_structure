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


/*public class DishesAdapter extends RecyclerView.Adapter<DishesAdapter.MyViewHolder>{

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
        holder.textView.setText(tempModel.getText());

        if (!tempModel.isSelectable()) {    // non-selectable model is a Label/group title row
            holder.textView.setTypeface(null, Typeface.BOLD);
            holder.textView.setTextSize(25);

            // setting larger layout margins
            ViewGroup.MarginLayoutParams layoutParams =
                    (ViewGroup.MarginLayoutParams) holder.textView.getLayoutParams();
            layoutParams.setMargins(0, 30, 0, 30);
        }

        // only assign image if model has an image
        if (tempModel.hasImage()) {
            Log.i("DishAdapt", "Found image in Dish object");
            holder.imageView.setImageURI(tempModel.getImage());
        }
        else {
            holder.imageView.setVisibility(View.GONE);
            Log.i("DishAdapt", "No image found in Dish object");
        }

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
}*/