package com.example.rpmsim.custom_text_watcher;

import android.text.Editable;
import android.text.TextWatcher;

import com.example.rpmsim.recycler_view_adapter.EditDetectorAdapter;

public class CustomEditTextListener implements TextWatcher {

    private final EditDetectorAdapter editDetectorAdapter;
    private int position;

    public CustomEditTextListener(EditDetectorAdapter editDetectorAdapter) {
        this.editDetectorAdapter = editDetectorAdapter;
    }

    public void updatePosition(int position) {
        this.position = position;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        editDetectorAdapter.getArraySensitivity().set(position, Double.valueOf(s.toString()));
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
