package com.example.assignment1.home.dishes;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DishesFragment extends Fragment {

    private FragmentDishesBinding binding;
    private DishDatabaseManager databaseManager;
    private Uri createdUri;

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

        handleCloseButtons();

        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    private void handleCloseButtons() {
        binding.buttonExitCreate.setOnClickListener(v->{
            retrieveDishes();
        });
        binding.buttonExitUpdate.setOnClickListener(v->{
            retrieveDishes();
        });
    }

    private void retrieveDishes() {
        // modifying form visibilities
        binding.createForm.setVisibility(View.GONE);
        binding.manageDishesForm.setVisibility(View.VISIBLE);
        binding.updateForm.setVisibility(View.GONE);

        // inflate recycler adapter
        DishesAdapter normalAdapter = new DishesAdapter(sortDishModelList());
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

        // on create set global uri to null as to not accidentally push an old selection
        // into a new record
        createdUri = null;

        // image browsing listener
        binding.buttonImageAdd.setOnClickListener(v->{
            /*Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 1000); // code: 1000 = request pick image*/

            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(i, 1000);
        });

        binding.buttonFinalAddDish.setOnClickListener(v->{

            // extract all variables from form
            String dishIdStr = binding.editAddDishID.getText().toString();
            //int dishId = Integer.parseInt(binding.editAddDishID.getText().toString());
            String dishName = binding.editAddDishName.getText().toString();
            int selectedDishTypeId = binding.radioAddDishType.getCheckedRadioButtonId();
            String ingredients = binding.editAddIndgredients.getText().toString();
            String priceStr = binding.editAddPrice.getText().toString();
            double price;
            String imageUri;
            if (createdUri == null) {
                imageUri = "";
            }
            else {
                imageUri = createdUri.toString();
            }

            // error check variables (empty and dupe ID)
            if (dishIdStr.isEmpty() || dishName.isEmpty() || selectedDishTypeId == -1
                    || ingredients.isEmpty() || priceStr.isEmpty()) {
                binding.textAddErrorResponse.setText("One or more fields are empty");
                return;
            }
            int dishId = Integer.parseInt(dishIdStr);
            if (!checkIdUnique(dishId)) {       // case for bad ID input
                binding.textAddErrorResponse.setText("Dish ID already exists!");
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

            // dividing ingredients with a non-comma character
            // commas are used by SQL to divide columns/fields
            ingredients = ingredients.replace(',', '|');

            Dish newDish = new Dish(dishId, dishName, dishType, ingredients, price, imageUri);
            databaseManager.addRow(newDish);
            reloadFragment();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*
        if (resultCode == RESULT_OK && requestCode == 1000) {   // case where user picked an image successfully
            Uri uri = data.getData();
            Bitmap bitmap = loadFromUri(uri);
            binding.imageView.setImageBitmap(bitmap);
            createdUri = uri;   // assign global variable for access when pushing form data in db
            //binding.textAddErrorResponse.setText(uri.toString()); // test the val of a uri here
        }*/
        if (resultCode == RESULT_OK && requestCode == 1000 && data != null) {
            Uri uri = data.getData();
            requireContext().getContentResolver().takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
            createdUri = uri;
            binding.imageView.setImageURI(uri);
        }
    }

    private Bitmap loadFromUri(Uri uri) {
        Bitmap bitmap = null;

        try{
            ImageDecoder.Source source = ImageDecoder.createSource(requireContext().getContentResolver(), uri);
            bitmap = ImageDecoder.decodeBitmap(source);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private void updateForm() {
        // modifying form visibilities
        binding.createForm.setVisibility(View.GONE);
        binding.manageDishesForm.setVisibility(View.GONE);
        binding.updateForm.setVisibility(View.VISIBLE);

        // inflate recycler adapter
        UpdateDishesAdapter updateAdapter = new UpdateDishesAdapter(sortDishModelList());
        binding.recyclerUpdate.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerUpdate.setAdapter(updateAdapter);
        binding.recyclerUpdate.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        // on create set global uri to null as to not accidentally push an old selection
        // into a new record
        createdUri = null;

        // image browsing listener
        binding.buttonImageUpdate.setOnClickListener(v->{
            // clear the current image view
            binding.imageUpdateView.setImageResource(0);

            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(i, 1000);
        });

        // when a recycler view row is selected...
        updateAdapter.setOnSelectionChangedListener(hasSelected -> {

            if (hasSelected) {
                // push that row into a dish object
                String selectedRow = updateAdapter.getSelected();

                Log.i("DishFrag", "Dish selected in update form: " + selectedRow);
                // dish constructor expects an extra field for the imageUri

                // get the imageUri for this record
                int id = getIdFromDbLine(selectedRow);
                String imageUriStr = databaseManager.retrieveImageUri(id);
                if (imageUriStr == null) {      // add an imageless model if no uri was returned from db
                    selectedRow+= ", ";
                }
                else {
                    selectedRow += ", " + imageUriStr;
                }

                Dish selectedDish = new Dish(selectedRow);

                // set the field of the update form to reflect the properties of the dish object
                setUpdateFormFields(selectedDish);
            }
        });

        binding.buttonSelectedUpdateDish.setOnClickListener(v->{

            // get selected row string
            String selectedRow = updateAdapter.getSelected().toString();

            // extract all variables from form
            Dish selectedDish = new Dish(selectedRow);      // lazy way to get dishID
            String dishName = binding.editUpdateDishName.getText().toString();
            int selectedDishTypeId = binding.radioUpdateDishType.getCheckedRadioButtonId();
            String ingredients = binding.editUpdateIndgredients.getText().toString();
            String priceStr = binding.editUpdatePrice.getText().toString();
            double price;
            String imageUri;
            if (createdUri == null) {
                imageUri = "";
            }
            else {
                imageUri = createdUri.toString();
            }

            // error check variables
            if (dishName.isEmpty() || selectedDishTypeId == -1 || ingredients.isEmpty() || priceStr.isEmpty()) {
                binding.textAddErrorResponse.setText("One or more fields are empty");
                return;
            }
            try {
                price = Double.parseDouble(priceStr);
            } catch (Exception e) {
                binding.textUpdateErrorResponse.setText("Invalid price given");
                return;
            }

            // pushing data into DB
            RadioButton selectedButton = binding.radioUpdateDishType.findViewById(selectedDishTypeId);
            String dishType = selectedButton.getText().toString();

            // dividing ingredients with a non-comma character
            // commas are used by SQL to divide columns/fields
            ingredients = ingredients.replace(',', '|');

            Dish newDish = new Dish(selectedDish.getDishID(), dishName, dishType, ingredients, price, imageUri);
            databaseManager.updateRow(newDish);
            reloadFragment();
        });

        binding.buttonDeleteUpdate.setOnClickListener(v->{
            // get the selected record
            String selectedRecord = updateAdapter.getSelected();

            if (selectedRecord.isEmpty()) {
                binding.textUpdateErrorResponse.setText("No dish is selected");
            }
            else {
                int id = getIdFromDbLine(selectedRecord);
                int [] idList = {id};
                databaseManager.deleteRows(idList);
                reloadFragment();
            }
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
        String rows = databaseManager.retrieveRowsWithoutImage();

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

    private List<DishModel> sortDishModelList() {
        List<DishModel> modelList = new ArrayList<>();

        // retrieve the dish db rows
        String rows = databaseManager.retrieveRowsWithoutImage();
        String currentDishGroup = "";

        // store rows in a model array
        String[] lines = rows.split("\\R"); // any newline
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            // replace first '.' with ',' for Dish Conversion (else double val will break)
            line = line.replaceFirst("\\.", ",");

            // get the id of the current record
            int id = getIdFromDbLine(line);

            // get the dish type from the ID
            String dishType = databaseManager.retrieveDishType(id);

            // if we encounter a new dish type, make a label row for it
            if (!currentDishGroup.equals(dishType)) {
                modelList.add(new DishModel(dishType + "s", false, false));
                currentDishGroup = dishType;
            }

            // handling of a records ImageUri
            String imageUriStr = databaseManager.retrieveImageUri(id);
            if (imageUriStr == null) {      // add an imageless model if no uri was returned from db
                modelList.add(new DishModel(line.trim(), false));
            }
            else {
                Uri imageUri = Uri.parse(imageUriStr);
                Bitmap image = loadFromUri(imageUri);
                modelList.add(new DishModel(line.trim(), false, imageUri));
            }
        }

        return modelList;
    }

    /*private List<DishModel> sortDishModelList() {
        List<DishModel> modelList = new ArrayList<>();

        // retrieve the dish db rows
        String rows = databaseManager.retrieveRowsWithoutImage();

        // store rows in a model array
        String[] lines = rows.split("\\R"); // any newline
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            // replace first '.' with ',' for Dish Conversion (else double val will break)
            line = line.replaceFirst("\\.", ",");

            // get the id of the current record
            int id = getIdFromDbLine(line);

            // use id to get that records imageUri
            String imageUriStr = databaseManager.retrieveImageUri(id);

            if (imageUriStr == null) {      // add an imageless model if no uri was returned from db
                modelList.add(new DishModel(line.trim(), false));
            }
            else {
                Uri imageUri = Uri.parse(imageUriStr);
                // imageUri is handled separately from rest of model list as to
                // not have uri displayed in the dish label text

                // convert uri to bitmap and pass it to new DishModel
                Bitmap image = loadFromUri(imageUri);
                modelList.add(new DishModel(line.trim(), false, imageUri));
            }
        }

        return modelList;
    }*/

    private int getIdFromDbLine(String recordString) {

        String idStr = "";

        for (int i=0; i<recordString.indexOf(',');i++) {
            idStr += recordString.charAt(i);
        }

        // removing of white spaces
        idStr = idStr.replaceAll("\\s+", "");
        Log.i("DishFrag", "Extracted ID: '" + idStr + "'");

        return Integer.parseInt(idStr);
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
        String rows = databaseManager.retrieveRowsWithoutImage();

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

    private void setUpdateFormFields(Dish dish) {

        binding.editUpdateDishName.setText(dish.getDishName());

        Log.d("DEBUG", "dish type = '" + dish.getDishType() + "'");


        switch (dish.getDishType()) {
            case " Entry":
                binding.radioUpdateDishType.check(R.id.radioUpdateEntry);
                break;
            case " Main":
                binding.radioUpdateDishType.check(R.id.radioUpdateMain);
                break;
            case " Drink":
                binding.radioUpdateDishType.check(R.id.radioUpdateDrink);
                break;
        }

        binding.editUpdateIndgredients.setText(dish.getIngredients());
        binding.editUpdatePrice.setText(Double.toString(dish.getPrice()));

        if (!dish.getImage().isEmpty()) {
            Uri uri = Uri.parse(dish.getImage());
            binding.imageUpdateView.setImageURI(uri);
        }
    }
}





/*

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
 */