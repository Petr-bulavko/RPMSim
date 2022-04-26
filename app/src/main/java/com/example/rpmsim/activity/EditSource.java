package com.example.rpmsim.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

import com.example.rpmsim.R;
import com.example.rpmsim.database.Constants;
import com.example.rpmsim.database.DatabaseHelper;
import com.example.rpmsim.entity.Source;
import com.example.rpmsim.fragment.FragmentAddSource;

import java.util.ArrayList;
import java.util.Locale;

public class EditSource extends AppCompatActivity implements View.OnClickListener{

    private EditText editCoefficient, editActivitySource, editCoordinateSourceX, editCoordinateSourceY, editCoordinateSourceZ;
    private TextView txtCoefficientEdit;
    private Spinner spinner_source_edit;
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;
    private Cursor cursor_source;
    private Cursor cursor;

    private ArrayList<Source> sources;
    private int positionInRecycler;
    private String nameSource;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_source);

        txtCoefficientEdit = findViewById(R.id.txtCoefficientEdit);
        editCoefficient = findViewById(R.id.editCoefficient);
        editActivitySource = findViewById(R.id.editActivitySource);
        spinner_source_edit = findViewById(R.id.spinner_source_edit);
        editCoordinateSourceX = findViewById(R.id.editCoordinateSourceX);
        editCoordinateSourceY = findViewById(R.id.editCoordinateSourceY);
        editCoordinateSourceZ = findViewById(R.id.editCoordinateSourceZ);

        Bundle args = getIntent().getExtras();
        if (args != null) {
            sources = (ArrayList<Source>) args.getSerializable("sources");
            positionInRecycler = args.getInt("position");
        }

        Button editSource = findViewById(R.id.edit_source);
        editSource.setOnClickListener(this);

        spinner_source_edit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("Range")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editCoefficient.setText("");
                if (sources.get(positionInRecycler).getPositionInSpinner() != position) {
                    /*Constants.COLUMN_SOURCE_FACTOR_DIMENSION*/
                    cursor = db.rawQuery("select * from " + Constants.TABLE_SOURCE_FACTOR + " where "
                            + Constants.COLUMN_SOURCE_FACTOR_ID +
                            "=" + (spinner_source_edit.getSelectedItemPosition() + 1), null);

                    cursor_source = db.rawQuery("select " + Constants.COLUMN_NAME_SOURCE + " from " + Constants.TABLE_SOURCE + " where "
                            + Constants.COLUMN_ID_SOURCE +
                            "=" + (spinner_source_edit.getSelectedItemPosition() + 1), null);

                    while (cursor.moveToNext() && cursor_source.moveToNext()) {
                        editCoefficient.append(cursor.getString(1));
                        nameSource = cursor_source.getString(0);
                    }

                } else {
                    editActivitySource.setText(String.format(Locale.ROOT, "%.1f", sources.get(positionInRecycler).getActivitySource()));
                    editCoefficient.setText(String.format(Locale.ROOT, "%.4f", sources.get(positionInRecycler).getCoefficient()));
                    editCoordinateSourceX.setText(String.format(Locale.ROOT, "%.0f", sources.get(positionInRecycler).getCoordinateSourceX()));
                    editCoordinateSourceY.setText(String.format(Locale.ROOT, "%.0f", sources.get(positionInRecycler).getCoordinateSourceY()));
                    editCoordinateSourceZ.setText(String.format(Locale.ROOT, "%.0f", sources.get(positionInRecycler).getCoordinateSourceZ()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        databaseHelper = new DatabaseHelper(this);
        databaseHelper.create_db();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    @Override
    public void onResume() {
        super.onResume();
        db = databaseHelper.open();

        cursor_source = db.rawQuery("select * from " + Constants.TABLE_SOURCE, null);

        String[] source = new String[]{Constants.COLUMN_NAME_SOURCE};

        SimpleCursorAdapter adapter_source = new SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_item,
                cursor_source, source, new int[]{android.R.id.text1}, 0);
        adapter_source.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_source_edit.setAdapter(adapter_source);
        spinner_source_edit.setSelection(sources.get(positionInRecycler).getPositionInSpinner());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
        cursor_source.close();
    }


    @Override
    public void onClick(View v) {
        if (nameSource != null){
            sources.get(positionInRecycler).setNameSource(nameSource);
        }
        sources.get(positionInRecycler).setActivitySource(Double.parseDouble(editActivitySource.getText().toString()));
        sources.get(positionInRecycler).setCoordinateSourceX(Double.parseDouble(editCoordinateSourceX.getText().toString()));
        sources.get(positionInRecycler).setCoordinateSourceY(Double.parseDouble(editCoordinateSourceY.getText().toString()));
        sources.get(positionInRecycler).setCoordinateSourceZ(Double.parseDouble(editCoordinateSourceZ.getText().toString()));
        sources.get(positionInRecycler).setCoefficient(Double.parseDouble(editCoefficient.getText().toString()));

        FragmentAddSource.save_source(sources);
        this.finish();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)ev.getRawX(), (int)ev.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
