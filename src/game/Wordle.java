package game;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static game.MatchLetter.*;

public class Wordle {

    private static final int WORD_SIZE = 5;
    private static final int TWO_SIZE = 2;
    private static final int ONE_SIZE = 1;

    public static List<MatchLetter> evaluate(String target, String guess) {
        validateParameters(target, "Target");
        validateParameters(guess, "Guess");

        if (guess.equals(target))
            return List.of(EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH);

        return IntStream.range(0, WORD_SIZE)
                .mapToObj(position -> computeMatching(target.toCharArray(), guess.toCharArray(), position))
                .toList();
    }

    private static MatchLetter computeMatching(char[] targets, char[] guesses, int position) {
        Map<String, List<String>> guessCharacterLists = groupCharacters(guesses);
        Map<String, List<String>> targetCharacterLists = groupCharacters(targets);
        String guessKey = guesses[position] + "";

        List<String> targetMatchingCharacters = targetCharacterLists.get(guessKey);
        if (targetMatchingCharacters == null)
            return NO_MATCH;

        BiPredicate<List<String>, List<String>> isExactMany = (guess, target) -> guess.size() == target.size();
        BiPredicate<List<String>, List<String>> isTwoOne = (guess, target) -> guess.size() == TWO_SIZE && target.size() == ONE_SIZE;
        BiPredicate<List<String>, List<String>> isOneTwo = (guess, target) -> guess.size() == ONE_SIZE && target.size() == TWO_SIZE;
        List<String> guessCharacters = guessCharacterLists.get(guessKey);
        final MatchLetter exactOrPartialMatchMatch = computeOneToOneRule(guessCharacters, targetMatchingCharacters);
        final MatchLetter twoOneMatchResult = computeTwoOneRule(position, guessCharacters, targetMatchingCharacters, isTwoOne);
        final MatchLetter oneTwoMatchResult = computeOneTwoRule(guessCharacters, targetMatchingCharacters, isOneTwo);

        if (exactOrPartialMatchMatch != null)
            return exactOrPartialMatchMatch;
        else if (isExactMany.test(guessCharacters, targetMatchingCharacters))
            return exactManyToManyRule(position, guessCharacters, targetMatchingCharacters);
        else
            return Objects.requireNonNullElseGet(twoOneMatchResult, () -> Objects.requireNonNullElse(oneTwoMatchResult, NO_MATCH));
    }

    private static MatchLetter computeOneToOneRule(List<String> guessCharacters, List<String> targetCharacters) {
        return oneToOneMatchingRule(guessCharacters, targetCharacters);
    }

    private static MatchLetter computeOneTwoRule(List<String> guessCharacters, List<String> targetCharacters, BiPredicate<List<String>, List<String>> isOneTwo) {
        if (isOneTwo.test(guessCharacters, targetCharacters))
            return oneTwoRule(guessCharacters, targetCharacters);
        return null;
    }

    private static MatchLetter computeTwoOneRule(int position, List<String> guessCharacters, List<String> targetCharacters, BiPredicate<List<String>, List<String>> isTwoOne) {
        if (isTwoOne.test(guessCharacters, targetCharacters)) {
            return twoOneRule(position, guessCharacters, targetCharacters);
        }
        return null;
    }

    private static MatchLetter oneTwoRule(List<String> guessCharacters, List<String> targetCharacters) {
        if (targetCharacters.getLast().equals(guessCharacters.getFirst()))
            return EXACT_MATCH;
        else
            return PARTIAL_MATCH;
    }

    private static MatchLetter twoOneRule(int position, List<String> guessCharacters, List<String> targetCharacters) {
        String guessFirstIndex = guessCharacters.getFirst();
        int firstGuestCharacterPosition = Integer.parseInt(guessFirstIndex.split(":")[1]);
        String lastGuessIndex = guessCharacters.getLast();
        int lastGuestCharacterPosition = Integer.parseInt(lastGuessIndex.split(":")[1]);
        String firstTargetIndex = targetCharacters.getFirst();
        int firstTargetPosition = Integer.parseInt(firstTargetIndex.split(":")[1]);
        final boolean isIndexesAndPositionMatch = guessFirstIndex.equals(firstTargetIndex) && position == firstGuestCharacterPosition || lastGuessIndex.equals(firstTargetIndex) && position == lastGuestCharacterPosition;

        if (guessCharacters.contains(firstTargetIndex)) {
            if (isIndexesAndPositionMatch)
                return EXACT_MATCH;
            else return NO_MATCH;
        } else if (position == firstGuestCharacterPosition && firstTargetPosition > position)
            return PARTIAL_MATCH;
        else
            return NO_MATCH;
    }

    private static MatchLetter exactManyToManyRule(int position, List<String> guessCharacters, List<String> targetCharacters) {
        MatchLetter[] willReturn = new MatchLetter[]{PARTIAL_MATCH, PARTIAL_MATCH};
        computeTheManyToManyExactRule(guessCharacters, targetCharacters, willReturn);
        boolean isPositionMatchTheFirstGuessIndex = position == Integer.parseInt(guessCharacters.getFirst().split(":")[1]);
        if (isPositionMatchTheFirstGuessIndex)
            return willReturn[0];
        else
            return willReturn[1];
    }

    private static void computeTheManyToManyExactRule(List<String> guessCharacters, List<String> targetCharacters, MatchLetter[] willReturn) {
        for (int i = 0; i < guessCharacters.size(); i++) {
            for (String targetCharacter : targetCharacters) {
                if (guessCharacters.get(i).equals(targetCharacter)) {
                    willReturn[i] = EXACT_MATCH;
                    break;
                }
            }
        }
    }

    private static MatchLetter oneToOneMatchingRule(List<String> guessCharacters, List<String> targetCharacters) {
        if (guessCharacters.size() == targetCharacters.size()
                && guessCharacters.size() == 1) {
            if (guessCharacters.equals(targetCharacters)) {
                return EXACT_MATCH;
            } else {
                return PARTIAL_MATCH;
            }
        }
        return null;
    }

    private static Map<String, List<String>> groupCharacters(char[] guesses) {
        return IntStream.range(0, WORD_SIZE)
                .mapToObj(position -> guesses[position] + ":" + position)
                .sorted(Comparator.comparing(key -> key.split(":")[0]))
                .collect(Collectors.groupingBy(character -> character.split(":")[0]));
    }

    private static void validateParameters(String paramValue, String paramName) {
        validateNullOrEmptyParamValue(paramValue, paramName);
        validateParamLengthValue(paramValue, paramName);
    }

    private static void validateParamLengthValue(String paramValue, String paramName) {
        if (paramValue.length() != WORD_SIZE)
            throw new RuntimeException(paramName + " length should be " + WORD_SIZE + ".");
    }

    private static void validateNullOrEmptyParamValue(String paramValue, String paramName) {
        if (paramValue == null || paramValue.isEmpty())
            throw new RuntimeException(paramName + " must not be null or empty.");
    }
}
