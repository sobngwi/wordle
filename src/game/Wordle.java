package game;

import java.util.*;
import java.util.stream.Collectors;

import static game.MatchLetter.*;

public class Wordle {

    public static List<MatchLetter> evaluate(String target, String guess) {
        validateParameters(target, "Target");
        validateParameters(guess, "Guess");

        var exactMatchAll = new MatchLetter[]{EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH};
        if (guess.equals(target))
            return Arrays.stream(exactMatchAll).toList();

        var results = new MatchLetter[]{NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH};
        var targets = target.toCharArray();
        var guesses = guess.toCharArray();
        var guessesPositions = computePositions(guesses);
        var targetsPositions = computePositions(targets);
        var guessesInfo = populateCharacters(guessesPositions);
        var targetsInfo = populateCharacters(targetsPositions);

        guessesInfo.forEach((infoCharacter) -> computeTheStatusForTheGivenInfoCharacter(infoCharacter, targetsInfo, results));

        return Arrays.stream(results).toList();
    }

    private static void computeTheStatusForTheGivenInfoCharacter(InfoCharacter guestInfoCharacter, List<InfoCharacter> targetsInfo, MatchLetter[] results) {
        InfoCharacter targetInfoCharacter = searchForTargetCharacter(guestInfoCharacter, targetsInfo);
        boolean isTargetCharacterMatch = targetInfoCharacter != null;
        var guestPositions = guestInfoCharacter.getPositions();
        List<Integer> targetPositions = (isTargetCharacterMatch) ? targetInfoCharacter.getPositions() : null;
        guestPositions.reversed().stream()
                .filter(p -> isTargetCharacterMatch)
                .forEach(position -> {
                    checkIfTheGuessCharacterIsExactMatch(guestInfoCharacter, targetInfoCharacter, position, targetPositions, results);
                    checkIfTheGuessCharacterIsPartialMatch(guestInfoCharacter, targetInfoCharacter, position, guestPositions, results);
                });
    }

    private static void checkIfTheGuessCharacterIsExactMatch(InfoCharacter guestInfoCharacter, InfoCharacter targetInfoCharacter, int guestPosition, List<Integer> targetPositions, MatchLetter[] results) {
        if (guestInfoCharacter.getNbOccurrences() > 0 && targetPositions.contains(guestPosition)) {
            setResultStatusAtPosition(guestPosition, EXACT_MATCH, results);
            decrementNbOccurrencesOf(guestInfoCharacter);
            decrementNbOccurrencesOf(targetInfoCharacter);
        }
    }

    private static void checkIfTheGuessCharacterIsPartialMatch(InfoCharacter guestInfoCharacter, InfoCharacter targetInfoCharacter, int guestPosition, List<Integer> guestPositions, MatchLetter[] results) {
        if (guestInfoCharacter.getNbOccurrences() > 0 && targetInfoCharacter.getNbOccurrences() > 0) {
            if (results[guestPositions.getFirst()] == PARTIAL_MATCH)
                setResultStatusAtPosition(guestPosition, PARTIAL_MATCH, results);
            else
                setResultStatusAtPosition(guestPositions.getFirst(), PARTIAL_MATCH, results);

            if ((results[guestPosition] == PARTIAL_MATCH)) {
                decrementNbOccurrencesOf(guestInfoCharacter);
                decrementNbOccurrencesOf(targetInfoCharacter);
            }
        }
    }

    private static void setResultStatusAtPosition(int position, MatchLetter status, MatchLetter[] results) {
        results[position] = status;
    }

    private static void decrementNbOccurrencesOf(InfoCharacter infoCharacter) {
        infoCharacter.setNbOccurrences(--infoCharacter.nbOccurrences);
    }

    private static InfoCharacter searchForTargetCharacter(InfoCharacter infoCharacter, List<InfoCharacter> targetsInfo) {
        for (InfoCharacter targetChar : targetsInfo) {
            if (infoCharacter.key.charAt(0) == targetChar.key.charAt(0))
                return targetChar;
        }
        return null;
    }

    private static List<InfoCharacter> populateCharacters(HashMap<String, List<Integer>> positions) {
        return positions.keySet()
                .stream()
                .map(k -> InfoCharacter.Of(k, positions.get(k)))
                .collect(Collectors.toList());
    }

    private static class InfoCharacter {
        private final String key;
        private final List<Integer> positions;
        private int nbOccurrences;

        private InfoCharacter(String key, List<Integer> positions) {
            this.key = key;
            this.positions = positions;
            this.nbOccurrences = this.positions.size();
        }

        public static InfoCharacter Of(String key, List<Integer> positions) {
            return new InfoCharacter(key, positions);
        }

        public List<Integer> getPositions() {
            return positions;
        }

        public int getNbOccurrences() {
            return nbOccurrences;
        }

        public void setNbOccurrences(int nbOccurrences) {
            this.nbOccurrences = nbOccurrences;
        }
    }

    private static HashMap<String, List<Integer>> computePositions(char[] guesses) {
        var results = new HashMap<String, List<Integer>>();
        buildPositions(guesses, results);
        updatePositions(guesses, results);
        return results;
    }

    private static void updatePositions(char[] guesses, HashMap<String, List<Integer>> results) {
        for (int i = 0; i < guesses.length; i++) {
            char k = guesses[i];
            for (String key : results.keySet()) {
                if (key.charAt(0) == k) {
                    List<Integer> positions = results.get(key);
                    if (!positions.contains(i)) positions.add(i);
                    Collections.sort(positions);
                    results.put(key, positions);
                }
            }
        }
    }

    private static void buildPositions(char[] guesses, HashMap<String, List<Integer>> results) {
        for (int i = 0; i < guesses.length; i++) {
            char k = guesses[i];
            final String key = k + "-" + i;
            List<Integer> positions = new ArrayList<>();
            positions.add(i);
            results.put(key, positions);
        }
    }

    private static void validateParameters(String paramValue, String paramName) {
        validateNullOrEmptyParamValue(paramValue, paramName);
        validateParamLengthValue(paramValue, paramName);
    }

    private static void validateParamLengthValue(String paramValue, String paramName) {
        if (paramValue.length() != 5)
            throw new RuntimeException(paramName + " length should be 5.");
    }

    private static void validateNullOrEmptyParamValue(String paramValue, String paramName) {
        if (paramValue == null || paramValue.isEmpty())
            throw new RuntimeException(paramName + " must not be null or empty.");
    }
}
