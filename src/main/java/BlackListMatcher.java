import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * <h1>Blacklist Name Matching</h1>
 * This is algorithm to compare given name against blacklist to detect such
 * transfers.
 * Unit tests can be found from test folder.
 *
 * @author Tanel Pikkel
 * @version 1.0
 * @since 2018-01-20
 */
public class BlackListMatcher {

    /**
     * In case of partial match - sensitivity is set by this percentage.
     * Smaller number will return more matches.
     */
    private static final int PARTIAL_MATCHING_PERCENTAGE = 70;

    /**
     * This method is used to find matches from blacklist.
     *
     * @param name       Name to validate against blacklist.
     * @param names_file Input file name that contains one blacklisted name per line
     * @param noise_file Input file name that contains one noise word per line
     * @return List<String> Returns list of names that have positive matches in blacklist. In case of error returns null.
     */
    public List<String> searchFromBlackList(String name, String names_file, String noise_file) {
        if (name == null || names_file == null) return null;
        List<String> noiseWordList = new ArrayList<>();
        List<String> results = new ArrayList<>();
        prepareNoiseWordList(noise_file, noiseWordList);
        getStreamFromFile(names_file).forEach(item -> compare(name, noiseWordList, results, item));
        return results;
    }

    /**
     * This method reads noise words from file and adds em to noiseWordList.
     *
     * @param noiseWordList Empty list where to put noise words read from noise_file.
     * @param noise_file    The name of file of noise words.
     */
    private void prepareNoiseWordList(String noise_file, List<String> noiseWordList) {
        if (noise_file != null)
            noiseWordList.addAll(Objects.requireNonNull(getStreamFromFile(noise_file)).collect(toList()));
    }

    /**
     * The comparing facade.
     *
     * @param name          Name to compare against black list.
     * @param noiseWordList List of noise words.
     * @param results       Empty list for results.
     * @param originalItem  Current row from black list file.
     */
    private void compare(String name, List<String> noiseWordList, List<String> results, String originalItem) {
        if (compareInDiffLanguages(name, originalItem)) {
            results.add(originalItem);
            return;
        }
        name = prepareString(name);
        name = removeNoise(name, noiseWordList);
        String item = prepareString(originalItem);
        item = removeNoise(item, noiseWordList);
        if (item.equals(name)
                //|| compareWords(name, item)  <--Deprecated
                || FuzzySearch.tokenSortPartialRatio(name, item) > PARTIAL_MATCHING_PERCENTAGE
                ) {
            results.add(originalItem);
        }
    }

    /**
     * This method converts strings into lowercase, removes everything that is not a character and trims.
     *
     * @param string The string to be converted.
     * @return String Returns input string in lowercase, trimmed and symbols are removed.
     */
    private String prepareString(String string) {
        return string
                .toLowerCase()
                .replaceAll("[^a-z\\s]", "")
                .replaceAll(" +", " ")
                .trim();
    }

    /**
     * This method converts strings into lowercase, trims and removes the noise words.
     *
     * @param string        The name to be converted.
     * @param noiseWordList Noise words as a list.
     * @return String Returns input string in lowercase, trimmed and noise words are removed.
     */
    private String removeNoise(String string, List<String> noiseWordList) {
        return noiseWordList.stream()
                .reduce(string, (str, toRem) -> str.replaceAll(
                        "(?<![a-z])" + toRem.toLowerCase() + "(?![a-z])", "")).trim();
    }

    /**
     * This method is deprecated and is not deleted for backup functionality purpose.
     *
     * @param name The inserted name.
     * @param item Name from black list to compare with.
     * @return Boolean Returns true if there is a match.
     */
    @Deprecated
    private boolean compareWords(String name, String item) {
        if (name.contains(item)) return true;
        if (item.contains(name)) return true;
        List<String> nameAsList = new ArrayList<>(Arrays.asList(name.split("\\s+")));
        List<String> itemAsList = new ArrayList<>(Arrays.asList(item.split("\\s+")));
        List<String> common = nameAsList.stream().filter(itemAsList::contains).collect(toList());
        double maxLenght = Math.max(nameAsList.size(), itemAsList.size());
        double minLenght = Math.min(nameAsList.size(), itemAsList.size());
        return common.size() >= minLenght || common.size() >= maxLenght / 2;
    }

    /**
     * This method is used to get data from file as Stream of Strings.
     *
     * @param file_name The name of file to read.
     * @return Stream<String> Returns the text file as a Stream of Strings.
     */
    private Stream<String> getStreamFromFile(String file_name) {
        try {
            return Files.lines(Paths.get(Objects.requireNonNull(getClass().getClassLoader()
                    .getResource(file_name)).toURI()));
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method is used to compare strings in different locales.
     *
     * @param name The name inserted into search.
     * @param item Current name(row) from blacklist.
     * @return boolean Returns true if names match.
     */
    private boolean compareInDiffLanguages(String name, String item) {
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        return collator.compare(name.toLowerCase(), item.toLowerCase()) == 0;
    }

}
