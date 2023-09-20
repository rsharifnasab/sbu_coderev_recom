package ir.msdehghan.revrec.framework.recommendation.revfinder;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

public final class FilePathSimilarity {

    public static final LoadingCache<String, String[]> cache = Caffeine.newBuilder()
            .maximumSize(200_000)
            .initialCapacity(200_000)
            .recordStats()
            .build(p -> p.split("/"));

//    public static final Cache<UnOrderedStringPair, Double> substringCache = Caffeine.newBuilder()
//            .maximumSize(200_000_000)
//            .initialCapacity(200_000_000)
//            .recordStats()
//            .build();
//
//    public static final Cache<UnOrderedStringPair, Double> subsequenceCache = Caffeine.newBuilder()
//            .maximumSize(200_000_000)
//            .initialCapacity(200_000_000)
//            .recordStats()
//            .build();

    private record UnOrderedStringPair(String a, String b) {
        UnOrderedStringPair {
            if (a.compareTo(b) < 0) {
                String tmp = a;
                a = b;
                b = tmp;
            }
        }
    }

    private FilePathSimilarity() {
        // This class is a utility class.
    }

    public static void clearCache() {
        cache.invalidateAll();
//        subsequenceCache.invalidateAll();
//        substringCache.invalidateAll();
    }

    /**
     * Find the longest common prefix of two strings.
     *
     * @param path1 first string.
     * @param path2 second string.
     * @return longest common prefix in the file paths of path1 and path2.
     */
    public static double longestCommonPrefix(String path1, String path2) {
        String[] path1Array = cache.get(path1);
        String[] path2Array = cache.get(path2);
        int counter = 0;

        final var min = Math.min(path1Array.length, path2Array.length);
        for (; counter < min; counter++) {
            if (!path1Array[counter].equals(path2Array[counter])) {
                break;
            }
        }
        return normalize(counter, path1Array.length, path2Array.length);
    }

    /**
     * Find the longest common suffix of two strings.
     *
     * @param path1 first string.
     * @param path2 second string.
     * @return longest common suffix in the file paths of path1 and path2.
     */
    public static double longestCommonSuffix(String path1, String path2) {
        String[] path1Array = cache.get(path1);
        String[] path2Array = cache.get(path2);

        int result = 0;

        final var min = Math.min(path1Array.length, path2Array.length);
        for (; result < min; result++) {
            if (!path1Array[path1Array.length - result - 1].equals(path2Array[path2Array.length - result - 1])) {
                break;
            }
        }
        return normalize(result, path1Array.length, path2Array.length);
    }

    /**
     * Find the longest common substring of two strings.
     *
     * @param path1 first string.
     * @param path2 second string.
     * @return longest common substring in the file paths of path1 and path2.
     */
    public static double longestCommonSubstring(String path1, String path2) {
//        UnOrderedStringPair pair = new UnOrderedStringPair(path1, path2);
//        Double result = substringCache.getIfPresent(pair);
//        if (result != null) {
//            return result;
//        }

        String[] path1Array = cache.get(path1);
        String[] path2Array = cache.get(path2);
        int m = path1Array.length;
        int n = path2Array.length;

        int maxValue = 0;
        int[][] dpMatrix = new int[m][n];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (path1Array[i].equals(path2Array[j])) {
                    if (i == 0 || j == 0) {
                        dpMatrix[i][j] = 1;
                    } else {
                        dpMatrix[i][j] = dpMatrix[i - 1][j - 1] + 1;
                    }

                    if (maxValue < dpMatrix[i][j]) {
                        maxValue = dpMatrix[i][j];
                    }
                }

            }
        }

        double normalized = normalize(maxValue, path1Array.length, path2Array.length);
//        substringCache.put(pair, normalized);
        return normalized;
    }

    /**
     * Find the longest common subsequence of two strings.
     *
     * @param path1 first string.
     * @param path2 second string.
     * @return longest common subsequence in the file paths of path1 and path2.
     */
    public static double longestCommonSubsequence(String path1, String path2) {
//        UnOrderedStringPair pair = new UnOrderedStringPair(path1, path2);
//        Double result = subsequenceCache.getIfPresent(pair);
//        if (result != null) {
//            return result;
//        }

        String[] path1Array = cache.get(path1);
        String[] path2Array = cache.get(path2);

        int m = path1Array.length;
        int n = path2Array.length;

        int[][] dpMatrix = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) {
            for (int j = 0; j <= n; j++) {
                if (i == 0 || j == 0) {
                    dpMatrix[i][j] = 0;
                } else if (path1Array[i - 1].equals(path2Array[j - 1])) {
                    dpMatrix[i][j] = 1 + dpMatrix[i - 1][j - 1];
                } else {
                    dpMatrix[i][j] = Math.max(dpMatrix[i - 1][j], dpMatrix[i][j - 1]);
                }
            }
        }

        double normalized = normalize(dpMatrix[m][n], path1Array.length, path2Array.length);
//        subsequenceCache.put(pair, normalized);
        return normalized;
    }

    private static double normalize(int score, int f1Parts, int f2Parts) {
        return score / (double) (Math.max(f1Parts, f2Parts));
    }
}
