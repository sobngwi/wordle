package game;

import org.apache.commons.lang3.function.TriFunction;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static game.MatchLetter.*;
import static game.Wordle.Status.*;

public class Wordle {

    private static final int WORD_SIZE = 5;
    private static final int TWO_SIZE = 2;
    private static final int ONE_SIZE = 1;
    private static final BiPredicate<List<String>, List<String>> isExactMany = (guesses, targets) -> guesses.size() == targets.size();
    private static final BiPredicate<List<String>, List<String>> isTwoOne = (guesses, targets) -> guesses.size() == TWO_SIZE && targets.size() == ONE_SIZE;
    private static final BiPredicate<List<String>, List<String>> isOneTwo = (guesses, targets) -> guesses.size() == ONE_SIZE && targets.size() == TWO_SIZE;
    private static final List<MatchLetter> exactMatchAll = List.of(EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH);
    private static final List<MatchLetter> allNoMatches = List.of(NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH);
    private static final String AMAZING_MESSAGE = "Amazing";
    private static final String SPLENDID_MESSAGE = "Splendid";
    private static final String AWSOME_MESSAGE = "Awesome";
    private static final String YAY_MESSAGE = "Yay";
    private static final TriFunction<Integer, List<MatchLetter>, Integer, Response> integerListIntegerResponseTriFunction = (numberOfTries, evaluateResult, retryCounter) -> {
        if (isTheFirstOrSecondOrThirdFourthFiveSixAttemptTheBest(numberOfTries, evaluateResult)) {
            if (numberOfTries == 0)
                return new Response(retryCounter, WON, evaluateResult, AMAZING_MESSAGE);
            else if (numberOfTries == 1)
                return new Response(retryCounter, WON, evaluateResult, SPLENDID_MESSAGE);
            else if ((numberOfTries == 2))
                return new Response(retryCounter, WON, evaluateResult, AWSOME_MESSAGE);
            else return new Response(retryCounter, WON, evaluateResult, YAY_MESSAGE);
        } else
            return (retryCounter < 6) ?
                    new Response(retryCounter, INPROGRESS, evaluateResult, "") :
                    new Response(retryCounter, LOST, evaluateResult, "");
    };

    public interface SpellChecker {
        boolean isSpellingCorrect(String guess);
    }

    private static SpellChecker spellChecker;

    public static void setSpellCheckerService(SpellChecker aSpellChecker) {
        spellChecker = aSpellChecker;
    }

    private static class ExactOrPartialMatchMatchInfo {
        private final int position;
        private final List<String> guessCharacters;
        private final List<String> targetMatchingCharacters;
        private final MatchLetter twoOneMatchResult;
        private final MatchLetter oneTwoMatchResult;

        public ExactOrPartialMatchMatchInfo
                (int position, List<String> guessCharacters,
                 List<String> targetMatchingCharacters,
                 MatchLetter twoOneMatchResult, MatchLetter oneTwoMatchResult) {
            this.position = position;
            this.guessCharacters = guessCharacters;
            this.targetMatchingCharacters = targetMatchingCharacters;
            this.twoOneMatchResult = twoOneMatchResult;
            this.oneTwoMatchResult = oneTwoMatchResult;
        }

        public ExactOrPartialMatchMatchInfo(int position, List<String> guessCharacters, List<String> targetMatchingCharacters) {
            this(position, guessCharacters, targetMatchingCharacters, null, null);
        }
    }

    public static enum Status {
        WON,
        INPROGRESS,
        WRONGSPELLING,
        LOST
    }

    public record Response(int numberOfTry, Status status, List<MatchLetter> matchLetters, String messageResult) {
    }

    public static Response play(final String target, final String guess, final int numberOfTries) {
        if (numberOfTries >= 6)
            throw new RuntimeException("Game Over");

        boolean spellCheckResult = spellChecker.isSpellingCorrect(guess);
        if (!spellCheckResult) {
            return new Response(numberOfTries, WRONGSPELLING, allNoMatches, "Incorrect spelling");
        }

        final List<MatchLetter> evaluateResult = evaluate(target, guess);
        int retryCounter = numberOfTries + 1;

        return integerListIntegerResponseTriFunction.apply(numberOfTries, evaluateResult, retryCounter);
    }

    private static boolean isTheFirstOrSecondOrThirdFourthFiveSixAttemptTheBest(int numberOfTries, List<MatchLetter> evaluateResult) {
        return evaluateResult.stream()
                .allMatch(matchResult -> matchResult == EXACT_MATCH
                        && (numberOfTries == 0 || numberOfTries == 1
                        || numberOfTries == 2 || numberOfTries == 3
                        || numberOfTries == 4 || numberOfTries == 5));
    }


    public static List<MatchLetter> evaluate(String target, String guess) {
        validateParameters(target, "Target");
        validateParameters(guess, "Guess");

        if (guess.equals(target))
            return exactMatchAll;

        return IntStream.range(0, WORD_SIZE)
                .mapToObj(position -> computeMatching(target, guess, position))
                .toList();
    }

