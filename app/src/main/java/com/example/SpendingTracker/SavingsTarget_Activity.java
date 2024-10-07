package com.example.SpendingTracker;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.SpendingTracker.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SavingsTarget_Activity extends AppCompatActivity {

    private EditText editTextSavingsTarget;
    private Button buttonSaveSavingsTarget;

    private double savingsTarget = 500.00; // Initial savings target value
    private FirebaseFirestore db;
    private String userId;

    private double savedAmount = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings_target);

        // Initialize userId before calling loadTotalFromFirestore()
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            userId = user.getUid();
            loadTotalFromFirestore();

            editTextSavingsTarget = findViewById(R.id.editTextSavingsTarget);
            buttonSaveSavingsTarget = findViewById(R.id.buttonSaveSavingsTarget);

            Button buttonEditSavingsTarget = findViewById(R.id.buttonEditSavingsTarget);
            buttonEditSavingsTarget.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editTextSavingsTarget.setEnabled(true);
                    editTextSavingsTarget.requestFocus();
                    editTextSavingsTarget.setSelection(editTextSavingsTarget.getText().length());

                    updateSavingsTargetProgress(savedAmount);
                    loadSavingsTargetFromFirestore();
                }
            });

    }
        buttonSaveSavingsTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String savingsTargetText = editTextSavingsTarget.getText().toString();
                if (!savingsTargetText.isEmpty()) {
                    try {
                        double newSavingsTarget = Double.parseDouble(savingsTargetText);
                        if (newSavingsTarget >= 0) {
                            savingsTarget = newSavingsTarget;
                            updateSavingsTargetProgress(savedAmount);
                            saveSavingsTargetToFirestore(newSavingsTarget);  // Save the new savings target

                            Toast.makeText(SavingsTarget_Activity.this, "Savings target saved successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SavingsTarget_Activity.this, "Invalid savings target value", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(SavingsTarget_Activity.this, "Invalid savings target value", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SavingsTarget_Activity.this, "Please enter a savings target value", Toast.LENGTH_SHORT).show();
                }

                // Disable editing and clear focus from the EditText
                editTextSavingsTarget.setEnabled(false);
                editTextSavingsTarget.clearFocus();
            }
        });

        updateSavingsTargetProgress(savedAmount);
    }

    private void loadTotalFromFirestore() {
        DocumentReference documentRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("totalAmounts")
                .document("total");

        documentRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Map<String, Object> userData = documentSnapshot.getData();
                            if (userData != null && userData.containsKey("total")) {
                                Double savedTotal = (Double) userData.get("total");
                                if (savedTotal != null) {
                                    savedAmount = savedTotal;
                                    updateSavingsTargetProgress(savedTotal);
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SavingsTarget_Activity.this, "Failed to load total amount: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void updateSavingsTargetProgress(double savedAmount) {
        TextView textViewSavingsTarget = findViewById(R.id.textViewSavingsTarget);
        TextView textViewProgressPercentage = findViewById(R.id.textViewProgressPercentage);
        ProgressBar progressBarSavingsTarget = findViewById(R.id.progressBarSavingsTarget);

        String progressText = String.format("£%.2f / £%.2f", savedAmount, savingsTarget);
        textViewSavingsTarget.setText(progressText);

        int progress = (int) ((savedAmount / savingsTarget) * 100);
        progressBarSavingsTarget.setProgress(progress);

        // Set the text of the progress percentage TextView
        textViewProgressPercentage.setText(String.format("%d%%", progress));
    }


    private void saveSavingsTargetToFirestore(double savingsTarget) {
        DocumentReference documentRef = FirebaseFirestore.getInstance()
                .collection("savings")
                .document(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("savingsTarget", savingsTarget);

        documentRef.set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SavingsTarget_Activity.this, "Savings target saved successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SavingsTarget_Activity.this, "Failed to save savings target: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void loadSavingsTargetFromFirestore() {
        DocumentReference documentRef = FirebaseFirestore.getInstance()
                .collection("savings")
                .document(userId);

        documentRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Double savedSavingsTarget = documentSnapshot.getDouble("savingsTarget");
                            if (savedSavingsTarget != null) {
                                savingsTarget = savedSavingsTarget;
                                editTextSavingsTarget.setText(String.valueOf(savedSavingsTarget));
                                updateSavingsTargetProgress(savedAmount);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SavingsTarget_Activity.this, "Failed to load savings target: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadSavingsTargetFromFirestore();
        loadTotalFromFirestore();
    }



}

