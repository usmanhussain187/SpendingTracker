package com.example.SpendingTracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private Button btnDashboard;
    private Button btnTips;
    private Button btnLogout;
    private Button btnSavings;
    private Button btnReminder;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();

        DashboardPage();
    }

    private void DashboardPage () {
        btnDashboard=findViewById(R.id.btn_dashboard);
        btnTips=findViewById(R.id.btn_tips);
        btnLogout=findViewById(R.id.btn_logout);
        btnSavings=findViewById(R.id.btn_SavingsTarget);
        btnReminder=findViewById(R.id.btn_PaymentReminder);

        //Dashboard Page
        btnDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Dashboard_Activity.class));
            }
        });

        //Tips Page
        btnTips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), TipsActivity.class));
            }
        });

        //Savings Target Page
        btnSavings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SavingsTarget_Activity.class));
            }
        });

        //Reminder Page
        btnReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Reminder_Activity.class));
            }
        });

        // Logout Button
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                Toast.makeText(HomeActivity.this, "User Logged Out", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }
}
