package com.example.neworderfood.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neworderfood.R;
import com.example.neworderfood.models.Invoice;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.ViewHolder> {
    private List<Invoice> invoices;
    private OnInvoiceClickListener listener;

    public interface OnInvoiceClickListener {
        void onInvoiceClick(Invoice invoice);
    }

    public InvoiceAdapter(List<Invoice> invoices, OnInvoiceClickListener listener) {
        this.invoices = invoices;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invoice, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Invoice invoice = invoices.get(position);
        holder.tvInvoiceNumber.setText("Số: " + invoice.getId());
        holder.tvDate.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(invoice.getDate()));
        holder.tvTable.setText("Bàn " + invoice.getTableNumber());
        holder.tvTotal.setText(String.format("%,dđ", invoice.getTotalAmount()));
        holder.tvChange.setText(String.format("Trả lại: %,dđ", invoice.getChangeAmount()));

        holder.itemView.setOnClickListener(v -> listener.onInvoiceClick(invoice));
    }

    @Override
    public int getItemCount() {
        return invoices.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInvoiceNumber, tvDate, tvTable, tvTotal, tvChange;

        ViewHolder(View view) {
            super(view);
            tvInvoiceNumber = view.findViewById(R.id.tv_invoice_number);
            tvDate = view.findViewById(R.id.tv_date);
            tvTable = view.findViewById(R.id.tv_table);
            tvTotal = view.findViewById(R.id.tv_total);
            tvChange = view.findViewById(R.id.tv_change);
        }
    }
}