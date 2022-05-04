package com.example.rpmsim.fragment;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rpmsim.R;
import com.example.rpmsim.database.Constants;
import com.example.rpmsim.database.DatabaseHelper;
import com.example.rpmsim.entity.Shield;
import com.example.rpmsim.recycler_view_adapter.MaterialAdapter;

import java.util.ArrayList;

public class FragmentAddMaterial extends Fragment implements View.OnClickListener {

    private static ArrayList<Shield> shields;

    private Spinner spinner_material;
    private EditText thickness;

    private RecyclerView recycler_view_shield;
    private Button add_material;

    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;
    private Cursor cursor_shield;

    ArrayList<String> arrayList = new ArrayList<>();

    public static ArrayList<Shield> getShields() {
        return shields;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_shield, container, false);

        spinner_material = result.findViewById(R.id.spinner_material);
        thickness = result.findViewById(R.id.thickness);
        thickness.setText("0");
        recycler_view_shield = result.findViewById(R.id.recycler_view_shield);
        add_material = result.findViewById(R.id.add_material);
        add_material.setOnClickListener(this);

        shields = new ArrayList<>();
        databaseHelper = new DatabaseHelper(getActivity());
        databaseHelper.create_db();
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();

        db = databaseHelper.open();

        cursor_shield = db.rawQuery("select * from " + Constants.TABLE_SHIELD, null);

        String[] detectors = new String[]{Constants.COLUMN_NAME_MATERIAL};

        SimpleCursorAdapter adapter_shield = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_spinner_item,
                cursor_shield, detectors, new int[]{android.R.id.text1}, 0);
        adapter_shield.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_material.setAdapter(adapter_shield);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
        cursor_shield.close();
    }

    @SuppressLint({"Range", "DefaultLocale"})
    @Override
    public void onClick(View v) {
        double txtThickness = Double.parseDouble(thickness.getText().toString());
        String name_shield = null;
        cursor_shield = db.rawQuery("select * from " + Constants.TABLE_SHIELD + " where " + Constants.COLUMN_ID_SHIELD +
                "=" + (spinner_material.getSelectedItemPosition() + 1), null);
        while (cursor_shield.moveToNext()) {
            name_shield = cursor_shield.getString(cursor_shield.getColumnIndex(Constants.COLUMN_NAME_MATERIAL));
            arrayList.add(String.format("%s - %.0f мм",name_shield, txtThickness));
        }
        recycler_view_shield.setLayoutManager(new LinearLayoutManager(getActivity()));
        MaterialAdapter adapter = new MaterialAdapter(getActivity(), arrayList, shields);
        recycler_view_shield.setAdapter(adapter);
        shields.add(new Shield(name_shield, txtThickness, spinner_material.getSelectedItemPosition() + 1));
    }
}
