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
import java.util.LinkedHashMap;
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
                progressTextView.setText(formatRadiusProgress(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        // kategorijų pasirinkimo hashmap

        categoryTypesMap = buildCategoryTypesMap();

        List<String> categories = new ArrayList<>(categoryTypesMap.keySet());
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedCategory = resolveCategoryType(categoryTypesMap, categories.get(position));
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


    static String formatRadiusProgress(int progress) {
        return String.valueOf(progress);
    }

    static Map<String, String> buildCategoryTypesMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("Kavinės", "cafe");
        map.put("Restoranai", "restaurant");
        map.put("Muziejai", "museum");
        map.put("Autobusų stotys", "bus_station");
        map.put("Traukinių stotys", "train_station");
        map.put("Parkai", "park");
        map.put("Prekybos centrai", "shopping_mall");
        map.put("Bendros lankytinos vietos", "tourist_attraction");
        return map;
    }

    static String resolveCategoryType(Map<String, String> categoryTypesMap, String categoryLabel) {
        if (categoryTypesMap == null || categoryLabel == null) {
            return null;
        }
        return categoryTypesMap.get(categoryLabel);
    }

    private void updateSelectedValues() {
        int selectedRadius = seekBarRadius.getProgress();
        Log.d("OptionsDialog", "Selected Radius: " + selectedRadius);
        selectedCategory = resolveCategoryType(categoryTypesMap, categorySpinner.getSelectedItem().toString());
        Log.d("OptionsDialog", "Selected Category: " + selectedCategory);
    }

    private int getSelectedRadius() {
        return seekBarRadius.getProgress();
    }

    private String getSelectedCategory() {
        return selectedCategory;
    }
}
