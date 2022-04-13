package com.example.rpmsim.fragment;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.rpmsim.R;
import com.example.rpmsim.database.Constants;
import com.example.rpmsim.database.DatabaseHelper;
import com.example.rpmsim.entity.Source;

import java.util.ArrayList;
import java.util.Locale;

public class FragmentEditSource extends Fragment implements View.OnClickListener {

    final String LOG_TAG = "myLogs";

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


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_edit_source, container, false);
        Log.d(LOG_TAG, "onCreateView_fragment_edit_source");
        txtCoefficientEdit = result.findViewById(R.id.txtCoefficientEdit);
        editCoefficient = result.findViewById(R.id.editCoefficient);
        editActivitySource = result.findViewById(R.id.editActivitySource);
        spinner_source_edit = result.findViewById(R.id.spinner_source_edit);
        editCoordinateSourceX = result.findViewById(R.id.editCoordinateSourceX);
        editCoordinateSourceY = result.findViewById(R.id.editCoordinateSourceY);
        editCoordinateSourceZ = result.findViewById(R.id.editCoordinateSourceZ);

        Bundle args = getArguments();
        if (args != null) {
            sources = (ArrayList<Source>) args.getSerializable("sources");
            positionInRecycler = args.getInt("position");
        }

        Button editSource = result.findViewById(R.id.edit_source);
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
                        //База даных сохранена или на телефоне или на компе
//                        String s = cursor.getString(2);
//                        txtCoefficientEdit.setText(String.format("Коэффициент, %s", "sd"));
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

        databaseHelper = new DatabaseHelper(getActivity());
        databaseHelper.create_db();
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume_fragment_edit_source");
        db = databaseHelper.open();

        cursor_source = db.rawQuery("select * from " + Constants.TABLE_SOURCE, null);

        String[] source = new String[]{Constants.COLUMN_NAME_SOURCE};

        SimpleCursorAdapter adapter_source = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_spinner_item,
                cursor_source, source, new int[]{android.R.id.text1}, 0);
        adapter_source.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_source_edit.setAdapter(adapter_source);
        spinner_source_edit.setSelection(sources.get(positionInRecycler).getPositionInSpinner());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy_fragment_edit_source");
        db.close();
        cursor_source.close();
    }

    @Override
    public void onClick(View v) {
        Log.d(LOG_TAG, "onClick_fragment_edit_source");
        if (nameSource != null){
            sources.get(positionInRecycler).setNameSource(nameSource);
        }
        sources.get(positionInRecycler).setActivitySource(Double.parseDouble(editActivitySource.getText().toString()));
        sources.get(positionInRecycler).setCoordinateSourceX(Double.parseDouble(editCoordinateSourceX.getText().toString()));
        sources.get(positionInRecycler).setCoordinateSourceY(Double.parseDouble(editCoordinateSourceY.getText().toString()));
        sources.get(positionInRecycler).setCoordinateSourceZ(Double.parseDouble(editCoordinateSourceZ.getText().toString()));
        sources.get(positionInRecycler).setCoefficient(Double.parseDouble(editCoefficient.getText().toString()));

        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.swipe_page_two, FragmentAddSource.newInstance(sources)).commit();
    }
}
