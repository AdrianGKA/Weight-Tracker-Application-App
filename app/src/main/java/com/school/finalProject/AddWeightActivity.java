package com.school.finalProject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddWeightActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private EditText weightInput;
    private Button saveWeightBtn;
    private long userId;
    private static final int REQUEST_CODE = 1000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_addweight);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addWeightPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String[] permissions = {android.Manifest.permission.SEND_SMS};

        // Request permission to send SMS messages
        if (checkSelfPermission(android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
        }

        dbHelper = new DBHelper(this);
        weightInput = findViewById(R.id.weightInput);
        saveWeightBtn = findViewById(R.id.saveWeightButton);

        userId = getIntent().getLongExtra("USER_ID", -1);

        saveWeightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String weightStr = weightInput.getText().toString();
                if (!weightStr.isEmpty()) {//executes code if a entry is not empty
                    if (dbHelper.hasTodayWeightEntry(userId)) { // prevents duplicate entries from happening
                        Toast.makeText(AddWeightActivity.this, "An entry for today already exists.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //formats and inserts data into user specific profile.
                    double weight = Double.parseDouble(weightStr);
                    long date = System.currentTimeMillis();
                    boolean success = dbHelper.insertWeight(userId, formatDate(date), weight);

                    if (success) { //if successful, toasts to notify uer weight was saved and checks against target weight so send SMS notification
                        Toast.makeText(AddWeightActivity.this, "Weight has been saved!", Toast.LENGTH_SHORT).show();
                        checkTargetWeightSMS(weight);
                        finish();
                    } else {// if weight is not successfully saved, notifies user.
                        Toast.makeText(AddWeightActivity.this, "Failed to save weight.", Toast.LENGTH_SHORT).show();
                    }
                } else { //notifies user to enter a weight
                    Toast.makeText(AddWeightActivity.this, "Please enter a weight", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @SuppressWarnings("deprecation")
    private void checkTargetWeightSMS(double currentWeight) {
        //Fetches the user's target weight
        double targetWeight = dbHelper.getTargetWeight(userId);

        if (currentWeight == targetWeight) { //if the current weight entered equals the target weight and user has permission to receive SMS notifications, sends notification to congratulate.
            if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage("15551234567", null,"Congratulations! You've reached your target weight! Way to go!", null, null);

            } else { //In the even the user did not accept to receive SMS notifications, they are sent a toast instead to congratulate.
                Toast.makeText(this, "Congratulations! You've reached your target weight! Way to go!", Toast.LENGTH_LONG).show();
            }
        }
    }
    private String formatDate(long date) { //method to format current date to month, day , year
        SimpleDateFormat mdy = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        return mdy.format(new Date(date));
    }
}
