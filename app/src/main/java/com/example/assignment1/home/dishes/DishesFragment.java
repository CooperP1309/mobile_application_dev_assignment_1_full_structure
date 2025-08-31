package com.example.assignment1.home.dishes;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.example.assignment1.R;
import com.example.assignment1.databinding.FragmentDishesBinding;
import com.example.assignment1.databinding.FragmentHomeBinding;
import com.example.assignment1.home.Model;

import java.util.ArrayList;
import java.util.List;

public class DishesFragment extends Fragment {

    private FragmentDishesBinding binding;
    private DishDatabaseManager databaseManager;

    public DishesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // final binding setup
        binding = FragmentDishesBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // injecting context of associated activity into database manager
        // requireContext(): throws except if no activity is attached to fragment - safe handling
        databaseManager = new DishDatabaseManager(requireContext());

        // initialize the data in recycler view
        retrieveDishes();

        // add dish Form button handling
        binding.buttonAdd.setOnClickListener(v->{
            addDishForm();
        });

        // show update Form button handling
        binding.buttonShowUpdate.setOnClickListener(v->{
            updateForm();
        });

        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    private void retrieveDishes() {
        // modifying form visibilities
        binding.createForm.setVisibility(View.GONE);
        binding.manageDishesForm.setVisibility(View.VISIBLE);
        binding.updateForm.setVisibility(View.GONE);

        // inflate recycler adapter
        NormalAdapter normalAdapter = new NormalAdapter(sortModelList());
        binding.recyclerSelect.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerSelect.setAdapter(normalAdapter);
        binding.recyclerSelect.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        // controlling visibility of remove button based on if dish/dishes are selected
        normalAdapter.setOnSelectionChangedListener(hasSelected -> {
            if (hasSelected) {
                binding.buttonRemoveSelected.setVisibility(View.VISIBLE);
            }
            else {
                binding.buttonRemoveSelected.setVisibility(View.GONE);
            }
        });

        binding.buttonRemoveSelected.setOnClickListener(v->{
            String selectedDishes = normalAdapter.getSelected().toString();
            deleteDishes(selectedDishes);
        });
    }

    private void addDishForm() {
        // modify visibilities
        binding.createForm.setVisibility(View.VISIBLE);
        binding.manageDishesForm.setVisibility(View.GONE);
        binding.updateForm.setVisibility(View.GONE);

        binding.buttonFinalAddDish.setOnClickListener(v->{

            // extract all variables from form
            int dishId = Integer.parseInt(binding.editAddDishID.getText().toString());
            String dishName = binding.editAddDishName.getText().toString();
            int selectedDishTypeId = binding.radioAddDishType.getCheckedRadioButtonId();
            String ingredients = binding.editAddIndgredients.getText().toString();
            String priceStr = binding.editAddPrice.getText().toString();
            double price;
            Bitmap image = null;

            // error check variables (empty and dupe ID)
            if (!checkIdUnique(dishId)) {       // case for bad ID input
                binding.textAddErrorResponse.setText("Dish ID already exists!");
                return;
            }
            if (dishName.isEmpty() || selectedDishTypeId == -1 || ingredients.isEmpty() || priceStr.isEmpty()) {
                binding.textAddErrorResponse.setText("One or more fields are empty");
                return;
            }
            try {
                price = Double.parseDouble(priceStr);
            } catch (Exception e) {
                binding.textAddErrorResponse.setText("Invalid price given");
                return;
            }

            // pushing data into DB
            RadioButton selectedButton = binding.radioAddDishType.findViewById(selectedDishTypeId);
            String dishType = selectedButton.getText().toString();

            Dish newDish = new Dish(dishId, dishName, dishType, ingredients, price, image);
            databaseManager.addRow(newDish);
            reloadFragment();
        });
    }

