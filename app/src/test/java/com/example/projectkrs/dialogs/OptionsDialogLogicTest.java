package com.example.projectkrs.dialogs;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class OptionsDialogLogicTest {

    @Test
    public void formatRadiusProgress_formatsPositiveValue() {
        assertEquals("25", OptionsDialog.formatRadiusProgress(25));
    }

    @Test
    public void formatRadiusProgress_formatsZeroValue() {
        assertEquals("0", OptionsDialog.formatRadiusProgress(0));
    }

    @Test
    public void formatRadiusProgress_formatsNegativeValue() {
        assertEquals("-7", OptionsDialog.formatRadiusProgress(-7));
    }

    @Test
    public void buildCategoryTypesMap_containsAllExpectedEntries() {
        Map<String, String> map = OptionsDialog.buildCategoryTypesMap();

        assertEquals(8, map.size());
        assertEquals("cafe", map.get("Kavinės"));
        assertEquals("restaurant", map.get("Restoranai"));
        assertEquals("museum", map.get("Muziejai"));
        assertEquals("bus_station", map.get("Autobusų stotys"));
        assertEquals("train_station", map.get("Traukinių stotys"));
        assertEquals("park", map.get("Parkai"));
        assertEquals("shopping_mall", map.get("Prekybos centrai"));
        assertEquals("tourist_attraction", map.get("Bendros lankytinos vietos"));
    }

    @Test
    public void buildCategoryTypesMap_returnsLinkedHashMapToPreserveOrder() {
        Map<String, String> map = OptionsDialog.buildCategoryTypesMap();

        assertTrue(map instanceof LinkedHashMap);
    }

    @Test
    public void buildCategoryTypesMap_preservesExpectedDisplayOrder() {
        Map<String, String> map = OptionsDialog.buildCategoryTypesMap();
        List<String> expectedOrder = Arrays.asList(
                "Kavinės",
                "Restoranai",
                "Muziejai",
                "Autobusų stotys",
                "Traukinių stotys",
                "Parkai",
                "Prekybos centrai",
                "Bendros lankytinos vietos"
        );

        assertEquals(expectedOrder, new ArrayList<>(map.keySet()));
    }

    @Test
    public void buildCategoryTypesMap_returnsNewMapInstanceEveryCall() {
        Map<String, String> first = OptionsDialog.buildCategoryTypesMap();
        Map<String, String> second = OptionsDialog.buildCategoryTypesMap();

        assertNotSame(first, second);
        first.put("Test", "test_value");

        assertFalse(second.containsKey("Test"));
        assertEquals(8, second.size());
    }

    @Test
    public void resolveCategoryType_returnsMappedValue_forKnownCategory() {
        Map<String, String> map = OptionsDialog.buildCategoryTypesMap();

        assertEquals("park", OptionsDialog.resolveCategoryType(map, "Parkai"));
    }

    @Test
    public void resolveCategoryType_returnsNull_forUnknownCategory() {
        Map<String, String> map = OptionsDialog.buildCategoryTypesMap();

        assertNull(OptionsDialog.resolveCategoryType(map, "Nežinoma"));
    }

    @Test
    public void resolveCategoryType_returnsNull_whenMapIsNull() {
        assertNull(OptionsDialog.resolveCategoryType(null, "Parkai"));
    }

    @Test
    public void resolveCategoryType_returnsNull_whenCategoryIsNull() {
        Map<String, String> map = OptionsDialog.buildCategoryTypesMap();

        assertNull(OptionsDialog.resolveCategoryType(map, null));
    }

    @Test
    public void resolveCategoryType_supportsCustomMapValues() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("Custom one", "custom_1");
        map.put("Custom two", "custom_2");

        assertEquals("custom_2", OptionsDialog.resolveCategoryType(map, "Custom two"));
    }
}
