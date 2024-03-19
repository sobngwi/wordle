package game;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

        var exactMatchAll = new MatchLetter[]{EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH};
        if (guess.equals(target))
            return Arrays.stream(exactMatchAll).toList();


        var results = new MatchLetter[]{NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH};
        var targets = target.toCharArray();
        var guesses = guess.toCharArray();

        IntStream.range(0, WORD_SIZE).forEach(position -> results[position] = computeMatching(targets, guesses, position));
        return Arrays.stream(results).toList();
    }

    private static MatchLetter computeMatching(char[] targets, char[] guesses, int position) {
        Map<String, List<String>> guessCharacterLists = groupCharacters(guesses);
        Map<String, List<String>> targetCharacterLists = groupCharacters(targets);

        List<String> targetCharacters = targetCharacterLists.get(guesses[position] + "");
        if (targetCharacters == null)
            return NO_MATCH;

        List<String> guessCharacters = guessCharacterLists.get(guesses[position] + "");
        MatchLetter ExactOrPartialMatchMatch = oneToOneMatchingRule(guessCharacters, targetCharacters);
        if (ExactOrPartialMatchMatch != null)
            return ExactOrPartialMatchMatch;

        BiPredicate<List<String>, List<String>> isExactMany = (guess, target) -> guess.size() == target.size();
        if (isExactMany.test(guessCharacters, targetCharacters))
            return exactManyToManyRule(position, guessCharacters, targetCharacters);

        BiPredicate<List<String>, List<String>> isTwoOne = (guess, target) -> guess.size() == TWO_SIZE && target.size() == ONE_SIZE;
        MatchLetter twoOneMatchResult = computeTwoOneRule(position, guessCharacters, targetCharacters, isTwoOne);
        if (twoOneMatchResult != null)
            return twoOneMatchResult;

        BiPredicate<List<String>, List<String>> isOneTwo = (guess, target) -> guess.size() == 1 && target.size() == TWO_SIZE;
        MatchLetter oneTwoMatchResult = computeOneTwoRule(position, guessCharacters, targetCharacters, isOneTwo);
        if (oneTwoMatchResult != null)
            return oneTwoMatchResult;

        return NO_MATCH;
    }

    private static MatchLetter computeOneTwoRule(int position, List<String> guessCharacters, List<String> targetCharacters, BiPredicate<List<String>, List<String>> isOneTwo) {
        if (isOneTwo.test(guessCharacters, targetCharacters))
            return oneTwoRule(position, guessCharacters, targetCharacters);
        return null;
    }

    private static MatchLetter computeTwoOneRule(int position, List<String> guessCharacters, List<String> targetCharacters, BiPredicate<List<String>, List<String>> isTwoOne) {
        if (isTwoOne.test(guessCharacters, targetCharacters)) {
            return twoOneRule(position, guessCharacters, targetCharacters);
        }
        return null;
    }

    private static MatchLetter oneTwoRule(int position, List<String> guessCharacters, List<String> targetCharacters) {
        System.out.println("1-2 @Position : " + position);
        System.out.println("1-2 guess  : " + guessCharacters);
        System.out.println("1-2 target  : " + targetCharacters);
        if (targetCharacters.getLast().equals(guessCharacters.getFirst())) {
            System.out.println("1-2b Will set the result to EXACT_MATCH @ Position: " + position);
            return EXACT_MATCH;
        } else {
            System.out.println(" ? 1-2c Will set the result to PARTIAL  @ Position: " + position);
            return PARTIAL_MATCH;
        }
    }

    private static MatchLetter twoOneRule(int position, List<String> guessCharacters, List<String> targetCharacters) {
        System.out.println("2-1 @Position : " + position);
        System.out.println("2-1 guess  : " + guessCharacters);
        System.out.println("2-1 target  : " + targetCharacters);
        int firstGuestCharacterPosition = Integer.parseInt(guessCharacters.getFirst().split(":")[1]);
        int lastGuestCharacterPosition = Integer.parseInt(guessCharacters.getLast().split(":")[1]);
        if (guessCharacters.contains(targetCharacters.getFirst())) {
            System.out.println(" ?2-1a Will set the result to EXACT_MATCH @Position: " + position);
            if (guessCharacters.getFirst().equals(targetCharacters.getFirst())
                    && position == firstGuestCharacterPosition) {
                System.out.println("2-1a Will set the result to EXACT_MATCH @Position: " + position);
                //results[Integer.parseInt(guessCharacterLists.get( guesses[position] + "").getFirst().split(":")[1])] = EXACT_MATCH;
                return EXACT_MATCH;
            }

            if (guessCharacters.getLast().equals(targetCharacters.getFirst())
                    && position == lastGuestCharacterPosition) {
                System.out.println("2-1b Will set the result to EXACT_MATCH @ Position: " + position);
                return EXACT_MATCH;
            }


        } else {
            System.out.println(" ? 2-1c Will set the result to EXACT_MATCH @ Position: " + position);
            System.out.println("Currrent guest position :" + firstGuestCharacterPosition);
            System.out.println("Target position = " + Integer.parseInt(targetCharacters.getFirst().split(":")[1]));
            if (position == firstGuestCharacterPosition
                    && Integer.parseInt(targetCharacters.getFirst().split(":")[1]) > position) {
                System.out.println("2-1c Will set the result to PARTIAL_MATCH @ Position: " + position);
                return PARTIAL_MATCH;
            } else {
                System.out.println("2-1d Will set the result to NO_MATCH @ Position: " + position);
                return NO_MATCH;
            }
        }
        return null;
    }

    private static MatchLetter exactManyToManyRule(int position, List<String> guessCharacters, List<String> targetCharacters) {
        MatchLetter[] willReturn = new MatchLetter[]{PARTIAL_MATCH, PARTIAL_MATCH};
        for (int i = 0; i < guessCharacters.size(); i++) {
            for (String targetCharacter : targetCharacters) {
                if (guessCharacters.get(i).equals(targetCharacter)
                ) {
                    willReturn[i] = EXACT_MATCH;
                    break;
                }
            }
        }
        if (position == Integer.parseInt(guessCharacters.getFirst().split(":")[1]))
            return willReturn[0];
        else return willReturn[1];
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
                .mapToObj(i -> guesses[i] + ":" + i)
                .sorted(Comparator.comparing(k -> k.split(":")[0]))
                .collect(Collectors.groupingBy(x -> x.split(":")[0]));
    }


    private static void validateParameters(String paramValue, String paramName) {
        validateNullOrEmptyParamValue(paramValue, paramName);
        validateParamLengthValue(paramValue, paramName);
    }

    private static void validateParamLengthValue(String paramValue, String paramName) {
        if (paramValue.length() != WORD_SIZE)
            throw new RuntimeException(paramName + " length should be 5.");
    }

    private static void validateNullOrEmptyParamValue(String paramValue, String paramName) {
        if (paramValue == null || paramValue.isEmpty())
            throw new RuntimeException(paramName + " must not be null or empty.");
    }
}
