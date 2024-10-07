package com.example.SpendingTracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.widget.Button;
import android.view.View.OnClickListener;


import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> transactions;
    private Context context;

    public TransactionAdapter(Context context, List<Transaction> transactions) {
        this.context = context;
        this.transactions = transactions;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        holder.textViewType.setText(transaction.getCategory());
        holder.textViewAmount.setText(String.format(Locale.UK, "£%.2f", transaction.getAmount()));
        holder.textViewDate.setText(transaction.getDate());

        // Set the color based on the transaction type
        if (transaction.getType().equals("income")) {
            holder.textViewAmount.setTextColor(ContextCompat.getColor(context, R.color.income_color));
        } else if (transaction.getType().equals("expense")) {
            holder.textViewAmount.setTextColor(ContextCompat.getColor(context, R.color.expense_color));
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewType;
        private TextView textViewAmount;
        private TextView textViewDate;
        private Button btnDeleteTransaction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewType = itemView.findViewById(R.id.textView_type);
            textViewAmount = itemView.findViewById(R.id.textView_amount);
            textViewDate = itemView.findViewById(R.id.textView_date);
            btnDeleteTransaction = itemView.findViewById(R.id.btn_delete_transaction);

            btnDeleteTransaction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        transactions.remove(position);
                        notifyItemRemoved(position);

                        ((Dashboard_Activity) context).updateIncomeExpenseAmounts();

                    }
                }
            });
        }
    }

}

