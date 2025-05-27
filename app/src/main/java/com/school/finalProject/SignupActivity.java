package com.school.finalProject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignupActivity extends AppCompatActivity {
    EditText editUser,editPassword, editRePassword;
    Button btnSignup, btnReturnToLogin;
    DBHelper dbHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        EdgeToEdge.enable(this);

        editUser = findViewById(R.id.signupUsr);
        editPassword = findViewById(R.id.signupPswd);
        editRePassword = findViewById(R.id.signupPswd2);
        btnSignup = findViewById(R.id.signUpButton2);
        btnReturnToLogin = findViewById(R.id.signupLoginBtn);
        dbHelper = new DBHelper(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signupPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = editUser.getText().toString();
                String password = editPassword.getText().toString();
                String repassword = editRePassword.getText().toString();

                if (username.isEmpty() || password.isEmpty() || repassword.isEmpty()) {//checks to make sure fields are not empty, if not gives a notification and returns
                    Toast.makeText(SignupActivity.this, "Please fill in all the fields", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!password.equals(repassword)) {//checks to make sure passwords are the same, if not gives a notification and returns
                    Toast.makeText(SignupActivity.this, "Passwords do not match", Toast.LENGTH_LONG).show();
                    return;
                }

                if (dbHelper.usernameStatus(username)) {//checks to make sure the username is not already taken, if it is it gives a notification and returns
                    Toast.makeText(SignupActivity.this, "Username is taken", Toast.LENGTH_LONG).show();
                    return;
                }

                boolean signUpSuccessful = dbHelper.insertUser(username,password); //if checks pass, data is inserted
                if (!signUpSuccessful){//if not successfully signed up, notifies user.
                    Toast.makeText(SignupActivity.this, "Unable to Sign Up User.", Toast.LENGTH_LONG).show();
                    return;
                }

                //if all checks pass, user is successfully signed up.
                Toast.makeText(SignupActivity.this, "User Successfully Signed Up!", Toast.LENGTH_LONG).show();

                long userID = dbHelper.getUserID(username, password);

                Intent intent = new Intent(SignupActivity.this, WelcomeActivity.class);
                intent.putExtra("USER_ID", userID);
                startActivity(intent);
                finish();
            }
        });
        btnReturnToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
