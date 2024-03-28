package game;



import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static game.MatchLetter.*;


public final class Wordle {
    private static final int WORD_SIZE = 5;
    private static final Map<Sizes,FourthFunction<Integer, String, String, CharactersInfo, MatchLetter>> SCENARIOS =
            Map.of(new Sizes(0,1), Wordle::computeZeroToOne,
                    new Sizes(1,1), Wordle::computeOneToOne,
                    new Sizes(1, 2), Wordle::computeOneToTwo,
                    new Sizes(2,1), Wordle::computeTwoToOne,
                    new Sizes(2,2), Wordle::computeTwoToTwo);
    private Wordle() {}
    private record Sizes( int targetSize, int guessSize){}
    private record CharactersInfo(Map<String, List<String>> targetCharacters, Map<String, List<String>> guessCharacters){}
    @FunctionalInterface
    public interface FourthFunction<T, U, V,W, R> {
        R apply(T var1, U var2, V var3, W var4 );
    }
    public static List<MatchLetter> evaluate(final String target, final String guess) {
        return
                IntStream.range(0,6).limit(5).mapToObj(position -> computeMatchingAtPosition(position, target, guess)).toList();
    }

    private static MatchLetter computeMatchingAtPosition(final int position, final String target, final String guess) {
        return chooseScenarioToApply( position, target, guess);
    }
    private static MatchLetter chooseScenarioToApply(final int position, final String target, final String guess){
        final CharactersInfo charactersInfo = new CharactersInfo( groupCharacters(target), groupCharacters(guess));
        final String guessKey = guess.charAt(position) + "";
        final List<String> guesses =charactersInfo.guessCharacters().getOrDefault(guessKey, List.of());
        final List<String> targets = charactersInfo.targetCharacters().getOrDefault(guessKey, List.of());
        return SCENARIOS.getOrDefault(new Sizes(targets.size(), guesses.size()), (pos, tar, gue, info) -> NO_MATCH).apply(position, target, guess, charactersInfo);
    }
    private static Map<String, List<String>> groupCharacters(final String aString) {
        return IntStream.range(0, WORD_SIZE)
                .mapToObj(position -> aString.charAt(position) + ":" + position)
                .sorted(Comparator.comparing(key -> key.split(":")[0]))
                .collect(Collectors.groupingBy(character -> character.split(":")[0]));
    }
    private static MatchLetter computeZeroToOne(final int position, final String target, final String guess, CharactersInfo charactersInfo) {
        return NO_MATCH;
    }
    private static MatchLetter computeOneToOne(final int position, final String target, final String guess,final CharactersInfo charactersInfo) {
        final String guessKey = guess.charAt(position) + "";
        final List<String> targets = charactersInfo.targetCharacters().get(guessKey);
        final List<String> guesses = charactersInfo.guessCharacters().get(guessKey);
        final boolean isExactMatch = targets.equals(guesses);
        final Map<Boolean, MatchLetter> results = Map.of(true, EXACT_MATCH);
        return results.getOrDefault(isExactMatch, PARTIAL_MATCH);
    }
    private static MatchLetter computeOneToTwo(final int position, final String target, final String guess, final CharactersInfo charactersInfo) {
        final String guessKey = guess.charAt(position) + "";
        final List<String> guesses = charactersInfo.guessCharacters().getOrDefault(guessKey, List.of());
        final List<String> targets = charactersInfo.targetCharacters().getOrDefault(guessKey, List.of());
        final boolean isPartialMatch =  position < getPositionOfFirstElementInList(targets);
        final boolean isExactMatch = guesses.contains(targets.getFirst()) && position == getPositionOfFirstElementInList(targets);
        final Map<Boolean, MatchLetter> exacts = Map.of(true, EXACT_MATCH);
        final Map<Boolean, MatchLetter> partials = Map.of(true, PARTIAL_MATCH);
        return exacts.getOrDefault(isExactMatch, partials.getOrDefault(isPartialMatch,NO_MATCH));
    }
    private static MatchLetter computeTwoToTwo(final int position, final String target, final String guess, final CharactersInfo charactersInfo) {
        final String guessKey = guess.charAt(position) + "";
        final List<String> guesses = charactersInfo.guessCharacters().getOrDefault(guessKey, List.of());
        final List<String> targets = charactersInfo.targetCharacters().getOrDefault(guessKey, List.of());
        final Map<Integer, MatchLetter> positionsResults = getIntegerMatchLetterMap(guesses, targets);
        final boolean isExactMatch = positionsResults.get(position) == EXACT_MATCH;
        final Map<Boolean, MatchLetter> exacts = Map.of(true, EXACT_MATCH);
        return exacts.getOrDefault(isExactMatch, PARTIAL_MATCH);
    }

    private static Map<Integer, MatchLetter> getIntegerMatchLetterMap(final List<String> guesses, final List<String> targets) {
        final  Map<Integer,MatchLetter> positionsResults = new HashMap<>();
        for (final String guess : guesses) {
            for (final String target : targets) {
                if (target.equals(guess)) {
                    positionsResults.put(Integer.parseInt(guess.split(":")[1]), EXACT_MATCH);
                }
            }
        }
        return positionsResults;
    }

    private static MatchLetter computeTwoToOne(final int position, final String target, final String guess, final CharactersInfo charactersInfo) {
        final String guessKey = guess.charAt(position) + "";
        final List<String> guesses = charactersInfo.guessCharacters().getOrDefault(guessKey, List.of());
        final List<String> targets = charactersInfo.targetCharacters().getOrDefault(guessKey, List.of());
        final boolean isExactMatch = targets.contains(guesses.getFirst()) && position == getPositionOfFirstElementInList(guesses);
        final Map<Boolean, MatchLetter> results = Map.of(true, EXACT_MATCH);
        return results.getOrDefault(isExactMatch, PARTIAL_MATCH);
    }
    private static int getPositionOfFirstElementInList(final List<String> strings){
        return Integer.parseInt(strings.getFirst().charAt(2) + "");
    }

}
