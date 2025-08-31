package com.example.assignment1.home;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.assignment1.databinding.FragmentHomeBinding;
import com.example.assignment1.login.MainActivity;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding; //declare a binding
    private String username;

    public HomeFragment() {
        // required blank constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // final binding setup
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // extracting and setting of bundle argument
        Bundle bundle = this.getArguments();
        username = bundle.getString("Username");
        binding.textWelcome.setText("Welcome back,\n" + username +"!");

        // wrapper for logout button
        logout();

        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    private void logout() {
        binding.buttonLogout.setOnClickListener(v->{
            // loading of home screen
            Intent nxtIntent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
            startActivity(nxtIntent);
        });
    }
}