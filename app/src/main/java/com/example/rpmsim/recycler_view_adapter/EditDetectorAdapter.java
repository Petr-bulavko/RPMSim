package com.example.rpmsim.recycler_view_adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rpmsim.R;
import com.example.rpmsim.custom_text_watcher.CustomEditTextListener;

import java.util.ArrayList;

public class EditDetectorAdapter extends RecyclerView.Adapter<EditDetectorAdapter.ViewHolder> {

    Context context;
    ArrayList<String> arrayNameSource;
    ArrayList<String> arrayDimension;
    ArrayList<Double> arraySensitivity;

    public EditDetectorAdapter(Context context, ArrayList<String> arrayNameSource, ArrayList<String> arrayDimension, ArrayList<Double> arraySensitivity) {
        this.context = context;
        this.arrayNameSource = arrayNameSource;
        this.arrayDimension = arrayDimension;
        this.arraySensitivity = arraySensitivity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_recycler_view_edit, parent, false);

        return new ViewHolder(view, new CustomEditTextListener(this));
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.txtSource_dimension_edit.setText(String.format("%d. %s - %s", position + 1, arrayNameSource.get(position), arrayDimension.get(position)));
        holder.customEditTextListener.updatePosition(holder.getAdapterPosition());
        holder.edit_sensitivity.setText(String.format("%.0f", arraySensitivity.get(holder.getAdapterPosition())));
    }

    @Override
    public int getItemCount() {
        return arraySensitivity.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // initialize variables
        TextView txtSource_dimension_edit;
        EditText edit_sensitivity;
        CustomEditTextListener customEditTextListener;

        public ViewHolder(@NonNull View itemView, CustomEditTextListener customEditTextListener) {
            super(itemView);
            // assign variables
            txtSource_dimension_edit = itemView.findViewById(R.id.txtSource_dimension_edit);
            edit_sensitivity = itemView.findViewById(R.id.edit_sensitivity);
            this.customEditTextListener = customEditTextListener;
            this.edit_sensitivity.addTextChangedListener(customEditTextListener);
        }
    }

    public ArrayList<Double> getArraySensitivity() {
        return arraySensitivity;
    }
}
