package com.example.projectkrs.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserBackgroundHelperTest {

    @Test
    public void hasUsableBackgroundSelection_returnsFalse_whenValueIsNull() {
        assertFalse(UserBackgroundHelper.hasUsableBackgroundSelection(null));
    }

    @Test
    public void hasUsableBackgroundSelection_returnsFalse_whenValueIsEmpty() {
        assertFalse(UserBackgroundHelper.hasUsableBackgroundSelection(""));
    }

    @Test
    public void hasUsableBackgroundSelection_returnsFalse_whenValueContainsOnlyWhitespace() {
        assertFalse(UserBackgroundHelper.hasUsableBackgroundSelection("   \n\t  "));
    }

    @Test
    public void hasUsableBackgroundSelection_returnsTrue_whenValueContainsText() {
        assertTrue(UserBackgroundHelper.hasUsableBackgroundSelection("bg_blue"));
    }

    @Test
    public void hasUsableBackgroundSelection_returnsTrue_whenValueContainsTextWithSpacesAround() {
        assertTrue(UserBackgroundHelper.hasUsableBackgroundSelection("  bg_pink  "));
    }

    @Test
    public void isValidDrawableResource_returnsFalse_whenResourceIdIsZero() {
        assertFalse(UserBackgroundHelper.isValidDrawableResource(0));
    }

    @Test
    public void isValidDrawableResource_returnsTrue_whenResourceIdIsPositive() {
        assertTrue(UserBackgroundHelper.isValidDrawableResource(123));
    }

    @Test
    public void isValidDrawableResource_returnsTrue_whenResourceIdIsNegative() {
        assertTrue(UserBackgroundHelper.isValidDrawableResource(-1));
    }
}
