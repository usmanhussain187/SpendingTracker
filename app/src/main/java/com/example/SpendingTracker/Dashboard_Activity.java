package com.example.SpendingTracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.util.Collections;
import java.util.Comparator;

import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;


public class Dashboard_Activity extends AppCompatActivity {

    private TextView textViewIncomeAmount;
    private TextView textViewExpenseAmount;
    private TextView textViewTotal;
    private Button btnAddExpense;
    private Button btnAddIncome;
    private Button btnSortTransactions;
    private Button btnSaveTransactions;
    private RecyclerView recyclerView;
    private Set<Transaction> uniqueTransactions;

    private ArrayList<Transaction> transactions;
    private TransactionAdapter transactionAdapter;

    private FirebaseFirestore db;
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        textViewIncomeAmount = findViewById(R.id.textView_income_amount);
        textViewExpenseAmount = findViewById(R.id.textView_expense_amount);
        textViewTotal = findViewById(R.id.textView_total_amount);
        btnAddExpense = findViewById(R.id.btn_add_expense);
        btnAddIncome = findViewById(R.id.btn_add_income);
        btnSortTransactions = findViewById(R.id.btn_sort_transactions);
        btnSaveTransactions = findViewById(R.id.btn_save_transactions);
        recyclerView = findViewById(R.id.recyclerView_dashboard);

