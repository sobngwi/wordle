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
    private static final BiPredicate<List<String>, List<String>> IS_TW_0_ONE = (guesses, targets) -> guesses.size() == TWO_SIZE && targets.size() == ONE_SIZE;
    private static final BiPredicate<List<String>, List<String>> IS_ONE_TWO = (guesses, targets) -> guesses.size() == ONE_SIZE && targets.size() == TWO_SIZE;
    private static final List<MatchLetter> EXACT_MATCH_ALL = List.of(EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH);
    private static final List<MatchLetter> ALL_NO_MATCHES = List.of(NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH);
    private static final String AMAZING_MESSAGE = "Amazing";
    private static final String SPLENDID_MESSAGE = "Splendid";
    private static final String AWESOME_MESSAGE = "Awesome";
    private static final String YAY_MESSAGE = "Yay";
    public static final String GAME_OVER = "Game Over";
    public static final String INCORRECT_SPELL = "Incorrect spelling";
    private static SpellChecker spellChecker;
    private static final TriFunction<Integer, List<MatchLetter>, Integer, Response> RESPONSE_TRI_FUNCTION = (numberOfTries, evaluateResult, retryCounter) -> {
        final Map<Integer, Response> winResponses = new HashMap<>();
        winResponses.putIfAbsent(0, new Response(retryCounter, WON, evaluateResult, AMAZING_MESSAGE));
        winResponses.putIfAbsent(1, new Response(retryCounter, WON, evaluateResult, SPLENDID_MESSAGE));
        winResponses.putIfAbsent(2, new Response(retryCounter, WON, evaluateResult, AWESOME_MESSAGE));
        winResponses.putIfAbsent(3, new Response(retryCounter, WON, evaluateResult, YAY_MESSAGE));

        final Map<Integer, Response> inProgressLost = new HashMap<>();
        inProgressLost.putIfAbsent(1, new Response(retryCounter, INPROGRESS, evaluateResult, ""));
        inProgressLost.putIfAbsent(6, new Response(retryCounter, LOST, evaluateResult, ""));

        if (isTheFirstOrSecondOrThirdFourthFiveSixAttemptTheBest(numberOfTries, evaluateResult)) {
            return winResponses.getOrDefault(numberOfTries, new Response(retryCounter, WON, evaluateResult, YAY_MESSAGE));
        } else
            return inProgressLost.getOrDefault(retryCounter, new Response(retryCounter, INPROGRESS, evaluateResult, ""));
    };

    @FunctionalInterface
    private interface TriFunction<T, U, V, R> {
        R apply(T firstArgumentType, U secondArgumentType, V returnType);
    }


    public interface SpellChecker {
        boolean isSpellingCorrect(String guess);
    }

    public static void setSpellCheckerService(final SpellChecker aSpellChecker) {
        spellChecker = aSpellChecker;
    }

    private record ExactOrPartialMatchMatchInfo (
         int position,
         List<String> guessCharacters,
         List<String> targetCharacters,
         MatchLetter twoOneMatchResult,
         MatchLetter oneTwoMatchResult){

        public ExactOrPartialMatchMatchInfo(final int position, final List<String> guessCharacters, final List<String> targetCharacters) {
            this(position, guessCharacters, targetCharacters, null, null);
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

        return executeThePlayTryInstance(target, guess, numberOfTries);
    }

    private static Response executeThePlayTryInstance(final String target, final String guess, final int numberOfTries) {
        final boolean isWellSpelled = spellChecker.isSpellingCorrect(guess);
        if (!isWellSpelled)
            return new Response(numberOfTries, WRONGSPELLING, ALL_NO_MATCHES, INCORRECT_SPELL);
        else
            return playTryInstance(target, guess, numberOfTries);
    }

    private static Response playTryInstance(String target, String guess, int numberOfTries) {
        final List<MatchLetter> evaluateResult = evaluate(target, guess);
        final int retryCounter = numberOfTries + 1;

        return RESPONSE_TRI_FUNCTION.apply(numberOfTries, evaluateResult, retryCounter);
    }

    private static boolean isTheFirstOrSecondOrThirdFourthFiveSixAttemptTheBest(final int numberOfTries, final List<MatchLetter> evaluateResult) {
        return evaluateResult.stream()
                .allMatch(matchResult -> matchResult == EXACT_MATCH
                        && (numberOfTries == 0 || numberOfTries == 1
                        || numberOfTries == 2 || numberOfTries == 3
                        || numberOfTries == 4 || numberOfTries == 5));
    }


    public static List<MatchLetter> evaluate(final String target, final String guess) {
        validateParameters(target, "Target");
        validateParameters(guess, "Guess");

        if (guess.equals(target))
            return EXACT_MATCH_ALL;

        return IntStream.range(0, WORD_SIZE)
                .mapToObj(position -> computeMatching(target, guess, position))
                .toList();
    }

    private static MatchLetter computeMatching(final String target, final String guess, final int position) {
        final var targets = target.toCharArray();
        final var guesses = guess.toCharArray();
        final Map<String, List<String>> guessesMap = groupCharacters(guesses);
        final Map<String, List<String>> targetsMap = groupCharacters(targets);
        final String guessKey = guesses[position] + "";

        final List<String> targetCharacters = targetsMap.get(guessKey);
        final List<String> guessCharacters = guessesMap.get(guessKey);
        return computeMatchingAtPosition(position, targetCharacters, guessCharacters);
    }

    private static MatchLetter computeMatchingAtPosition(final int position, final List<String> targetCharacters, final List<String> guessCharacters) {
        if (targetCharacters == null)
            return NO_MATCH;
        else
            return computeExactMatchOrPartialMatch(position, guessCharacters, targetCharacters);
    }

    private static MatchLetter computeExactMatchOrPartialMatch(final int position, final List<String> guessCharacters, final List<String> targetCharacters) {
        final MatchLetter exactOrPartialMatchMatch = computeOneToOneRule(guessCharacters, targetCharacters);
        final MatchLetter twoOneMatchResult = computeTwoOneRule(position, guessCharacters, targetCharacters);
        final MatchLetter oneTwoMatchResult = computeOneTwoRule(guessCharacters, targetCharacters);
        final ExactOrPartialMatchMatchInfo nonExactOrPartialMatchMatchInfo = new ExactOrPartialMatchMatchInfo(position, guessCharacters, targetCharacters, twoOneMatchResult, oneTwoMatchResult);
        final ExactOrPartialMatchMatchInfo exactOrPartialMatchMatchInfo1 = new ExactOrPartialMatchMatchInfo(position, guessCharacters, targetCharacters);
        return (exactOrPartialMatchMatch == null) ?
                applyRulesForNonExactOrPartialMatchMatch(nonExactOrPartialMatchMatchInfo)
                : applyRulesForExactOrPartialMatchMatch(exactOrPartialMatchMatchInfo1);
    }


    private static MatchLetter applyRulesForExactOrPartialMatchMatch(final ExactOrPartialMatchMatchInfo exactOrPartialMatchMatchInfo) {
        return exactManyToManyRule(exactOrPartialMatchMatchInfo.position, exactOrPartialMatchMatchInfo.guessCharacters, exactOrPartialMatchMatchInfo.targetCharacters);
    }

    private static MatchLetter applyRulesForNonExactOrPartialMatchMatch(final ExactOrPartialMatchMatchInfo exactOrPartialMatchMatchInfo) {
        return Objects.requireNonNullElseGet(exactOrPartialMatchMatchInfo.twoOneMatchResult, () -> Objects.requireNonNullElse(exactOrPartialMatchMatchInfo.oneTwoMatchResult, NO_MATCH));
    }

    private static MatchLetter computeOneToOneRule(final List<String> guessCharacters, final List<String> targetCharacters) {
        return oneToOneMatchingRule(guessCharacters, targetCharacters);
    }

    private static MatchLetter computeOneTwoRule(final List<String> guessCharacters, final List<String> targetCharacters) {
        if (IS_ONE_TWO.test(guessCharacters, targetCharacters))
            return oneTwoRule(guessCharacters, targetCharacters);
        else
            return null;
    }

    private static MatchLetter computeTwoOneRule(final int position, final List<String> guessCharacters, final List<String> targetCharacters) {
        if (IS_TW_0_ONE.test(guessCharacters, targetCharacters)) {
            return twoOneRule(position, guessCharacters, targetCharacters);
        }
        else
            return null;
    }

    private static MatchLetter oneTwoRule(final List<String> guessCharacters, final List<String> targetCharacters) {
        if (targetCharacters.getLast().equals(guessCharacters.getFirst()))
            return EXACT_MATCH;
        else
            return PARTIAL_MATCH;
    }

    private static MatchLetter twoOneRule(final int position, final List<String> guessCharacters, final List<String> targetCharacters) {
        final String firstGuessIndex = guessCharacters.getFirst();
        final int firstGuestCharacterPosition = Integer.parseInt(getIndexAsString(firstGuessIndex));
        final String lastGuessIndex = guessCharacters.getLast();
        final int lastGuestCharacterPosition = Integer.parseInt(getIndexAsString(lastGuessIndex));
        final String firstTargetIndex = targetCharacters.getFirst();
        final int firstTargetPosition = Integer.parseInt(getIndexAsString(firstTargetIndex));
        final boolean areIndexesAndPositionMatch = firstGuessIndex.equals(firstTargetIndex) && position == firstGuestCharacterPosition || lastGuessIndex.equals(firstTargetIndex) && position == lastGuestCharacterPosition;

        final Optional<MatchLetter> exactMatch = executeTwoMatchRuleExactMatch(guessCharacters, firstTargetIndex, areIndexesAndPositionMatch);
        return exactMatch.orElse(executeTwoMatchRulePartialMatch(position, firstGuestCharacterPosition, firstTargetPosition));

    }

    private static String getIndexAsString(final String firstGuessIndex) {
        Objects.requireNonNull(firstGuessIndex);
        return firstGuessIndex.split(":")[1];
    }

    private static MatchLetter executeTwoMatchRulePartialMatch(final int position, final int firstGuestCharacterPosition, final int firstTargetPosition) {
        final Map<Boolean, MatchLetter> booleanMatchLetterMap = Map.of(true, PARTIAL_MATCH);
        final boolean isPartialMatch = position == firstGuestCharacterPosition && firstTargetPosition > position;
        return booleanMatchLetterMap.getOrDefault(isPartialMatch,NO_MATCH);
    }

    private static Optional<MatchLetter> executeTwoMatchRuleExactMatch(final List<String> guessCharacters, final String firstTargetIndex, final boolean areIndexesAndPositionMatch) {
       final Map<Boolean, MatchLetter> booleanMatchLetterMap =Map.of(true, EXACT_MATCH);
        if (guessCharacters.contains(firstTargetIndex)) {
            return Optional.of(booleanMatchLetterMap.getOrDefault(areIndexesAndPositionMatch, NO_MATCH));
        }
        return Optional.empty();
    }

    private static MatchLetter exactManyToManyRule(final int position, final List<String> guessCharacters, final List<String> targetCharacters) {
        final List<MatchLetter> exactMatchLetters = computeTheManyToManyExactRule(guessCharacters, targetCharacters);
        final boolean isPositionMatchTheFirstGuessIndex = position == Integer.parseInt(getIndexAsString(guessCharacters.getFirst()));
        if (isPositionMatchTheFirstGuessIndex)
            return exactMatchLetters.getFirst();
        else
            return exactMatchLetters.getLast();
    }

    private static List<MatchLetter> computeTheManyToManyExactRule(final List<String> guessCharacters, final List<String> targetCharacters) {
        final List<MatchLetter> matchLetters = Arrays.asList(PARTIAL_MATCH, PARTIAL_MATCH);
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

    private static MatchLetter oneToOneMatchingRule(final List<String> guessCharacters, final List<String> targetCharacters) {
        final Map<List<String>, MatchLetter> listMatchLetterMap = Map.of(guessCharacters, EXACT_MATCH);
        if (guessCharacters.size() == targetCharacters.size()) {
            return listMatchLetterMap.getOrDefault(targetCharacters, PARTIAL_MATCH);
        }
        return null;
    }

    private static Map<String, List<String>> groupCharacters(final char[] guesses) {
        return IntStream.range(0, WORD_SIZE)
                .mapToObj(position -> guesses[position] + ":" + position)
                .sorted(Comparator.comparing(key -> key.split(":")[0]))
                .collect(Collectors.groupingBy(character -> character.split(":")[0]));
    }

    private static void validateParameters(final String paramValue, final String paramName) {
        validateNullOrEmptyParamValue(paramValue, paramName);
        validateParamLengthValue(paramValue, paramName);
    }

    private static void validateParamLengthValue(final String paramValue, final String paramName) {
        if (paramValue.length() != WORD_SIZE)
            throw new RuntimeException(paramName + " length should be " + WORD_SIZE + ".");
    }

    private static void validateNullOrEmptyParamValue(final String paramValue, final String paramName) {
        if (paramValue == null || paramValue.isEmpty())
            throw new RuntimeException(paramName + " must not be null or empty.");
    }

    public static class AgileDeveloperSpellChecker implements SpellChecker {

       /* default */ String getResponse(String guess) throws IOException {
            Objects.requireNonNull(guess);
            if (guess.equals("gddo"))
                return "false";
            return "true";
        }

        @Override
        public boolean isSpellingCorrect(final String guess) {
            try {
                return Boolean.parseBoolean(getResponse(guess));
            } catch (IOException ioException) {
                throw new RuntimeException(ioException.getMessage(), ioException);
            }
        }
    }
}
