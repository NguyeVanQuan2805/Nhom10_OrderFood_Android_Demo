package com.example.neworderfood.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neworderfood.R;
import com.example.neworderfood.models.Table;

import java.util.List;

public class AdminTableAdapter extends RecyclerView.Adapter<AdminTableAdapter.ViewHolder> {
    private List<Table> tables;
    private OnTableActionListener listener;

    public interface OnTableActionListener {
        void onTableAction(Table table);
    }

    public AdminTableAdapter(List<Table> tables, OnTableActionListener listener) {
        this.tables = tables;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_table, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Table table = tables.get(position);
        holder.numberText.setText("Bàn " + table.getNumber());
        holder.statusText.setText("Trạng thái: " + table.getStatus());
        holder.itemView.setOnLongClickListener(v -> {
            listener.onTableAction(table);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return tables.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView numberText, statusText;

        ViewHolder(View view) {
            super(view);
            numberText = view.findViewById(R.id.table_number);
            statusText = view.findViewById(R.id.table_status);
        }
    }

    public void updateTables(List<Table> newTables) {
        this.tables = newTables;
        notifyDataSetChanged();
    }
}