    private void updateForm() {
        // modifying form visibilities
        binding.createForm.setVisibility(View.GONE);
        binding.manageDishesForm.setVisibility(View.GONE);
        binding.updateForm.setVisibility(View.VISIBLE);

        // inflate recycler adapter
        UpdateAdapter updateAdapter = new UpdateAdapter(sortModelList());
        binding.recyclerUpdate.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerUpdate.setAdapter(updateAdapter);
        binding.recyclerUpdate.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        // detect when a recycler view is selected and set its data to form fields
        updateAdapter.setOnSelectionChangedListener(hasSelected -> {
            String selectedRow = updateAdapter.getSelected().toString();


        });


        binding.buttonSelectedUpdateDish.setOnClickListener(v->{

            // get selected rows string
            String selectedRow = updateAdapter.getSelected().toString();

            // convert to a dish object
            Dish dish = new Dish(selectedRow);

            // update the dish using form fields
            dish.setDishName("Pork Ribs");
            dish.setDishType("BBQ");
            dish.setPrice(43.00);
            dish.setIngredients("Pork");

            databaseManager.updateRow(dish);

            reloadFragment();
        });
    }

    private void deleteDishes(String selectedDishes) {

        // sort selected dishes into array of strings
        String[] lines = selectedDishes.split("\\R");

        // process the ID of each line into int array
        int[] idList = new int[lines.length];
        for (int j=0; j< lines.length; j++) {

            String id = "";
            String line = lines[j];
            Log.i("DishesFrag", "Processing line:\n" + line);

            // iterate until first comma is met (the end of our ID section)
            for (int i=0; i<line.indexOf(','); i++) {
                id = id + line.charAt(i);
            }

            // once at end of ID, store as int and move to next line
            Log.i("DishesFrag", "Extracted ID: " + id);
            idList[j] = Integer.parseInt(id);
        }

        // passing the extracted IDs into database delete method
        databaseManager.deleteRows(idList);

        reloadFragment();
    }

    private List<Model> sortModelList() {
        List<Model> modelList = new ArrayList<>();

        // retrieve the dish db rows
        String rows = databaseManager.retrieveRows();

        // store rows in a model array
        String[] lines = rows.split("\\R"); // any newline
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            // replace first '.' with ',' for Dish Conversion (else double val will break)
            line = line.replaceFirst("\\.", ",");

            modelList.add(new Model(line.trim(), false));
        }

        return modelList;
    }

    private void reloadFragment() {
        FragmentManager fm = requireActivity().getSupportFragmentManager();

        fm.beginTransaction()
                .replace(R.id.frameLayout, new DishesFragment())
                .addToBackStack(null) // optional
                .commit();
    }

    private boolean checkIdUnique(int givenId) {

        // get all rows from database
        String rows = databaseManager.retrieveRows();

        // sort rows into array
        String[] lines = rows.split("\\R");
        List<Integer> idList = new ArrayList<>();

        if (lines[0].isEmpty()) {                       // no records to compare with
            return true;
        }

        for (String line : lines) {

            // extract IDs from each row
            String id = "";
            for (int i=0; i<line.indexOf('.'); i++) {   // ID end delimited by '.' in db
                id = id + line.charAt(i);
            }

            // push ID into int list
            Log.i("Dish Frag", "Parsing id: " + id);
            idList.add(Integer.parseInt(id));
        }

        // compare to given ID
        for (int i: idList) {
            if (i == givenId) {
                Log.i("Dish Frag", "DishID NOT unique: " + i);
                return false;
            }
        }

        Log.i("Dish Frag", "DishID IS unique: " + givenId);

        return true;
    }

    // TODO DELETE THIS AFTER TESTING
    private String testDishProperties(Dish dish) {
        String properties = "";

        properties = properties + Integer.toString(dish.getDishID());
        properties = properties + dish.getDishName();
        properties = properties + dish.getDishType();
        properties = properties + dish.getIngredients();
        properties = properties + dish.getPrice().toString();
        properties = properties + null;

        return properties;
    }
}