        transactions = new ArrayList<>();
        uniqueTransactions = new HashSet<>();
        transactionAdapter = new TransactionAdapter(this, transactions);
        recyclerView.setAdapter(transactionAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Firestore and user ID before calling updateIncomeExpenseAmounts
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        updateIncomeExpenseAmounts();

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        btnAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(false);
            }
        });

        btnAddIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(true);
            }
        });

        btnSortTransactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSortingDialog();
            }
        });

        btnSaveTransactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveTransactionsToFirestore();
            }
        });

        // Call fetchTransactionsFromFirestore() to fetch and display transactions
        fetchTransactionsFromFirestore();

        Button btnViewChart = findViewById(R.id.btn_view_chart);



        btnViewChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard_Activity.this, Graph_Activity.class);
                intent.putExtra("transactions", transactions);
                startActivity(intent);
            }
        });


    }

    private void showDialog(boolean isIncome) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_transaction, null);
        builder.setView(dialogView);

        EditText etAmount = dialogView.findViewById(R.id.et_amount);
        EditText etCategory = dialogView.findViewById(R.id.et_category);
        EditText etDate = dialogView.findViewById(R.id.et_date);
        Button btnAddTransaction = dialogView.findViewById(R.id.btn_add_transaction);

        if (isIncome) {
            builder.setTitle("Add Income");
        } else {
            builder.setTitle("Add Expense");
        }

        AlertDialog dialog = builder.create();

        btnAddTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double amount = Double.parseDouble(etAmount.getText().toString());
                String category = etCategory.getText().toString();
                String date = etDate.getText().toString();
                String type = isIncome ? "income" : "expense"; // determine type based on isIncome parameter

                if (amount <= 0 || category.isEmpty() || date.isEmpty()) {
                    Toast.makeText(Dashboard_Activity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean categoryExists = false;
                for (Transaction transaction : transactions) {
                    if (transaction.getCategory().equalsIgnoreCase(category) && transaction.getType().equals(type)) {
                        transaction.setAmount(transaction.getAmount() + amount);
                        categoryExists = true;
                        break;
                    }
                }

                if (!categoryExists) {
                    Transaction transaction = new Transaction(amount, category, date, type);
                    transactions.add(transaction);
                }

                transactionAdapter.notifyDataSetChanged(); // update the RecyclerView
                updateIncomeExpenseAmounts(); // update the income and expense amounts
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showSortingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_sort_transactions, null);
        builder.setView(dialogView);

        Spinner spinnerField = dialogView.findViewById(R.id.spinner_sort_field);
        Spinner spinnerOrder = dialogView.findViewById(R.id.spinner_sort_order);
        Button btnApplySorting = dialogView.findViewById(R.id.btn_apply_sorting);

        builder.setTitle("Sort Transactions");
        AlertDialog dialog = builder.create();

        btnApplySorting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sortField = spinnerField.getSelectedItem().toString();
                String sortOrder = spinnerOrder.getSelectedItem().toString();
                sortTransactions(sortField, sortOrder);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void sortTransactions(String sortField, String sortOrder) {
        Collections.sort(transactions, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction t1, Transaction t2) {
                int compareResult = 0;
                if (sortField.equals("Amount")) {
                    compareResult = Double.compare(t1.getAmount(), t2.getAmount());
                } else if (sortField.equals("Category")) {
                    compareResult = t1.getCategory().compareTo(t2.getCategory());
                }

                if (sortOrder.equals("Descending")) {
                    compareResult *= -1;
                }

                return compareResult;
            }
        });

        transactionAdapter.notifyDataSetChanged();
    }


    void updateIncomeExpenseAmounts() {
        double totalIncome = 0;
        double totalExpense = 0;

        for (Transaction transaction : transactions) {
            if (transaction.getType().equals("income")) {
                totalIncome += transaction.getAmount();
            } else if (transaction.getType().equals("expense")) {
                totalExpense += transaction.getAmount();
            }
        }

        // Format the income and expense amounts as pounds
        String incomeAmount = String.format("£%.2f", totalIncome);
        String expenseAmount = String.format("£%.2f", Math.abs(totalExpense));

        textViewIncomeAmount.setText(incomeAmount);
        textViewExpenseAmount.setText(expenseAmount);

        textViewIncomeAmount.setTextColor(getResources().getColor(R.color.income_color));
        textViewExpenseAmount.setTextColor(getResources().getColor(R.color.expense_color));

        // calculate and display total
        double total = totalIncome - totalExpense;
        String totalAmount = String.format("£%.2f", total);
        TextView textViewTotal = findViewById(R.id.textView_total_amount);
        textViewTotal.setText(totalAmount);

        // Save the total amount to Firestore
        Map<String, Object> totalData = new HashMap<>();
        totalData.put("total", total);

        db.collection("users")
                .document(userId)
                .collection("totalAmounts")
                .document("total")
                .set(totalData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Dashboard_Activity.this, "Error saving total amount: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }



    private void saveTransactionsToFirestore() {
        // Delete all existing transactions in the Firestore
        db.collection("users")
                .document(userId)
                .collection("transactions")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                db.collection("users")
                                        .document(userId)
                                        .collection("transactions")
                                        .document(document.getId())
                                        .delete();
                            }
                            // Save new transactions after deleting the old ones
                            final AtomicInteger transactionsSaved = new AtomicInteger();
                            final int totalTransactions = transactions.size();
                            for (Transaction transaction : transactions) {
                                // Create a new document with a generated ID
                                Map<String, Object> transactionData = new HashMap<>();
                                transactionData.put("amount", transaction.getAmount());
                                transactionData.put("category", transaction.getCategory());
                                transactionData.put("date", transaction.getDate());
                                transactionData.put("type", transaction.getType());

                                db.collection("users")
                                        .document(userId)
                                        .collection("transactions")
                                        .add(transactionData)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                if (transactionsSaved.incrementAndGet() == totalTransactions) {
                                                    Toast.makeText(Dashboard_Activity.this, "Transaction saved successfully", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(Dashboard_Activity.this, "Error saving transaction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(Dashboard_Activity.this, "Error deleting old transactions: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void fetchTransactionsFromFirestore() {
        uniqueTransactions.clear();
        db.collection("users")
                .document(userId)
                .collection("transactions")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Transaction transaction = new Transaction(
                                        document.getDouble("amount"),
                                        document.getString("category"),
                                        document.getString("date"),
                                        document.getString("type")
                                );
                                uniqueTransactions.add(transaction);
                            }
                            transactions.clear();
                            transactions.addAll(uniqueTransactions);
                            transactionAdapter.notifyDataSetChanged();
                            updateIncomeExpenseAmounts();
                        } else {
                            Toast.makeText(Dashboard_Activity.this, "Error fetching transactions: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchTransactionsFromFirestore();
    }


}

