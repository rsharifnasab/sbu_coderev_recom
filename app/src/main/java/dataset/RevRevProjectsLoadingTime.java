package dataset;

import java.util.Map;

public class RevRevProjectsLoadingTime {
    public static final Map<String, Integer> PROJECTS_TIME_TO_LOAD = Map.ofEntries(
            Map.entry("typo3", 17), // 2 2 1
//            Map.entry("kubernetes", 17),
            Map.entry("spark", 17),
            Map.entry("tensorflow", 17),
            Map.entry("moby", 17),
            Map.entry("angular", 17),
            Map.entry("swift", 17),
            Map.entry("bitcoin", 17),
            Map.entry("opencv", 17), // Average 89
            Map.entry("django", 17), // Average 93
            Map.entry("openstack", 10), // Old
//            Map.entry("android", 8)
            Map.entry("homebrew-core", 17), // Old
            Map.entry("react", 16),
            Map.entry("react-native", 17),
            Map.entry("libreoffice", 15),
            Map.entry("go", 9),
            Map.entry("threejs", 13),
            Map.entry("gerrit", 7)
    );
}
