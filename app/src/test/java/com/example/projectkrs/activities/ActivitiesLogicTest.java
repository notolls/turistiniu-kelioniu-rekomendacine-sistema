package com.example.projectkrs.activities;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ActivitiesLogicTest {

    @Test
    public void buildCategoryTypesMap_returnsAllExpectedEntries() {
        Map<String, String> map = RegisterActivity.buildCategoryTypesMap();

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
    public void buildCategoryTypesMap_returnsLinkedHashMap() {
        Map<String, String> map = RegisterActivity.buildCategoryTypesMap();

        assertTrue(map instanceof LinkedHashMap);
    }

    @Test
    public void buildCategoryTypesMap_preservesExpectedOrder() {
        Map<String, String> map = RegisterActivity.buildCategoryTypesMap();

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
    public void buildCategoryTypesMap_returnsNewIndependentMapEveryTime() {
        Map<String, String> first = RegisterActivity.buildCategoryTypesMap();
        Map<String, String> second = RegisterActivity.buildCategoryTypesMap();

        assertNotSame(first, second);
        first.put("Testinė", "test");

        assertFalse(second.containsKey("Testinė"));
        assertEquals(8, second.size());
    }

    @Test
    public void resolveCategoryType_returnsMappedValue_forKnownLabel() {
        Map<String, String> map = RegisterActivity.buildCategoryTypesMap();

        String result = RegisterActivity.resolveCategoryType(map, "Parkai");

        assertEquals("park", result);
    }

    @Test
    public void resolveCategoryType_returnsNull_forUnknownLabel() {
        Map<String, String> map = RegisterActivity.buildCategoryTypesMap();

        String result = RegisterActivity.resolveCategoryType(map, "Nežinoma kategorija");

        assertNull(result);
    }

    @Test
    public void resolveCategoryType_returnsNull_whenMapIsNull() {
        String result = RegisterActivity.resolveCategoryType(null, "Parkai");

        assertNull(result);
    }

    @Test
    public void resolveCategoryType_returnsNull_whenSelectedLabelIsNull() {
        Map<String, String> map = RegisterActivity.buildCategoryTypesMap();

        String result = RegisterActivity.resolveCategoryType(map, null);

        assertNull(result);
    }

    @Test
    public void resolveCategoryType_returnsNull_whenSelectedLabelIsEmpty() {
        Map<String, String> map = RegisterActivity.buildCategoryTypesMap();

        String result = RegisterActivity.resolveCategoryType(map, "");

        assertNull(result);
    }

    @Test
    public void resolveCategoryType_supportsCustomMap() {
        Map<String, String> customMap = new LinkedHashMap<>();
        customMap.put("Custom", "custom_value");

        String result = RegisterActivity.resolveCategoryType(customMap, "Custom");

        assertEquals("custom_value", result);
    }

    @Test
    public void quizQuestionConstructor_setsAllFields() {
        String[] answers = new String[]{"A", "B", "C"};

        QuizActivity.Question question = new QuizActivity.Question("Klausimas", answers, 2);

        assertEquals("Klausimas", question.question);
        assertArrayEquals(answers, question.answers);
        assertEquals(2, question.correctAnswerIndex);
    }

    @Test
    public void quizQuestionConstructor_allowsNullQuestionText() {
        QuizActivity.Question question = new QuizActivity.Question(null, new String[]{"A"}, 0);

        assertNull(question.question);
        assertNotNull(question.answers);
        assertEquals(0, question.correctAnswerIndex);
    }

    @Test
    public void quizQuestionConstructor_allowsNullAnswersArray() {
        QuizActivity.Question question = new QuizActivity.Question("Klausimas", null, 0);

        assertEquals("Klausimas", question.question);
        assertNull(question.answers);
        assertEquals(0, question.correctAnswerIndex);
    }

    @Test
    public void quizQuestionConstructor_allowsNegativeCorrectAnswerIndex() {
        QuizActivity.Question question = new QuizActivity.Question("Klausimas", new String[]{"A"}, -1);

        assertEquals(-1, question.correctAnswerIndex);
    }

    @Test
    public void quizQuestionConstructor_allowsLargeCorrectAnswerIndex() {
        QuizActivity.Question question = new QuizActivity.Question("Klausimas", new String[]{"A", "B"}, 99);

        assertEquals(99, question.correctAnswerIndex);
    }

    @Test
    public void quizQuestionConstructor_keepsSameAnswersReference() {
        String[] answers = new String[]{"A", "B"};

        QuizActivity.Question question = new QuizActivity.Question("Klausimas", answers, 1);

        assertSame(answers, question.answers);
    }

    @Test
    public void quizQuestionAnswersReflectExternalMutations() {
        String[] answers = new String[]{"A", "B"};
        QuizActivity.Question question = new QuizActivity.Question("Klausimas", answers, 1);

        answers[0] = "Pakeista";

        assertEquals("Pakeista", question.answers[0]);
    }
}
