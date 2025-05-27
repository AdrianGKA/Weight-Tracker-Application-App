package com.school.finalProject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WelcomeActivity extends AddWeightActivity{
    private EditText currentWeightInput, targetWeightInput;
    private Button contBtn;
    private DBHelper dbHelper;
    private long userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.welcomePage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        currentWeightInput = findViewById(R.id.curWeightInput);
        targetWeightInput = findViewById(R.id.targetWeightInput);
        contBtn = findViewById(R.id.continueButton);
        dbHelper = new DBHelper(this);

        // Retrieve the user ID from the intent
        userId = getIntent().getLongExtra("USER_ID", -1);
        if (userId == -1) {
            Toast.makeText(this, "Invalid user ID.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        contBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String curString = currentWeightInput.getText().toString();
                String targetString = targetWeightInput.getText().toString();

                if (curString.isEmpty() || targetString.isEmpty()){ //if weight entries are empty, notify user
                    Toast.makeText(WelcomeActivity.this, "Please enter your current and target weight.", Toast.LENGTH_SHORT).show();
                    return;
                }

                //returns weights back to doubles to store in database
                double currentWeight = Double.parseDouble(curString);
                double targetWeight = Double.parseDouble(targetString);

                saveWeights(currentWeight, targetWeight);

                Intent intent = new Intent(WelcomeActivity.this, DashboardActivity.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
                finish();
            }
        });
    }

    public void saveWeights(double currentWeight, double targetWeight) { //function meant to use current weight, and create identical historical entries for graph and also save target weight
        long currentdate = System.currentTimeMillis();

        //stores current date, weight, and target weight
        dbHelper.insertWeight(userId, formatDate(currentdate), currentWeight);
        dbHelper.insertTargetWeight(userId, targetWeight);

        for (int i = 1; i <= 5; ++i) {
            long pastDates = currentdate - (i * 24 * 60 * 60 * 1000); // subtracts 1 day for each loop
            dbHelper.insertWeight(userId, formatDate(pastDates), currentWeight);//stores current weight to old dates.
        }

    }

    private String formatDate(long date) { //Method for format date to month day year
        SimpleDateFormat mdy = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        return mdy.format(new Date(date));
    }
}
