package com.example.rpmsim.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

import com.example.rpmsim.R;
import com.example.rpmsim.database.Constants;
import com.example.rpmsim.database.DatabaseHelper;
import com.example.rpmsim.entity.Detector;
import com.example.rpmsim.fragments_not_used.FragmentAddDetector;
import com.example.rpmsim.fragment.FragmentDetector;

import java.util.ArrayList;

public class AddDetector extends AppCompatActivity implements View.OnClickListener {

    final String LOG_TAG = "myLogs";

    private EditText background, distanceX, distanceY, distanceZ;
    private Button addNewDetector;
    private ListView listView;

    private Spinner spinner_detector;
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;
    private Cursor cursor_detector;
    private Cursor cursor_source;
    private Cursor cursor_sensitivity;
    private Cursor cursor;

    private double geometric_size;
    private double detector_sensitivity;
    private String nameDetector;
    private String dimension;
    private String nameSource;

    ArrayList<Double> detector_sensitivity_array = new ArrayList<>();
    ArrayList<String> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_detector);

        Log.d(LOG_TAG, "onCreateView_fragment_add_detector");

        spinner_detector = findViewById(R.id.spinner_detector);
        background = findViewById(R.id.background);
        distanceX = findViewById(R.id.distanceX);
        distanceX.setText("0");
        distanceY = findViewById(R.id.distanceY);
        distanceY.setText("0");
        distanceZ = findViewById(R.id.distanceZ);
        distanceZ.setText("0");
        addNewDetector = findViewById(R.id.add_detector);
        addNewDetector.setOnClickListener(this);
        listView = findViewById(R.id.sensitivity);

        spinner_detector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint({"Range", "DefaultLocale"})
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                background.setText("");
                detector_sensitivity_array.clear();
                arrayList.clear();
                cursor = db.rawQuery("select * from " + Constants.TABLE_DETECTOR + " where " + Constants.COLUMN_ID_DETECTOR +
                        "=" + (spinner_detector.getSelectedItemPosition() + 1), null);

                while (cursor.moveToNext()) {
                    geometric_size = cursor.getDouble(cursor.getColumnIndex(Constants.COLUMN_GEOMETRICAL_SIZES));
                    nameDetector = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_NAME_DETECTOR));
                    background.append(cursor.getString(3));
                }

                cursor_sensitivity = db.rawQuery("select * from " + Constants.TABLE_DETECTOR_SOURCE + " where " + Constants.COLUMN_DETECTOR_SOURCE_ID +
                        "=" + (spinner_detector.getSelectedItemPosition() + 1), null);
                cursor_source = db.rawQuery("select * from " + Constants.TABLE_SOURCE, null);

                while (cursor_sensitivity.moveToNext() && cursor_source.moveToNext()) {
                    detector_sensitivity_array.add(cursor_sensitivity.getDouble(cursor_sensitivity.getColumnIndex(Constants.COLUMN_VALUE)));
                    detector_sensitivity = cursor_sensitivity.getDouble(cursor_sensitivity.getColumnIndex(Constants.COLUMN_VALUE));
                    nameSource = cursor_source.getString(cursor_source.getColumnIndex(Constants.COLUMN_NAME_SOURCE));
                    dimension = cursor_source.getString(cursor_source.getColumnIndex(Constants.COLUMN_SOURCE_DIMENSION));
                    arrayList.add(String.format("%-10.0f [%s - %s]", detector_sensitivity, nameSource, dimension));
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplication(), android.R.layout.simple_list_item_1, arrayList);
                listView.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        databaseHelper = new DatabaseHelper(getApplication());
        databaseHelper.create_db();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    @SuppressLint("Range")
    @Override
    public void onResume() {
        super.onResume();

        Log.d(LOG_TAG, "onResume_fragment_add_detector");

        db = databaseHelper.open();

        cursor_detector = db.rawQuery("select * from " + Constants.TABLE_DETECTOR, null);

        String[] detectors = new String[]{Constants.COLUMN_NAME_DETECTOR};

        SimpleCursorAdapter adapter_detector = new SimpleCursorAdapter(getApplication(),
                android.R.layout.simple_spinner_item,
                cursor_detector, detectors, new int[]{android.R.id.text1}, 0);
        adapter_detector.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_detector.setAdapter(adapter_detector);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy_fragment_add_detector");
        db.close();
        cursor_detector.close();
        cursor.close();
    }

    @Override
    public void onClick(View v) {
        double x = Double.parseDouble(distanceX.getText().toString());
        double y = Double.parseDouble(distanceY.getText().toString());
        double z = Double.parseDouble(distanceZ.getText().toString());
        double backgroundDetector = Double.parseDouble(background.getText().toString());
        int position = spinner_detector.getSelectedItemPosition();

        Detector detector = new Detector(nameDetector, detector_sensitivity_array, x, y, z, geometric_size, backgroundDetector, position);

        FragmentDetector.add_detectors(detector);
        this.finish();
    }

    public static FragmentAddDetector newInstance(ArrayList<Detector> detectors) {
        FragmentAddDetector fragmentAddDetector = new FragmentAddDetector();
        Bundle args = new Bundle();
        args.putSerializable("detectors_from_add", detectors);
        fragmentAddDetector.setArguments(args);
        return fragmentAddDetector;
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
