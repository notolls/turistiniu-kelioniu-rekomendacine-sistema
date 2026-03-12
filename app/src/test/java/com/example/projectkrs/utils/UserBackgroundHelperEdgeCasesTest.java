package com.example.projectkrs.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserBackgroundHelperEdgeCasesTest {

    @Test
    public void hasUsableBackgroundSelection_returnsFalse_forSingleSpace() {
        assertFalse(UserBackgroundHelper.hasUsableBackgroundSelection(" "));
    }

    @Test
    public void hasUsableBackgroundSelection_returnsFalse_forManySpaces() {
        assertFalse(UserBackgroundHelper.hasUsableBackgroundSelection("          "));
    }

    @Test
    public void hasUsableBackgroundSelection_returnsFalse_forSingleTab() {
        assertFalse(UserBackgroundHelper.hasUsableBackgroundSelection("\t"));
    }

    @Test
    public void hasUsableBackgroundSelection_returnsFalse_forSingleNewline() {
        assertFalse(UserBackgroundHelper.hasUsableBackgroundSelection("\n"));
    }

    @Test
    public void hasUsableBackgroundSelection_returnsFalse_forCarriageReturnAndSpaces() {
        assertFalse(UserBackgroundHelper.hasUsableBackgroundSelection("\r   \r"));
    }

    @Test
    public void hasUsableBackgroundSelection_returnsFalse_forMixedWhitespace() {
        assertFalse(UserBackgroundHelper.hasUsableBackgroundSelection(" \n \t \r "));
    }

    @Test
    public void hasUsableBackgroundSelection_returnsTrue_forLowercaseName() {
        assertTrue(UserBackgroundHelper.hasUsableBackgroundSelection("bg_blue"));
    }

    @Test
    public void hasUsableBackgroundSelection_returnsTrue_forUppercaseName() {
        assertTrue(UserBackgroundHelper.hasUsableBackgroundSelection("BG_BLUE"));
    }

    @Test
    public void hasUsableBackgroundSelection_returnsTrue_forNameWithNumbers() {
        assertTrue(UserBackgroundHelper.hasUsableBackgroundSelection("bg_2024"));
    }

    @Test
    public void hasUsableBackgroundSelection_returnsTrue_forNameWithDashes() {
        assertTrue(UserBackgroundHelper.hasUsableBackgroundSelection("bg-blue-v2"));
    }

    @Test
    public void hasUsableBackgroundSelection_returnsTrue_forUnicodeText() {
        assertTrue(UserBackgroundHelper.hasUsableBackgroundSelection("fonas_žalias"));
    }

    @Test
    public void hasUsableBackgroundSelection_returnsTrue_forEmojiText() {
        assertTrue(UserBackgroundHelper.hasUsableBackgroundSelection("bg_sun_☀️"));
    }

    @Test
    public void hasUsableBackgroundSelection_returnsTrue_whenWrappedInWhitespace() {
        assertTrue(UserBackgroundHelper.hasUsableBackgroundSelection(" \n bg_purple \t "));
    }

    @Test
    public void hasUsableBackgroundSelection_returnsTrue_forVeryLongInput() {
        String value = "bg_" + "a".repeat(500);
        assertTrue(UserBackgroundHelper.hasUsableBackgroundSelection(value));
    }

    @Test
    public void hasUsableBackgroundSelection_returnsFalse_forWhitespaceOnlyLongInput() {
        String value = " \t\n".repeat(200);
        assertFalse(UserBackgroundHelper.hasUsableBackgroundSelection(value));
    }

    @Test
    public void isValidDrawableResource_returnsFalse_forZero() {
        assertFalse(UserBackgroundHelper.isValidDrawableResource(0));
    }

    @Test
    public void isValidDrawableResource_returnsTrue_forOne() {
        assertTrue(UserBackgroundHelper.isValidDrawableResource(1));
    }

    @Test
    public void isValidDrawableResource_returnsTrue_forLargePositiveId() {
        assertTrue(UserBackgroundHelper.isValidDrawableResource(Integer.MAX_VALUE));
    }

    @Test
    public void isValidDrawableResource_returnsTrue_forNegativeOne() {
        assertTrue(UserBackgroundHelper.isValidDrawableResource(-1));
    }

    @Test
    public void isValidDrawableResource_returnsTrue_forLargeNegativeId() {
        assertTrue(UserBackgroundHelper.isValidDrawableResource(Integer.MIN_VALUE));
    }

    @Test
    public void combinedValidation_acceptsTypicalValidPair() {
        String drawableName = "bg_blue";
        int resId = 123;

        assertTrue(UserBackgroundHelper.hasUsableBackgroundSelection(drawableName));
        assertTrue(UserBackgroundHelper.isValidDrawableResource(resId));
    }

    @Test
    public void combinedValidation_rejectsMissingNameEvenWithValidResource() {
        String drawableName = "   ";
        int resId = 123;

        assertFalse(UserBackgroundHelper.hasUsableBackgroundSelection(drawableName));
        assertTrue(UserBackgroundHelper.isValidDrawableResource(resId));
    }

    @Test
    public void combinedValidation_rejectsZeroResourceEvenWithValidName() {
        String drawableName = "bg_blue";
        int resId = 0;

        assertTrue(UserBackgroundHelper.hasUsableBackgroundSelection(drawableName));
        assertFalse(UserBackgroundHelper.isValidDrawableResource(resId));
    }

    @Test
    public void combinedValidation_rejectsBothInvalidValues() {
        String drawableName = "\t\n";
        int resId = 0;

        assertFalse(UserBackgroundHelper.hasUsableBackgroundSelection(drawableName));
        assertFalse(UserBackgroundHelper.isValidDrawableResource(resId));
    }
}