    private static MatchLetter computeMatching(String target, String guess, int position) {
        var targets = target.toCharArray();
        var guesses = guess.toCharArray();
        Map<String, List<String>> guessCharacterLists = groupCharacters(guesses);
        Map<String, List<String>> targetCharacterLists = groupCharacters(targets);
        String guessKey = guesses[position] + "";

        List<String> targetMatchingCharacters = targetCharacterLists.get(guessKey);
        if (targetMatchingCharacters == null)
            return NO_MATCH;

        List<String> guessCharacters = guessCharacterLists.get(guessKey);
        final MatchLetter exactOrPartialMatchMatch = computeOneToOneRule(guessCharacters, targetMatchingCharacters);
        final MatchLetter twoOneMatchResult = computeTwoOneRule(position, guessCharacters, targetMatchingCharacters);
        final MatchLetter oneTwoMatchResult = computeOneTwoRule(guessCharacters, targetMatchingCharacters);
        final ExactOrPartialMatchMatchInfo nonExactOrPartialMatchMatchInfo = new ExactOrPartialMatchMatchInfo(position, guessCharacters, targetMatchingCharacters, twoOneMatchResult, oneTwoMatchResult);
        final ExactOrPartialMatchMatchInfo exactOrPartialMatchMatchInfo1 = new ExactOrPartialMatchMatchInfo(position, guessCharacters, targetMatchingCharacters);
        return (exactOrPartialMatchMatch == null) ?
                applyRulesForNonExactOrPartialMatchMatch(nonExactOrPartialMatchMatchInfo)
                : applyRulesForExactOrPartialMatchMatch(exactOrPartialMatchMatchInfo1);
    }


    private static MatchLetter applyRulesForExactOrPartialMatchMatch(ExactOrPartialMatchMatchInfo exactOrPartialMatchMatchInfo) {

        return exactManyToManyRule(exactOrPartialMatchMatchInfo.position, exactOrPartialMatchMatchInfo.guessCharacters, exactOrPartialMatchMatchInfo.targetMatchingCharacters);
    }

    private static MatchLetter applyRulesForNonExactOrPartialMatchMatch(ExactOrPartialMatchMatchInfo exactOrPartialMatchMatchInfo) {

        if (isExactMany.test(exactOrPartialMatchMatchInfo.guessCharacters, exactOrPartialMatchMatchInfo.targetMatchingCharacters))
            return exactManyToManyRule(exactOrPartialMatchMatchInfo.position, exactOrPartialMatchMatchInfo.guessCharacters, exactOrPartialMatchMatchInfo.targetMatchingCharacters);
        else
            return Objects.requireNonNullElseGet(exactOrPartialMatchMatchInfo.twoOneMatchResult, () -> Objects.requireNonNullElse(exactOrPartialMatchMatchInfo.oneTwoMatchResult, NO_MATCH));
    }

    private static MatchLetter computeOneToOneRule(List<String> guessCharacters, List<String> targetCharacters) {
        return oneToOneMatchingRule(guessCharacters, targetCharacters);
    }

    private static MatchLetter computeOneTwoRule(List<String> guessCharacters, List<String> targetCharacters) {
        if (Wordle.isOneTwo.test(guessCharacters, targetCharacters))
            return oneTwoRule(guessCharacters, targetCharacters);
        return null;
    }

    private static MatchLetter computeTwoOneRule(int position, List<String> guessCharacters, List<String> targetCharacters) {
        if (Wordle.isTwoOne.test(guessCharacters, targetCharacters)) {
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
        String firstGuessIndex = guessCharacters.getFirst();
        int firstGuestCharacterPosition = Integer.parseInt(firstGuessIndex.split(":")[1]);
        String lastGuessIndex = guessCharacters.getLast();
        int lastGuestCharacterPosition = Integer.parseInt(lastGuessIndex.split(":")[1]);
        String firstTargetIndex = targetCharacters.getFirst();
        int firstTargetPosition = Integer.parseInt(firstTargetIndex.split(":")[1]);
        final boolean areIndexesAndPositionMatch = firstGuessIndex.equals(firstTargetIndex) && position == firstGuestCharacterPosition || lastGuessIndex.equals(firstTargetIndex) && position == lastGuestCharacterPosition;

        final MatchLetter exactMatch = executeTwoMatchRuleExactMatch(guessCharacters, firstTargetIndex, areIndexesAndPositionMatch);
        if (exactMatch != null)
            return exactMatch;
        return
                executeTwoMatchRulePartialMatch(position, firstGuestCharacterPosition, firstTargetPosition);
    }

    private static MatchLetter executeTwoMatchRulePartialMatch(int position, int firstGuestCharacterPosition, int firstTargetPosition) {
        if (position == firstGuestCharacterPosition && firstTargetPosition > position)
            return PARTIAL_MATCH;
        else
            return NO_MATCH;
    }

    private static MatchLetter executeTwoMatchRuleExactMatch(List<String> guessCharacters, String firstTargetIndex, boolean areIndexesAndPositionMatch) {
        if (guessCharacters.contains(firstTargetIndex)) {
            if (areIndexesAndPositionMatch)
                return EXACT_MATCH;
            else return NO_MATCH;
        }
        return null;
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
        if (guessCharacters.size() == targetCharacters.size() && guessCharacters.size() == ONE_SIZE) {
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
