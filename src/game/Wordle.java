package game;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

        IntStream.range(0, 5).limit(5).forEach( i -> {
            results[i] = compute(targets, guesses, i);
        });
        return Arrays.stream(results).toList();
    }

    private static MatchLetter compute(char[] targets, char[] guesses, int position ) {
        Map<String, List<String>> guessCharacterLists = groupCharacters(guesses);
        Map<String, List<String>> targetCharacterLists = groupCharacters(targets);
       /* System.out.println("guessCharacterLists = " + guessCharacterLists);
        System.out.println("targetCharacterLists = " + targetCharacterLists);*/


        if (targetCharacterLists.get(guesses[position] + "") == null)
            return NO_MATCH;
        if (guessCharacterLists.get(guesses[position] + "").size() == targetCharacterLists.get(guesses[position] + "").size()
                && guessCharacterLists.get(guesses[position] + "").size() == 1) {
            System.out.println("Same Size 1 :" + guessCharacterLists.get(guesses[position] + "").size());
            if (guessCharacterLists.get(guesses[position] + "").equals(targetCharacterLists.get(guesses[position] + ""))) {
                return EXACT_MATCH;
            } else {
                return PARTIAL_MATCH;
            }
        }
        if (guessCharacterLists.get(guesses[position] + "").size() == targetCharacterLists.get(guesses[position] + "").size()) {
            MatchLetter[] willReturn = new MatchLetter[]{PARTIAL_MATCH, PARTIAL_MATCH};
            for (int i = 0; i < guessCharacterLists.get(guesses[position] + "").size(); i++) {
                for (int j = 0; j < targetCharacterLists.get(guesses[position] + "").size(); j++) {
                    if (guessCharacterLists.get(guesses[position] + "").get(i).equals(targetCharacterLists.get(guesses[position] + "").get(j))
                    ) {
                        willReturn[i] = EXACT_MATCH;
                    }
                }
            }
            if (position == Integer.parseInt(guessCharacterLists.get(guesses[position] + "").getFirst().split(":")[1]))
                return willReturn[0];
            else return willReturn[1];
        } else {
            if (guessCharacterLists.get(guesses[position] + "").size() == 2 && targetCharacterLists.get(guesses[position] + "").size() == 1) {
                System.out.println("2-1 @Position : " + position);
                System.out.println("2-1 guess  : " + guessCharacterLists.get(guesses[position] + ""));
                System.out.println("2-1 target  : " + targetCharacterLists.get(guesses[position] + ""));
                if (guessCharacterLists.get(guesses[position] + "").contains(targetCharacterLists.get(guesses[position] + "").getFirst())) {
                    System.out.println(" ?2-1a Will set the result to EXACT_MATCH @Position: " + position);
                    if (guessCharacterLists.get(guesses[position] + "").getFirst().equals(targetCharacterLists.get(guesses[position] + "").getFirst())
                    && position == Integer.parseInt(guessCharacterLists.get(guesses[position] + "").getFirst().split(":")[1] )
                    ) {
                        System.out.println("2-1a Will set the result to EXACT_MATCH @Position: " + position);
                        //results[Integer.parseInt(guessCharacterLists.get( guesses[position] + "").getFirst().split(":")[1])] = EXACT_MATCH;
                        return EXACT_MATCH;
                    }
                    if (guessCharacterLists.get(guesses[position] + "").getLast().equals(targetCharacterLists.get(guesses[position] + "").getFirst())
                            && position == Integer.parseInt(guessCharacterLists.get(guesses[position] + "").getLast().split(":")[1] )
                    ) {
                        System.out.println("2-1b Will set the result to EXACT_MATCH @ Position: " + position);
                        return EXACT_MATCH;
                    }


                } else { // PARTIAL MATCH
                    System.out.println( " ? 2-1c Will set the result to EXACT_MATCH @ Position: " + position);
                    System.out.println("Currrent guest position :" + Integer.parseInt(guessCharacterLists.get(guesses[position] + "").getFirst().split(":")[1]));
                    System.out.println("Target position = " + Integer.parseInt(targetCharacterLists.get(guesses[position] + "").getFirst().split(":")[1]));
                    if (position == Integer.parseInt(guessCharacterLists.get(guesses[position] + "").getFirst().split(":")[1])
                            && Integer.parseInt(targetCharacterLists.get(guesses[position] + "").getFirst().split(":")[1]) > position) {
                        System.out.println( "2-1c Will set the result to PARTIAL_MATCH @ Position: " + position);
                        return PARTIAL_MATCH;
                    }
                    else {
                        System.out.println( "2-1d Will set the result to NO_MATCH @ Position: " + position);
                        return NO_MATCH;
                    }
                }
                if (guessCharacterLists.get(guesses[position] + "").size() == 1 && targetCharacterLists.get(guesses[position] + "").size() == 2) {
                    if (targetCharacterLists.get(guesses[position] + "").contains(guessCharacterLists.get(guesses[position] + "").getFirst())) {
                        System.out.println("1-2 Will set the result to EXACT_MATCH");
                        return EXACT_MATCH;
                    } else {
                        System.out.println("1-2 Will set the result to PARTIAL MATCH");
                        return PARTIAL_MATCH;
                    }
                }
            }
            if (guessCharacterLists.get(guesses[position] + "").size() == 1 && targetCharacterLists.get(guesses[position] + "").size() == 2) {
                System.out.println("1-2 @Position : " + position);
                System.out.println("1-2 guess  : " + guessCharacterLists.get(guesses[position] + ""));
                System.out.println("1-2 target  : " + targetCharacterLists.get(guesses[position] + ""));
                if (targetCharacterLists.get(guesses[position] + "").getFirst().equals(guessCharacterLists.get(guesses[position] + "").getFirst())) {
                    System.out.println("1-2a Will set the result to EXACT_MATCH @Position: " + position);
                    //results[Integer.parseInt(guessCharacterLists.get( guesses[position] + "").getFirst().split(":")[1])] = EXACT_MATCH;
                    return EXACT_MATCH;
                } else if (targetCharacterLists.get(guesses[position] + "").getLast().equals(guessCharacterLists.get(guesses[position] + "").getFirst())) {
                    System.out.println("1-2b Will set the result to EXACT_MATCH @ Position: " + position);
                    return EXACT_MATCH;
                }
                else {
                    System.out.println( " ? 1-2c Will set the result to PARTIAL  @ Position: " + position);
                        return  PARTIAL_MATCH;
                }
            }
            return NO_MATCH;
        }
    }

    private static Map<String, List<String>> groupCharacters(char[] guesses) {
        return IntStream.range(0, 5)
                .mapToObj(i -> guesses[i] + ":" + i)
                .sorted(Comparator.comparing(k -> k.split(":")[0]))
                .collect(Collectors.groupingBy(x -> x.split(":")[0]));
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
