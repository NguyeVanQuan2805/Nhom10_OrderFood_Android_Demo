package com.example.neworderfood.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neworderfood.R;
import com.example.neworderfood.models.Table;

import java.util.List;

public class TableStatusAdapter extends RecyclerView.Adapter<TableStatusAdapter.ViewHolder> {
    private List<Table> tables;

    public TableStatusAdapter(List<Table> tables) {
        this.tables = tables;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_table_status, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Table table = tables.get(position);
        holder.numberText.setText("Bàn " + table.getNumber());
        holder.statusText.setText(table.getStatus());
        // Color code: Green for Available, Red for Occupied
        if ("Còn trống".equals(table.getStatus())) {
            holder.statusText.setTextColor(Color.GREEN);
        } else {
            holder.statusText.setTextColor(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        return tables.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView numberText, statusText;

        ViewHolder(View view) {
            super(view);
            numberText = view.findViewById(R.id.table_number_status);
            statusText = view.findViewById(R.id.table_status_status);
        }
    }
}