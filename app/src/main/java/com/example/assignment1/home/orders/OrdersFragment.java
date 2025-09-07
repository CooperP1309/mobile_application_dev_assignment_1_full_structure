package com.example.assignment1.home.orders;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
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
import com.example.assignment1.databinding.FragmentOrdersBinding;
import com.example.assignment1.home.Model;
import com.example.assignment1.home.dishes.Dish;
import com.example.assignment1.home.dishes.DishDatabaseManager;
import com.example.assignment1.home.dishes.DishModel;
import com.example.assignment1.home.dishes.DishesAdapter;
import com.example.assignment1.home.dishes.DishesFragment;
import com.example.assignment1.home.dishes.NormalAdapter;
import com.example.assignment1.home.dishes.UpdateAdapter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrdersFragment extends Fragment {

    private FragmentOrdersBinding binding;
    private DishDatabaseManager dishDbManager;
    private OrdersDatabaseManager orderDbManager;

    public OrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // final binding setup
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // injecting context of associated activity into database managers
        dishDbManager = new DishDatabaseManager(requireContext());
        orderDbManager = new OrdersDatabaseManager(requireContext());

        // retrieving order on create
        retrieveOrders();

        binding.buttonCreateForm.setOnClickListener(v->{
            showCreateForm();
        });

        binding.buttonUpdateForm.setOnClickListener(v->{
            showUpdateForm();
        });

        handleCloseButtons();

        return binding.getRoot();
    }

    private void handleCloseButtons() {
        binding.buttonExitCreate.setOnClickListener(v->{
            retrieveOrders();
        });
        binding.buttonExitUpdate.setOnClickListener(v->{
            retrieveOrders();
        });
    }

    private void showCreateForm() {

        // handle form visibilities
        binding.formUpdate.setVisibility(View.GONE);
        binding.formManage.setVisibility(View.GONE);
        binding.formCreate.setVisibility(View.VISIBLE);

        // inflate select dishes recycler view
        DishesAdapter normalAdapter = new DishesAdapter(sortDishModelList());
        binding.recyclerAddDishes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerAddDishes.setAdapter(normalAdapter);
        binding.recyclerAddDishes.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));


        // event trigger - user selects a dish
        normalAdapter.setOnSelectionChangedListener(hasSelected -> {

            if (hasSelected) {
                // push that row into a dish object
                String selectedRows = normalAdapter.getSelected().toString();

                // get total price from the currently selected rows
                Log.i("OrderFrag", "Calling totalPrice from createForm()");
                Double price = getTotalPrice(selectedRows);

                // set the price field to reflect this
                binding.editAddPrice.setText(price.toString());
            }
        });

        // event trigger - user attempts to add order with form details filled
        binding.buttonAddOrder.setOnClickListener(v->{
            // verify user inputs are all valid
            if (!validateFormData(normalAdapter)) {
                return;
            }

            // retrieve form data
            Order newOrder = collectFormData(normalAdapter);

            // push form data into db record
            orderDbManager.addRow(newOrder);

            // show updated data
            reloadFragment();
        });
    }

    private boolean validateFormData(DishesAdapter dishView) {
        // check for empty fields
        if (binding.editAddOrderID.getText().toString().isEmpty() ||
                binding.radioAddDiningOption.getCheckedRadioButtonId() == -1 ||
                binding.editAddPrice.getText().toString().isEmpty()) {
                binding.textCreateErrorResponse.setText("One or more fields are empty");
                return false;
        }

        // if dine in selected & no table number provided or table number = 0 (case for N/A)
        if (binding.radioAddDiningOption.getCheckedRadioButtonId() == R.id.radioDineIn &&
                binding.editAddTableNumber.getText().toString().isEmpty()) {
            binding.textCreateErrorResponse.setText("Table number must be provided when dining in");
            return false;
        }
        if (binding.radioAddDiningOption.getCheckedRadioButtonId() == R.id.radioDineIn &&
                binding.editAddTableNumber.getText().toString().equals("0")) {
            binding.textCreateErrorResponse.setText("Non-existing table number");
            return false;
        }

        // if take away selected and table number provided, clear that table number
        if (binding.radioAddDiningOption.getCheckedRadioButtonId() == R.id.radioTakeAway) {
            binding.editAddTableNumber.setText("0");
        }

        // check OrderId is unique
        if (!checkIdUnique(binding.editAddOrderID.getText().toString())) {
            binding.textCreateErrorResponse.setText("Order ID already exists!");
            return false;
        }

        // checking that at least one dish is selected
        if (!dishView.hasSelected()) {
            binding.textCreateErrorResponse.setText("No dishes selected!");
            return false;
        }

        return true;
    }

    private boolean checkIdUnique(String idStrInput) {

        // strip white spaces for safe comparison
        idStrInput = idStrInput.replaceAll("\\s+", "");

        // get all rows from database
        String rows = orderDbManager.retrieveRows();

        // sort rows into array
        String[] lines = rows.split("\\R");
        List<Integer> idList = new ArrayList<>();

        if (lines[0].isEmpty()) {                       // no records to compare with
            return true;
        }

        for (String line : lines) {                     // for each record in the db
            String idStr = "";
            for (int i=0; i<line.indexOf('.'); i++) {   // (ID field end delimited by '.' in SQL DB)
                idStr = idStr + line.charAt(i);         // extract the current record ID
            }

            // strip whitespaces for safe comparison
            idStr = idStr.replaceAll("\\s+", "");

            if (idStrInput.equals(idStr)) {
                Log.i("OrdersFrag", "OrderID NOT unique: " + idStr);
                return false;
            }
        }
        Log.i("OrdersFrag", "OrderID IS unique: " + idStrInput);

        return true;
    }

    private Order collectFormData(DishesAdapter dishView) {
        // collecting edit text data
        String orderIdStr = binding.editAddOrderID.getText().toString();
        String tableNumberStr = binding.editAddTableNumber.getText().toString();
        double price = Double.parseDouble(binding.editAddPrice.getText().toString());

        // collecting radio button data
        int selectedDiningOption = binding.radioAddDiningOption.getCheckedRadioButtonId();
        RadioButton selectedButton = binding.radioAddDiningOption.findViewById(selectedDiningOption);
        String diningOption = selectedButton.getText().toString();

        // collecting recycler view data
        String selectedDishIDs = getIdStrings(dishView.getSelected());

        return new Order(Integer.parseInt(orderIdStr), Integer.parseInt(tableNumberStr),
                diningOption, selectedDishIDs, price);
    }

    private String getIdStrings(String records) {

        //sort selected dishes into array of strings (per newline)
        String[] lines = records.split("\\R");

        // process the ID of each line into string of IDs
        String dishIDs = "";
        for (int j=0; j< lines.length; j++) {
            String id = "";
            String line = lines[j];
            Log.i("OrdersFrag", "Processing Dish: " + line + "\n");

            // iterate until first comma is met (the end of our ID section)
            for (int i=0; i<line.indexOf(','); i++) {
                id = id + line.charAt(i);
            }

            // once at end of ID, store id and restart loop
            Log.i("OrdersFrag", "Extracted ID: " + id);
            dishIDs = dishIDs + id;

            if (j != (lines.length-1)) {
                dishIDs = dishIDs + " | ";
            }
        }

        return dishIDs;
    }

    private Double getTotalPrice(String selectedDishes) {

        //sort selected dishes into array of strings (per newline)
        String[] lines = selectedDishes.split("\\R");

        // extract the price field of each record into a double
        double totalPrice = 0.0;
        for (int j=0; j< lines.length; j++) {
            String line = lines[j];
            Log.i("OrdersFrag", "Getting price from: " + line + "\n");

            // iterate through record until fourth comma is met (beginning of price field)
            int currentIndex = 0;
            for (int i = 0; i < 4; i++) {
                currentIndex = line.indexOf(',', currentIndex + 1); // start from last found comma
                Log.i("OrdersFrag", "Index at: " + currentIndex);
            }

            // using index at start of price field, iterate until end of field met
            currentIndex = currentIndex+2;
            String priceStr = "";
            line = line.trim(); // remove trailing whitespaces
            for (;currentIndex<line.length();currentIndex++) {
                priceStr += line.charAt(currentIndex);
            }

            /* No longer works as last field (bitmap) was removed from recycler
            // iterate until the next comma is reached (end of price field)
            for (currentIndex ++ ; currentIndex<line.indexOf(',', currentIndex +1); currentIndex++) {
                priceStr = priceStr + line.charAt(currentIndex);
            }
             */

            // once at end of price, process that price and restart loop
            Log.i("OrdersFrag", "Extracted price: " + priceStr);
            totalPrice = totalPrice + Double.parseDouble(priceStr);
        }

        return totalPrice;
    }

    private void showUpdateForm() {
        // handle form visibilities
        binding.formUpdate.setVisibility(View.VISIBLE);
        binding.formManage.setVisibility(View.GONE);
        binding.formCreate.setVisibility(View.GONE);

        // inflate order recycler
        UpdateAdapter orderAdapter = new UpdateAdapter(sortOrderList());
        binding.recyclerUpdateOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerUpdateOrders.setAdapter(orderAdapter);
        binding.recyclerUpdateOrders.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        // inflate dishes recycler
        NormalAdapter normalAdapter = new NormalAdapter(sortDishesList());
        binding.recyclerUpdateDishes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerUpdateDishes.setAdapter(normalAdapter);
        binding.recyclerUpdateDishes.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        // event trigger - user selects a dish
        normalAdapter.setOnSelectionChangedListener(hasSelected -> {

            if (hasSelected) {
                // push that row into a dish object
                String selectedRows = normalAdapter.getSelected().toString();

                // get total price from the currently selected rows
                Double price = getTotalPrice(selectedRows);

                // set the price field to reflect this
                binding.editUpdatePrice.setText(price.toString());
            }
        });

        // when a recycler view row is selected...
        orderAdapter.setOnSelectionChangedListener(hasSelected -> {

            // push that row into a dish object
            String selectedRow = orderAdapter.getSelected().toString();

            // get the ID from the selected row
            String idStr ="";
            for (int i=0; i <selectedRow.indexOf(","); i++) {
                idStr += selectedRow.charAt(i);
            }

            // use ID to push unpolished order from db into new order object
            String orderDbRecord = orderDbManager.retrieveOrder(Integer.parseInt(idStr));
            Order selectedOrder = new Order(orderDbRecord);

            // set the field of the update form to reflect the properties of the dish object
            setUpdateFormFields(selectedOrder);
        });

        // handling to processing of the form
        binding.buttonUpdateOrder.setOnClickListener(v->{
            // verify user inputs are all valid
            if (!validateUpdateData(normalAdapter, orderAdapter)) {
                return;
            }

            // get the ID from the selected row
            String selectedRow = orderAdapter.getSelected().toString();
            String idStr ="";
            for (int i=0; i <selectedRow.indexOf(","); i++) {
                idStr += selectedRow.charAt(i);
            }

            // retrieve form data
            Order newOrder = collectUpdateData(normalAdapter, Integer.parseInt(idStr));

            // push form data into db record
            orderDbManager.updateRow(newOrder);

            // show updated data
            reloadFragment();
        });

        binding.buttonDeleteUpdate.setOnClickListener(v->{
            // get the selected record
            String selectedRecord = orderAdapter.getSelected();

            if (selectedRecord.isEmpty()) {
                binding.textUpdateErrorResponse.setText("No order is selected");
            }
            else {
                int id = getIdFromDbLine(selectedRecord);
                int [] idList = {id};
                orderDbManager.deleteRows(idList);
                reloadFragment();
            }
        });
    }

    private void setUpdateFormFields(Order selectedOrder) {

        // selecting of dining option
        switch (selectedOrder.getDiningOption()) {
            case "DineIn":
                binding.radioUpdateDiningOption.check(R.id.radioUpdateDineIn);
                binding.editUpdateTableNumber.setText(Integer.toString(selectedOrder.getTableNo()));
                break;
            case "TakeAway":
                binding.radioUpdateDiningOption.check(R.id.radioUpdateTakeAway);
                binding.editUpdateTableNumber.setText("");
                break;
        }

        binding.editUpdatePrice.setText(Double.toString(selectedOrder.getPrice()));
    }

    private boolean validateUpdateData(NormalAdapter dishView, UpdateAdapter orderView) {
        // check for empty fields
        if (!orderView.hasSelected()) {
            binding.textUpdateErrorResponse.setText("No order selected!");
            return false;
        }

        // if dine in selected & no table number provided or table number = 0 (case for N/A)
        if (binding.radioUpdateDiningOption.getCheckedRadioButtonId() == R.id.radioUpdateDineIn &&
                binding.editUpdateTableNumber.getText().toString().isEmpty()) {
            binding.textUpdateErrorResponse.setText("Table number must be provided when dining in");
            return false;
        }
        if (binding.radioUpdateDiningOption.getCheckedRadioButtonId() == R.id.radioUpdateDineIn &&
                binding.editUpdateTableNumber.getText().toString().equals("0")) {
            binding.textUpdateErrorResponse.setText("Non-existing table number");
            return false;
        }

        // if take away selected and table number provided, clear that table number
        if (binding.radioUpdateDiningOption.getCheckedRadioButtonId() == R.id.radioUpdateTakeAway &&
                !binding.editUpdateTableNumber.getText().toString().isEmpty()) {
            binding.editUpdateTableNumber.setText("0");
        }

        // checking that at least one dish is selected
        if (!dishView.hasSelected()) {
            binding.textUpdateErrorResponse.setText("No dishes selected!");
            return false;
        }

        return true;
    }

    private Order collectUpdateData(NormalAdapter dishView, int orderId) {
        // collecting edit text data
        String tableNumberStr = binding.editUpdateTableNumber.getText().toString();

        // collecting radio button data
        int selectedDiningOption = binding.radioUpdateDiningOption.getCheckedRadioButtonId();
        RadioButton selectedButton = binding.radioUpdateDiningOption.findViewById(selectedDiningOption);
        String diningOption = selectedButton.getText().toString();

        // collecting recycler view data
        Log.i("OrderFrag", "Calling totalPrice from collectForm()");
        String selectedDishIDs = getIdStrings(dishView.getSelected());
        double price = getTotalPrice(dishView.getSelected());

        return new Order(orderId, Integer.parseInt(tableNumberStr),
                diningOption, selectedDishIDs, price);
    }

    private void retrieveOrders() {
        // handle form visibilities
        binding.formUpdate.setVisibility(View.GONE);
        binding.formManage.setVisibility(View.VISIBLE);
        binding.formCreate.setVisibility(View.GONE);

        // inflate recycler adapter
        OrderAdapter normalAdapter = new OrderAdapter(sortOrderList());
        binding.recyclerSelect.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerSelect.setAdapter(normalAdapter);
        binding.recyclerSelect.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        // only showing processing time when an order is short clicked
        normalAdapter.setOnShortClickListener(checkShortClickMade->{
            if(checkShortClickMade){
                binding.textProcessingTime.setVisibility(View.VISIBLE);

                // get record of the most recently selected view from db
                String mostRecentRecord = orderDbManager.retrieveOrderWithTime(getMostRecentRecordId(normalAdapter));

                // extract the timestamp from this record
                String timeStamp = extractTimestamp(mostRecentRecord);

                // use the time stamp to get the processing time
                String processTime = getProcessingTime(timeStamp);

                binding.textProcessingTime.setText(processTime);
            }
        });

        // only showing the delete button when at least one order is selected
        normalAdapter.setOnSelectionChangedListener(hasSelected -> {
            if (hasSelected) {
                binding.buttonDeleteOrder.setVisibility(View.VISIBLE);
            }
            else {
                binding.buttonDeleteOrder.setVisibility(GONE);
            }
        });

        // handling the deletion of orders on click
        binding.buttonDeleteOrder.setOnClickListener(v->{
            String selectedOrders = normalAdapter.getSelected().toString();
            deleteOrders(selectedOrders);
        });
    }

    private int getMostRecentRecordId(OrderAdapter adapter) {
        String selectedView = adapter.getLastShortClickedPosString();

        // get the ID from the record
        String idStr = "";
        for (int i=0; i<selectedView.indexOf(","); i++) {
            idStr += selectedView.charAt(i);
        }

        // remove whitespaces
        idStr = idStr.replaceAll("\\s+", "");

        return Integer.parseInt(idStr);
    }

    private String extractTimestamp(String selectedRow) {
        // get starting index of timestamp
        // get the beginning and end indexes of the time stamp field
        int currentIndex = 0;
        for (int i = 0; i < 4; i++) {
            currentIndex = selectedRow.indexOf(',', currentIndex + 1); // start from last found comma
        }
        int startOfTableNo = currentIndex + 1;

        // extract the found number into an int for comparison
        String timeStampStr = selectedRow.substring(startOfTableNo);
        timeStampStr = timeStampStr.trim();
        Log.i("OrdersFrag", "Extracted Time Stamp: '" + timeStampStr
                    + "' From record: " + selectedRow);

        return timeStampStr;
    }

    private String getProcessingTime(String timeCreated) {
        // convert timeCreated into a date data structure
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date orderDate;
        try{
            orderDate = sdf.parse(timeCreated);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        // get the current time
        long now = System.currentTimeMillis();

        // get the time passed in milli seconds
        long difference = now - orderDate.getTime();

        // get the total seconds that have passed
        long seconds = difference / 1000;

        // get the total minutes from the total seconds
        long minutes = seconds / 60;

        // finally, get the remainder in seconds
        seconds = seconds % 60;

        return "Processing Time:\n" + (minutes-600) + " mins and " + seconds + " secs ago";
    }

    private void deleteOrders(String selectedOrders) {

        // get the ID of each selected order in a newline delimited list
        String orderIdsStr = getIdStrings(selectedOrders);

        // sort the IDs into an array of strings
        String[] lines = orderIdsStr.split(" \\| ");
        // Note: "\\|" so that the '|' is read as a literal and not regex

        // process the ID of each line into int array
        int[] idList = new int[lines.length];
        for (int j=0; j< lines.length; j++) {

            // strip whitespaces for safe int parsing
            String line = lines[j].replaceAll("\\s+","");
            Log.i("OrderFrag", "Processing ID: " + line + "\n");

            // strip whitespaces for safe comparison
            idList[j] = Integer.parseInt(line);
        }

        // passing the extracted IDs into database delete method
        orderDbManager.deleteRows(idList);

        // refresh view
        reloadFragment();
    }

    private List<Model> sortOrderList() {
        List<Model> modelList = new ArrayList<>();

        // retrieve the dish db rows
        String rows = orderDbManager.retrieveRows();

        // store rows in a model array
        String[] lines = rows.split("\\R"); // any newline
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            // replace first '.' with ',' for SQL to recycler conversion
            line = line.replaceFirst("\\.", ",");

            // extract the dish name from the dish id section
            line = insertDishNames(line);
            line = polishTableNo(line);
            line = polishPrice(line);

            Log.i("OrdersFrag", "Adding line to recycler: " + line);

            modelList.add(new Model(line.trim(), false));
        }

        return modelList;
    }

    private String insertDishNames(String line) {

        // get the beginning and end indexes of the dish ID field (3rd comma to fourth comma)
        int currentIndex = 0;
        for (int i = 0; i < 3; i++) {
            currentIndex = line.indexOf(',', currentIndex + 1); // start from last found comma
        }
        int startOfDishes = currentIndex + 2; // + 2 for the comma itself and the space there after
        int endOfDishes = line.indexOf(',', startOfDishes); // the next comma index after start of id field

        // extract the found IDs into a string array
        String idSectionStr = line.substring(startOfDishes, endOfDishes);
        Log.i("OrdersFrag", "Extracted IDs: " + idSectionStr);
        String[] idStrList = idSectionStr.split(" \\| ");

        // convert each id str into an actual int
        String actualNames = "";
        for (int i=0; i<idStrList.length; i++) {
            String idStr = idStrList[i];
            String dishName = dishDbManager.retrieveDishname(Integer.parseInt(idStr));

            // append the retrieved name to our actual names string
            actualNames += dishName;

            if (i != idStrList.length-1) {
                actualNames += " | ";
            }
        }

        // using the indexes for the start and end of id field, replace the id char
        // sequence with the acquired names
        line = line.substring(0, startOfDishes) +  actualNames + line.substring(endOfDishes);

        return line;
    }

    private String polishTableNo(String line) {

        // get the beginning and end indexes of the table no field (2rd comma to third comma)
        int currentIndex = 0;
        for (int i = 0; i < 2; i++) {
            currentIndex = line.indexOf(',', currentIndex + 1); // start from last found comma
        }
        int startOfTableNo = currentIndex + 2; // + 2 for the comma itself and the space there after
        int endOfTableNo = line.indexOf(',', startOfTableNo); // the next comma index after start of id field

        // extract the found number into an int for comparison
        String tableNoStr = line.substring(startOfTableNo, endOfTableNo);
        Log.i("OrdersFrag", "Extracted table number: " + tableNoStr);

        // make sure of no whitespaces
        tableNoStr = tableNoStr.replaceAll("\\s+", "");

        // convert to int for 0 comparison
        if (Integer.parseInt(tableNoStr) == 0) {
            tableNoStr = "N/A";
        }

        // using the indexes for the start and end of id field, replace the table no char
        // sequence with the acquired names
        line = line.substring(0, startOfTableNo) + " Table Number: "+
                tableNoStr + line.substring(endOfTableNo);

        return line;
    }

    private String polishPrice(String line) {

        // get the beginning and end indexes of the table no field (2rd comma to third comma)
        int currentIndex = 0;
        for (int i = 0; i < 4; i++) {
            currentIndex = line.indexOf(',', currentIndex + 1); // start from last found comma
        }
        int startOfPrice = currentIndex + 2; // + 2 for the comma itself and the space there after

        // insert '$' at this point and return result
        line = line.substring(0, startOfPrice) + "$" + line.substring(startOfPrice);

        return line;
    }

    private List<Model> sortDishesList() {
        List<Model> modelList = new ArrayList<>();

        // retrieve the dish db rows
        String rows = dishDbManager.retrieveRowsWithoutImage();

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
        String rows = dishDbManager.retrieveRowsWithoutImage();
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
            String dishType = dishDbManager.retrieveDishType(id);

            // if we encounter a new dish type, make a label row for it
            if (!currentDishGroup.equals(dishType)) {
                modelList.add(new DishModel(dishType + "s", false, false));
                currentDishGroup = dishType;
            }

            // handling of a records ImageUri
            String imageUriStr = dishDbManager.retrieveImageUri(id);
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

    private void reloadFragment() {
        FragmentManager fm = requireActivity().getSupportFragmentManager();

        fm.beginTransaction()
                .replace(R.id.frameLayout, new OrdersFragment())
                .addToBackStack(null) // optional
                .commit();
    }
}