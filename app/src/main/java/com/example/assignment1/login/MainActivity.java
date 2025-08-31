package com.example.assignment1.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.assignment1.R;
import com.example.assignment1.databinding.ActivityMainBinding;
import com.example.assignment1.home.HomeScreen;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding; //declare a binding
    /*  NOTICE!
        The name of the above CLASS! It's "Activity" + the name of our activity ("Main")
        plus "Binding". Please remember this when invoking binding in other activities
        (see HomeScreen for an example of a custom named class)
     */
    private Authenticator authenticator; // authenticator

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // final binding setup
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // inject context into authenticator/login class
        authenticator = new Authenticator(this);

        // login button - onClickListener wrapper method
        tryLogin();
    }

    public void tryLogin(){
        binding.buttonLogin.setOnClickListener(v->{
            // pulling entered details from login form
            String user = binding.editUsername.getText().toString();
            String password = binding.editPassword.getText().toString();

            // checkLogin() returns 0 if not a valid user
            int accessLevel = authenticator.checkLogin(user, password);

            if (accessLevel > 0) {
                // loading of home screen
                Intent nxtIntent = new Intent(getApplicationContext(), HomeScreen.class);

                // passing of access level and username to next activity
                nxtIntent.putExtra("AccessLevel", accessLevel);
                nxtIntent.putExtra("Username", user);
                startActivity(nxtIntent);
            }
            else {
                binding.textPrint.setText("Invalid login, please try again.");
            }
        });
    }
}



        /*
        // some code to insert users
            String password = "123";
            authenticator.addRow("mary",password,1);
            authenticator.addRow("john",password,2);
            authenticator.addRow("james",password,2);
            authenticator.addRow("owen",password,1);
            binding.textPrint.setText("Inserted records");
       */