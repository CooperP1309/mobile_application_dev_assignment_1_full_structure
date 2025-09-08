package com.example.assignment1.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.assignment1.R;
import com.example.assignment1.databinding.ActivityHomeScreenBinding;
import com.example.assignment1.home.dishes.DishesFragment;
import com.example.assignment1.home.orders.OrdersFragment;

public class HomeScreen extends AppCompatActivity {

    /*      Use of Access Levels
            Level 2: Standard user/employee - Order management
            Level 1: Admin access           - Order and Dish management    */

    private ActivityHomeScreenBinding binding; //declare a binding
    Bundle bundle;                             // bundle to pass params to fragments
    Integer accessLevel;                       // param 1
    String username;                           // param 2

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // final binding setup
        binding = ActivityHomeScreenBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // inheriting of variables from "Login" activity
        Intent getIntent = getIntent();
        accessLevel = getIntent.getIntExtra("AccessLevel", 2);
        username = getIntent.getStringExtra("Username");

        // storing inherited variables in a bundle as access point for fragment classes
        bundle = new Bundle();
        bundle.putInt("AccessLevel", accessLevel);
        bundle.putString("Username", username);

        // setting home fragment on create
        setFragment(new HomeFragment());

        // menu listener wrapper for choosing fragments
        fragmentSwitch();
    }

    public void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        // opening of new fragment operations from current fragment manager
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // injecting bundle into fragment
        fragment.setArguments(bundle);

        // injecting new fragment into our frameLayout
        fragmentTransaction.replace(R.id.frameLayout,fragment);
        fragmentTransaction.commit();
    }

    private void fragmentSwitch(){
        binding.bottomNavigationView.setOnItemSelectedListener(item->{
            switch(item.getItemId()) {

               /*   Each R.id.x references a button id in
                    "bottom_nav_menu.xml" under /res/menu/
                */
                case R.id.home:
                    setFragment(new HomeFragment());
                    break;

                case R.id.orders:
                    setFragment(new OrdersFragment());
                    break;

                case R.id.dishes:
                    setFragment(new DishesFragment());
                    break;
            }
            return true;
        });
    }
}