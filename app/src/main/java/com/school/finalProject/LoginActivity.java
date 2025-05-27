package com.school.finalProject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    DBHelper dbHelper;
    private EditText username;
    private EditText password;
    private Button loginButton;
    private Button signupButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        dbHelper = new DBHelper(this);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signUpButton);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usr = username.getText().toString();
                String pswd = password.getText().toString();
                boolean userExists = dbHelper.verifyUser(usr, pswd);
                if (usr.isEmpty() || pswd.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter a Username/Password.", Toast.LENGTH_LONG).show();
                }
                if (userExists) {
                    //Gets the user ID
                    long userId = dbHelper.getUserID(usr, pswd);
                    if (userId == -1) {
                        Toast.makeText(LoginActivity.this, "User ID not found.", Toast.LENGTH_SHORT).show();
                    }

                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                    intent.putExtra("USER_ID", userId);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Incorrect Username/Password.", Toast.LENGTH_LONG).show();
                }
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}