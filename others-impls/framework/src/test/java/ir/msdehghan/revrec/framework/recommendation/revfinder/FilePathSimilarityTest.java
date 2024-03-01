package ir.msdehghan.revrec.framework.recommendation.revfinder;

import org.junit.jupiter.api.Test;

import static ir.msdehghan.revrec.framework.recommendation.revfinder.FilePathSimilarity.*;
import static org.junit.jupiter.api.Assertions.*;

class FilePathSimilarityTest {
    private final String f1 = "sb-tools/sb-maven-plugin/src/it/jar-scopes-filering/verify.groovy";
    private final String f2 = "sb-tools/sb-maven-plugin/src/test/java/org/springframework/boot/maven/Verify.java";
    private final String f3 = ".gitignore";
    private final String f4 = "sb/src/main/java/org/springframework/boot/BeanDefinitionLoader.java";
    private final String f5 = "sb/src/test/org/boot/application.properties";
    private final String f6 = "sb/src/main/java/com/springframework/boot/BeanDefinitionLoader.java";

    @Test
    void testLongestCommonSubsequence() {
        assertEquals(3.0 / 10, longestCommonSubsequence(f1, f2));
        assertEquals(7.0/ 8, longestCommonSubsequence(f4, f6));
        assertEquals(4.0/ 8, longestCommonSubsequence(f4, f5));
        assertEquals(1.0/ 8.0, longestCommonSubsequence(f1, f4));
        assertEquals(0.0, longestCommonSubsequence(f3, f4));
        assertEquals(1.0, longestCommonSubsequence(f3, f3));
    }

    @Test
    void testLongestCommonSubstring() {
        assertEquals(3.0 / 10, longestCommonSubstring(f1, f2));
        assertEquals(4.0/ 8, longestCommonSubstring(f4, f6));
        assertEquals(1.0/ 8, longestCommonSubstring(f1, f4));
        assertEquals(0.0, longestCommonSubstring(f3, f4));
        assertEquals(1.0, longestCommonSubstring(f3, f3));
    }

    @Test
    void testLongestCommonPrefix() {
        assertEquals(3.0/ 10, longestCommonPrefix(f1, f2));
        assertEquals(4.0/ 8, longestCommonPrefix(f4, f6));
        assertEquals(0.0, longestCommonPrefix(f1, f4));
        assertEquals(0.0, longestCommonPrefix(f3, f4));
        assertEquals(1.0, longestCommonPrefix(f3, f3));
    }

    @Test
    void testLongestCommonSuffix() {
        assertEquals(0.0, longestCommonSuffix(f1, f2));
        assertEquals(3.0/ 8, longestCommonSuffix(f4, f6));
        assertEquals(0.0, longestCommonSuffix(f1, f4));
        assertEquals(0.0, longestCommonSuffix(f3, f4));
        assertEquals(1.0, longestCommonSuffix(f3, f3));
    }
}