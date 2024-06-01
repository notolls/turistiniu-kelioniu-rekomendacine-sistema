package com.example.projectkrs.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.projectkrs.R;
import com.example.projectkrs.fragments.HomeFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OptionsDialog extends AlertDialog {

    private SeekBar seekBarRadius;
    private TextView progressTextView;
    private Spinner categorySpinner;
    private Map<String, String> categoryTypesMap;
    private String selectedCategory;

    private HomeFragment homeFragment;

    public OptionsDialog(Context context, HomeFragment homeFragment) {
        super(context);
        this.homeFragment = homeFragment;
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_select_options, null);

        seekBarRadius = dialogView.findViewById(R.id.radiusSeekBar);
        progressTextView = dialogView.findViewById(R.id.progressTextView);
        categorySpinner = dialogView.findViewById(R.id.categorySpinner);

        seekBarRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressTextView.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        // kategorijų pasirinkimo hashmap

        categoryTypesMap = new HashMap<>();
        categoryTypesMap.put("Kavinės", "cafe");
        categoryTypesMap.put("Restoranai", "restaurant");
        categoryTypesMap.put("Muziejai", "museum");
        categoryTypesMap.put("Autobusų stotys","bus_station");
        categoryTypesMap.put("Traukinių stotys","train_station");
        categoryTypesMap.put("Parkai", "park");
        categoryTypesMap.put("Prekybos centrai", "shopping_mall");
        categoryTypesMap.put("Bendros lankytinos vietos","tourist_attraction");

        List<String> categories = new ArrayList<>(categoryTypesMap.keySet());
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedCategory = categoryTypesMap.get(categories.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Nieko nedaryti
            }
        });

        setView(dialogView);

        Button saveButton = dialogView.findViewById(R.id.buttonSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSelectedValues();
                homeFragment.handleOptionsDialogResult(getSelectedRadius(), getSelectedCategory());
                dismiss();
            }
        });

        Button backButton = dialogView.findViewById(R.id.buttonBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void updateSelectedValues() {
        int selectedRadius = seekBarRadius.getProgress();
        Log.d("OptionsDialog", "Selected Radius: " + selectedRadius);
        selectedCategory = categoryTypesMap.get(categorySpinner.getSelectedItem().toString());
        Log.d("OptionsDialog", "Selected Category: " + selectedCategory);
    }

    private int getSelectedRadius() {
        return seekBarRadius.getProgress();
    }

    private String getSelectedCategory() {
        return selectedCategory;
    }
}
