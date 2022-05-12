package com.example.rpmsim.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rpmsim.R;
import com.example.rpmsim.activity.EditSource;
import com.example.rpmsim.database.Constants;
import com.example.rpmsim.database.DatabaseHelper;
import com.example.rpmsim.entity.Detector;
import com.example.rpmsim.entity.Source;
import com.example.rpmsim.recycler_view_adapter.SourceAdapter;

import java.util.ArrayList;
import java.util.Locale;

public class FragmentAddSource extends Fragment implements View.OnClickListener {

    private static ArrayList<Source> sources;
    final String LOG_TAG = "myLogs";

    private EditText txtCoefficient, activitySource, coordinateSourceX, coordinateSourceY, coordinateSourceZ;
    private Button addSource;
    private RecyclerView recyclerView;
    private SourceAdapter adapter;

    private Spinner spinner_source;
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;
    private Cursor cursor_source;
    private Cursor cursor_source_add;
    private Cursor cursor;

    private String dimension;
    private String nameSource;
    private String dimension_factor;

    ArrayList<String> arrayList;

    public static FragmentAddSource newInstance(ArrayList<Source> sources) {
        FragmentAddSource fragmentAddSource = new FragmentAddSource();
        Bundle args = new Bundle();
        args.putSerializable("save_source", sources);
        fragmentAddSource.setArguments(args);
        return fragmentAddSource;
    }

    public static ArrayList<Source> getSources() {
        return sources;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_add_source, container, false);

        Log.d(LOG_TAG, "onCreateView_fragment_add_source");

        txtCoefficient = result.findViewById(R.id.coefficient);
        activitySource = result.findViewById(R.id.activitySource);
        activitySource.setText("0");
        spinner_source = result.findViewById(R.id.spinner_source);
        coordinateSourceX = result.findViewById(R.id.coordinateSourceX);
        coordinateSourceX.setText("0");
        coordinateSourceY = result.findViewById(R.id.coordinateSourceY);
        coordinateSourceY.setText("0");
        coordinateSourceZ = result.findViewById(R.id.coordinateSourceZ);
        coordinateSourceZ.setText("0");

        recyclerView = result.findViewById(R.id.recycler_view_source);

        addSource = result.findViewById(R.id.add_source);
        addSource.setOnClickListener(this);

        spinner_source.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("Range")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                txtCoefficient.setText("");
                cursor = db.rawQuery("select * from " + Constants.TABLE_SOURCE_FACTOR + " where " + Constants.COLUMN_SOURCE_FACTOR_ID +
                        "=" + (spinner_source.getSelectedItemPosition() + 1), null);

                while (cursor.moveToNext()) {
                    txtCoefficient.append(cursor.getString(1));
                    dimension_factor = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_SOURCE_FACTOR_DIMENSION));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sources = new ArrayList<>();


        databaseHelper = new DatabaseHelper(getActivity());
        databaseHelper.create_db();
        return result;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onResume() {
        super.onResume();

        Log.d(LOG_TAG, "onResume_fragment_add_source");

        db = databaseHelper.open();

        cursor_source = db.rawQuery("select * from " + Constants.TABLE_SOURCE, null);

        String[] source = new String[]{Constants.COLUMN_NAME_SOURCE};

        SimpleCursorAdapter adapter_source = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_spinner_item,
                cursor_source, source, new int[]{android.R.id.text1}, 0);
        adapter_source.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_source.setAdapter(adapter_source);

        arrayList = new ArrayList<>();
        for (int i = 0; i < sources.size(); i++) {
            arrayList.add(String.format(Locale.ROOT,"%s" + " [%.1f - %s] - (%.1f, %.1f, %.1f)", sources.get(i).getNameSource(),
                    sources.get(i).getActivitySource(), sources.get(i).getDimension_factor(), sources.get(i).getCoordinateSourceX(),
                    sources.get(i).getCoordinateSourceY(), sources.get(i).getCoordinateSourceZ()));
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new SourceAdapter(getActivity(), arrayList, sources);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy_fragment_add_source");
        db.close();
        cursor_source.close();
        cursor.close();
    }

    @SuppressLint({"Range", "DefaultLocale"})
    @Override
    public void onClick(View v) {
        Log.d(LOG_TAG, "onClick_fragment_add_source");

        double x = Double.parseDouble(coordinateSourceX.getText().toString());
        double y = Double.parseDouble(coordinateSourceY.getText().toString());
        double z = Double.parseDouble(coordinateSourceZ.getText().toString());

        double activity = Double.parseDouble(activitySource.getText().toString());
        double coefficient = Double.parseDouble(txtCoefficient.getText().toString());

        cursor_source_add = db.rawQuery("select * from " + Constants.TABLE_SOURCE + " where " + Constants.COLUMN_ID_SOURCE +
                "=" + (spinner_source.getSelectedItemPosition() + 1), null);

        while (cursor_source_add.moveToNext()) {
            nameSource = cursor_source_add.getString(cursor_source_add.getColumnIndex(Constants.COLUMN_NAME_SOURCE));
            dimension = cursor_source_add.getString(cursor_source_add.getColumnIndex(Constants.COLUMN_SOURCE_DIMENSION));
        }

        sources.add(new Source(nameSource, dimension, coefficient, activity, x, y, z, spinner_source.getSelectedItemPosition(), dimension_factor));

        arrayList = new ArrayList<>();
        for (int i = 0; i < sources.size(); i++) {
            sources.get(i).setCoordinateSourceX(x);
            sources.get(i).setCoordinateSourceY(y);
            sources.get(i).setCoordinateSourceZ(z);
            arrayList.add(String.format(Locale.ROOT,"%s" + " [%.1f - %s] - (%.1f, %.1f, %.1f)", sources.get(i).getNameSource(),
                    sources.get(i).getActivitySource(), sources.get(i).getDimension_factor(), sources.get(i).getCoordinateSourceX(),
                    sources.get(i).getCoordinateSourceY(), sources.get(i).getCoordinateSourceZ()));
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new SourceAdapter(getActivity(), arrayList, sources);
        recyclerView.setAdapter(adapter);
    }


    //сохранение состояния
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG, "onSaveInstanceState_fragment_add_source");
    }

    //Думаю когда мы переходим в другой фрагмент, срабатывает этот метод
    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause_fragment_add_source");
    }

    public static void save_source(ArrayList<Source> arrayList){
        sources = arrayList;
    }
}
