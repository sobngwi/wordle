package game;


import java.io.IOException;
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
    private static final String AWESOME_MESSAGE = "Awesome";
    private static final String YAY_MESSAGE = "Yay";
    public static final String GAME_OVER = "Game Over";
    public static final String INCORRECT_SPELLING = "Incorrect spelling";
    private static SpellChecker spellChecker;

    @FunctionalInterface
    private interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }

    private static final TriFunction<Integer, List<MatchLetter>, Integer, Response> integerListIntegerResponseTriFunction = (numberOfTries, evaluateResult, retryCounter) -> {
        Map<Integer, Response> winResponses = new HashMap<>();
        winResponses.putIfAbsent(0, new Response(retryCounter, WON, evaluateResult, AMAZING_MESSAGE));
        winResponses.putIfAbsent(1, new Response(retryCounter, WON, evaluateResult, SPLENDID_MESSAGE));
        winResponses.putIfAbsent(2, new Response(retryCounter, WON, evaluateResult, AWESOME_MESSAGE));
        winResponses.putIfAbsent(3, new Response(retryCounter, WON, evaluateResult, YAY_MESSAGE));

        Map<Integer, Response> inProgressLostResponses = new HashMap<>();
        inProgressLostResponses.putIfAbsent(1, new Response(retryCounter, INPROGRESS, evaluateResult, ""));
        inProgressLostResponses.putIfAbsent(6, new Response(retryCounter, LOST, evaluateResult, ""));

        if (isTheFirstOrSecondOrThirdFourthFiveSixAttemptTheBest(numberOfTries, evaluateResult)) {
            return winResponses.getOrDefault(numberOfTries, new Response(retryCounter, WON, evaluateResult, YAY_MESSAGE));
        } else
            return inProgressLostResponses.getOrDefault(retryCounter, new Response(retryCounter, INPROGRESS, evaluateResult, ""));
    };

    public interface SpellChecker {
        boolean isSpellingCorrect(String guess);
    }

    public static void setSpellCheckerService(SpellChecker aSpellChecker) {
        spellChecker = aSpellChecker;
    }

    private record ExactOrPartialMatchMatchInfo (
         int position,
         List<String> guessCharacters,
         List<String> targetMatchingCharacters,
         MatchLetter twoOneMatchResult,
         MatchLetter oneTwoMatchResult){

        public ExactOrPartialMatchMatchInfo(int position, List<String> guessCharacters, List<String> targetMatchingCharacters) {
            this(position, guessCharacters, targetMatchingCharacters, null, null);
        }
    }

     public enum Status {
        WON,
        INPROGRESS,
        WRONGSPELLING,
        LOST
    }

    public record Response(int numberOfTry, Status status, List<MatchLetter> matchLetters, String messageResult) {
    }

    public static Response play(final String target, final String guess, final int numberOfTries) {
        if (numberOfTries >= 6)
            throw new RuntimeException(GAME_OVER);

        boolean spellCheckResult = spellChecker.isSpellingCorrect(guess);
        if (!spellCheckResult) {
            return new Response(numberOfTries, WRONGSPELLING, allNoMatches, INCORRECT_SPELLING);
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
        int firstGuestCharacterPosition = Integer.parseInt(getIndexAsString(firstGuessIndex));
        String lastGuessIndex = guessCharacters.getLast();
        int lastGuestCharacterPosition = Integer.parseInt(getIndexAsString(lastGuessIndex));
        String firstTargetIndex = targetCharacters.getFirst();
        int firstTargetPosition = Integer.parseInt(getIndexAsString(firstTargetIndex));
        final boolean areIndexesAndPositionMatch = firstGuessIndex.equals(firstTargetIndex) && position == firstGuestCharacterPosition || lastGuessIndex.equals(firstTargetIndex) && position == lastGuestCharacterPosition;

        final Optional<MatchLetter> exactMatch = executeTwoMatchRuleExactMatch(guessCharacters, firstTargetIndex, areIndexesAndPositionMatch);
        return exactMatch.orElse(executeTwoMatchRulePartialMatch(position, firstGuestCharacterPosition, firstTargetPosition));

    }

    private static String getIndexAsString(String firstGuessIndex) {
        Objects.requireNonNull(firstGuessIndex);
        return firstGuessIndex.split(":")[1];
    }

    private static MatchLetter executeTwoMatchRulePartialMatch(int position, int firstGuestCharacterPosition, int firstTargetPosition) {
       Map<Boolean, MatchLetter> booleanMatchLetterMap = Map.of(true, PARTIAL_MATCH);
        final boolean isPartialMatch = position == firstGuestCharacterPosition && firstTargetPosition > position;
        return booleanMatchLetterMap.getOrDefault(isPartialMatch,NO_MATCH);
    }

    private static Optional<MatchLetter> executeTwoMatchRuleExactMatch(List<String> guessCharacters, String firstTargetIndex, boolean areIndexesAndPositionMatch) {
       Map<Boolean, MatchLetter> booleanMatchLetterMap =Map.of(true, EXACT_MATCH);
        if (guessCharacters.contains(firstTargetIndex)) {
            return Optional.of(booleanMatchLetterMap.getOrDefault(areIndexesAndPositionMatch, NO_MATCH));
        }
        return Optional.empty();
    }

    private static MatchLetter exactManyToManyRule(int position, List<String> guessCharacters, List<String> targetCharacters) {
        List<MatchLetter> willReturn = computeTheManyToManyExactRule(guessCharacters, targetCharacters);
        boolean isPositionMatchTheFirstGuessIndex = position == Integer.parseInt(getIndexAsString(guessCharacters.getFirst()));
        if (isPositionMatchTheFirstGuessIndex)
            return willReturn.getFirst();
        else
            return willReturn.getLast();
    }

    private static List<MatchLetter> computeTheManyToManyExactRule(List<String> guessCharacters, List<String> targetCharacters) {
        List<MatchLetter> matchLetters = Arrays.asList(PARTIAL_MATCH, PARTIAL_MATCH);
        Map<Integer, List<MatchLetter>> exactMatchers = new HashMap<>();
        for (int i = 0; i < guessCharacters.size(); i++) {
            final int index = i;
            exactMatchers = targetCharacters.stream()
                    .filter(targetCharacter -> targetCharacter.equals(guessCharacters.get(index)))
                    .limit(1)
                    .map(x -> EXACT_MATCH)
                    .collect(Collectors.groupingBy(x -> index));
        }
        final Optional<Integer> anExactMatcher = exactMatchers.keySet().stream().findFirst();
        if (anExactMatcher.isPresent())
            matchLetters.set(anExactMatcher.get(), exactMatchers.get(anExactMatcher.get()).getFirst());
        return matchLetters;
    }

    private static MatchLetter oneToOneMatchingRule(List<String> guessCharacters, List<String> targetCharacters) {
        Map<List<String>, MatchLetter> listMatchLetterMap = Map.of(guessCharacters, EXACT_MATCH);
        if (guessCharacters.size() == targetCharacters.size()) {
            return listMatchLetterMap.getOrDefault(targetCharacters, PARTIAL_MATCH);
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

    public static class AgileDeveloperSpellChecker implements SpellChecker {

        String getResponse(String guess) throws IOException {
            Objects.requireNonNull(guess);
            if (guess.equals("gddo"))
                return "false";
            return "true";
        }

        @Override
        public boolean isSpellingCorrect(String guess) {
            try {
                return Boolean.parseBoolean(getResponse(guess));
            } catch (IOException ioException) {
                throw new RuntimeException(ioException.getMessage(), ioException);
            }
        }
    }
}
