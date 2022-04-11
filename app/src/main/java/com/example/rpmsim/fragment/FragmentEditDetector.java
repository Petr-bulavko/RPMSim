package com.example.rpmsim.fragment;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rpmsim.R;
import com.example.rpmsim.database.Constants;
import com.example.rpmsim.database.DatabaseHelper;
import com.example.rpmsim.entity.Detector;
import com.example.rpmsim.recycler_view_adapter.EditDetectorAdapter;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class FragmentEditDetector extends Fragment implements View.OnClickListener{

    private EditText backgroundEdit, distanceXEdit, distanceYEdit, distanceZEdit;
    private RecyclerView recyclerView;

    private Spinner spinner_detector;

    private EditDetectorAdapter adapter;

    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;

    private Cursor cursor_detector;
    private Cursor cursor_source;
    private Cursor cursor_sensitivity;
    private Cursor cursor;

    private double geometric_size;
    private String nameDetector;
    private String dimension;
    private String nameSource;
    private ArrayList<Detector> detectors;
    private int position_recycler;

    ArrayList<Double> detector_sensitivity_array = new ArrayList<>();
    ArrayList<String> dimensionArray = new ArrayList<>();
    ArrayList<String> sourceArray = new ArrayList<>();

    @SuppressLint("DefaultLocale")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_edit_detector, container, false);

        backgroundEdit = result.findViewById(R.id.backgroundEdit);
        distanceXEdit = result.findViewById(R.id.distanceXEdit);
        distanceYEdit = result.findViewById(R.id.distanceYEdit);
        distanceZEdit = result.findViewById(R.id.distanceZEdit);
        Button editDetector = result.findViewById(R.id.edit_detector);
        editDetector.setOnClickListener(this);

        recyclerView = result.findViewById(R.id.sensitivityEdit);

        spinner_detector = result.findViewById(R.id.spinner_detector_edit);

        Bundle args = getArguments();
        if (args != null) {
            detectors = (ArrayList<Detector>) args.getSerializable("edit_detector");
            position_recycler = (int) args.get("position_recycler_detector");
            backgroundEdit.setText(String.format(Locale.ROOT,"%.1f", detectors.get(position_recycler).getBackground()));
            distanceXEdit.setText(String.format(Locale.ROOT,"%.0f", detectors.get(position_recycler).getX()));
            distanceYEdit.setText(String.format(Locale.ROOT,"%.0f", detectors.get(position_recycler).getY()));
            distanceZEdit.setText(String.format(Locale.ROOT,"%.0f", detectors.get(position_recycler).getZ()));
        }

        spinner_detector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("Range")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                detector_sensitivity_array.clear();
                backgroundEdit.setText("");
                distanceXEdit.setText("0");
                distanceYEdit.setText("0");
                distanceZEdit.setText("0");

                cursor = db.rawQuery("select * from " + Constants.TABLE_DETECTOR + " where " + Constants.COLUMN_ID_DETECTOR +
                        "=" + (spinner_detector.getSelectedItemPosition() + 1), null);

                while (cursor.moveToNext()) {
                    geometric_size = cursor.getDouble(cursor.getColumnIndex(Constants.COLUMN_GEOMETRICAL_SIZES));
                    nameDetector = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_NAME_DETECTOR));
                    if (detectors.get(position_recycler).getPositionInSpinner() != spinner_detector.getSelectedItemPosition()){
                        backgroundEdit.append(cursor.getString(3));
                    }else {
                        backgroundEdit.setText(String.format(Locale.ROOT, "%.1f", detectors.get(position_recycler).getBackground()));
                        distanceXEdit.setText(String.format("%.0f", detectors.get(position_recycler).getX()));
                        distanceYEdit.setText(String.format("%.0f", detectors.get(position_recycler).getY()));
                        distanceZEdit.setText(String.format("%.0f", detectors.get(position_recycler).getZ()));
                    }

                }

                cursor_sensitivity = db.rawQuery("select * from " + Constants.TABLE_DETECTOR_SOURCE + " where " + Constants.COLUMN_DETECTOR_SOURCE_ID +
                        "=" + (spinner_detector.getSelectedItemPosition() + 1), null);
                while (cursor_sensitivity.moveToNext()) {
                    detector_sensitivity_array.add(cursor_sensitivity.getDouble(cursor_sensitivity.getColumnIndex(Constants.COLUMN_VALUE)));
                }

                cursor_source = db.rawQuery("select * from " + Constants.TABLE_SOURCE, null);
                while (cursor_source.moveToNext()) {
                    nameSource = cursor_source.getString(cursor_source.getColumnIndex(Constants.COLUMN_NAME_SOURCE));
                    sourceArray.add(nameSource);
                    dimension = cursor_source.getString(cursor_source.getColumnIndex(Constants.COLUMN_SOURCE_DIMENSION));
                    dimensionArray.add(dimension);
                }

                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                adapter = new EditDetectorAdapter(getActivity(), sourceArray, dimensionArray, detector_sensitivity_array);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        databaseHelper = new DatabaseHelper(getActivity());
        databaseHelper.create_db();
        return result;
    }

    @SuppressLint({"Range", "DefaultLocale"})
    @Override
    public void onResume() {
        super.onResume();

        db = databaseHelper.open();
        cursor_detector = db.rawQuery("select * from " + Constants.TABLE_DETECTOR, null);

        String[] detectorsName = new String[]{Constants.COLUMN_NAME_DETECTOR};

        SimpleCursorAdapter adapter_detector = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_spinner_item,
                cursor_detector, detectorsName, new int[]{android.R.id.text1}, 0);
        adapter_detector.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_detector.setAdapter(adapter_detector);
        spinner_detector.setSelection(detectors.get(position_recycler).getPositionInSpinner());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
        cursor_detector.close();
        cursor_sensitivity.close();
        cursor.close();
        cursor_source.close();
    }

    @Override
    public void onClick(View v) {
        EditDetectorAdapter editDetectorAdapter = (EditDetectorAdapter) recyclerView.getAdapter();
        ArrayList<Double> arrayList = Objects.requireNonNull(editDetectorAdapter).getArraySensitivity();

        detectors.get(position_recycler).setNameDetector(nameDetector);
        detectors.get(position_recycler).setBackground(Double.parseDouble(backgroundEdit.getText().toString()));
        detectors.get(position_recycler).setSensitivity(arrayList);
        detectors.get(position_recycler).setX(Double.parseDouble(distanceXEdit.getText().toString()));
        detectors.get(position_recycler).setY(Double.parseDouble(distanceYEdit.getText().toString()));
        detectors.get(position_recycler).setZ(Double.parseDouble(distanceZEdit.getText().toString()));
        detectors.get(position_recycler).setGeometricalSizes(geometric_size);

        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.swipe_page_one, FragmentDetector.newInstance(detectors)).commit();

    }


}
