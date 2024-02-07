package game;

import java.util.*;
import java.util.stream.Collectors;

import static game.MatchLetter.*;

public class Wordle {

    public static List<MatchLetter> evaluate(String target, String guess) {
        validateParameters(target, "Target");
        validateParameters(guess, "Guess");


        var results = new MatchLetter[]{NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH};
        var targets = target.toCharArray();
        var guesses = guess.toCharArray();

        var guessesPositions = computePositions(guesses);
        var targetsPositions = computePositions(targets);
        var guessesInfo = populateCharacters(guessesPositions);
        var targetsInfo = populateCharacters(targetsPositions);

        for (InfoCharacter guessInfoCharacter : guessesInfo) {
            for (InfoCharacter targetInfoCharacter : targetsInfo) {
                if (guessInfoCharacter.key().charAt(0) == targetInfoCharacter.key().charAt(0)) {
                    var nbToUpdate = guessInfoCharacter.positions().size() - targetInfoCharacter.positions().size();
                    if (nbToUpdate <= 0) { // update all
                        for (int i = 0; i < guessInfoCharacter.positions().size(); i++) {
                            if (!targetInfoCharacter.positions().contains(guessInfoCharacter.positions().get(i))) {
                                results[guessInfoCharacter.positions().get(i)] = PARTIAL_MATCH;
                            } else results[guessInfoCharacter.positions().get(i)] = EXACT_MATCH;
                        }
                    } else {
                        for (int i = 0; i < guessInfoCharacter.positions().size() - targetInfoCharacter.positions().size(); i++) {
                            var rivers = guessInfoCharacter.positions();
                            int matchingIndex = 0;
                            for (var t = 0; t < rivers.size(); t++) {
                                if (Objects.equals(rivers.get(t), targetInfoCharacter.positions().getFirst())) {
                                    matchingIndex = t;
                                    break;
                                }
                            }
                            boolean isTargetInfoInGuesses = rivers.contains(targetInfoCharacter.positions().getFirst());
                            if (isTargetInfoInGuesses) {
                                results[rivers.get(matchingIndex)] = EXACT_MATCH;
                                break;
                            }
                            if (!targetInfoCharacter.positions().contains(guessInfoCharacter.positions().get(i))) {
                                results[guessInfoCharacter.positions().get(i)] = PARTIAL_MATCH;
                            }
                        }
                    }
                }

            }
        }
        return Arrays.stream(results).toList();
    }

    private static List<InfoCharacter>  populateCharacters( HashMap<String, List<Integer>> datas) {
       return datas.keySet()
               .stream()
               .map( k -> new InfoCharacter(k, datas.get(k)) )
               .collect(Collectors.toList());
    }
    private record InfoCharacter(String key, List<Integer> positions) {}

    private static HashMap<String, List<Integer>> computePositions(char[] guesses) {
        var results = new HashMap<String, List<Integer>>();
        for (int i = 0; i < guesses.length; i++) {
            char k = guesses[i];
            final String key = k + "-" + i;
            List<Integer> positions = new ArrayList<>();
            positions.add(i);
            results.put(key, positions);
        }

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
        return results;
    }

    private static void validateParameters(String paramValue, String paramName) {
        if (paramValue == null || paramValue.isEmpty())
            throw new RuntimeException(paramName + " must not be null or empty.");
        if (paramValue.length() != 5)
            throw new RuntimeException(paramName + " length should be 5.");
    }
}
