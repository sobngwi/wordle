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
            compute(targets, guesses, i, results);
        });
        return Arrays.stream(results).toList();
    }

    private static void compute(char[] targets, char[] guesses, int position, MatchLetter[] results ) {
        Map<String, List<String>> guessCharacterLists = groupCharacters(guesses);
        System.out.println("guessCharacterLists = " + guessCharacterLists);
        Map<String, List<String>> targetCharacterLists = groupCharacters(targets);
        System.out.println("targetCharacterLists = " + targetCharacterLists);
        System.out.println("treating character " + guesses[position] + "  --- At position : " + position );
        System.out.println(" guess info : " +  guessCharacterLists.get( guesses[position] + ""));
        System.out.println(" target info : " + targetCharacterLists.get( guesses[position] + ""));
       /* {s=[s:0], i=[i:2], k=[k:1], l=[l:3, l:4]} */
       /* {c=[c:0], v=[v:2], i=[i:1, i:3], l=[l:4]} */
       /* if ( targetCharacterLists.get( guesses[position] + "") == null ) {
            System.out.println("Will set the result to NO_MATCH");
            results[position] = NO_MATCH;
            return;
        } else */
        if ( targetCharacterLists.get( guesses[position] + "") == null )
            return;
        if ( guessCharacterLists.get( guesses[position] + "").size() ==  targetCharacterLists.get( guesses[position] + "").size()
        && guessCharacterLists.get( guesses[position] + "").size() == 1) {
            System.out.println("Same Size 1 :" + guessCharacterLists.get(guesses[position] + "").size());
            if ( guessCharacterLists.get( guesses[position] + "").equals(targetCharacterLists.get( guesses[position] + "")) ) {
                results[position] = EXACT_MATCH;
                return;
            }
            else {
                results[position] = PARTIAL_MATCH;
                return;
            }
        }
        if ( guessCharacterLists.get( guesses[position] + "").size() ==  targetCharacterLists.get( guesses[position] + "").size() ) {
            System.out.println("Same Size :" + guessCharacterLists.get( guesses[position] + "").size());
            for (int i = 0; i < guessCharacterLists.get( guesses[position] + "").size(); i++) {
                if ( targetCharacterLists.get( guesses[position] + "").contains( guessCharacterLists.get( guesses[position] + "").get(i))
                 ) {
                    System.out.println("Same Size :" + "EXACT");
                    results[Integer.parseInt(guessCharacterLists.get( guesses[position] + "").get(i).split(":")[1])] = EXACT_MATCH;
                    System.out.println("results22 EXACT = " + Arrays.toString(results));
                }
                else {
                    System.out.println("Same Size :" + "PARTIAL" + " @Position " + Integer.parseInt(guessCharacterLists.get(guesses[position] + "").get(i).split(":")[1]));
                    results[Integer.parseInt(guessCharacterLists.get(guesses[position] + "").get(i).split(":")[1])] = PARTIAL_MATCH;
                    System.out.println("results 22 PARTIAL = " + Arrays.toString(results));
                }
            }
           // results[position] = EXACT_MATCH;
            return;
        }
        else {

            if ( guessCharacterLists.get( guesses[position] + "").size() == 2 && targetCharacterLists.get( guesses[position] + "").size() == 1 ) {
                System.out.println("2-1");
                 if ( guessCharacterLists.get( guesses[position] + "").getFirst().equals(targetCharacterLists.get( guesses[position] + "").getFirst()) )
                 {
                     System.out.println("2-1a Will set the result to EXACT_MATCH");
                     results[Integer.parseInt(guessCharacterLists.get( guesses[position] + "").getFirst().split(":")[1])] = EXACT_MATCH;
                     return;
                 }
                if ( guessCharacterLists.get( guesses[position] + "").getLast().equals(targetCharacterLists.get( guesses[position] + "").getLast()) )
                {
                    System.out.println("2-1b Will set the result to EXACT_MATCH");
                    results[Integer.parseInt(guessCharacterLists.get( guesses[position] + "").getLast().split(":")[1])] = EXACT_MATCH;
                    return;
                }
                System.out.println("Will set the result to PARTIAL_MATCH and NO_MATCH");
                int positionResultToSet = Integer.parseInt(guessCharacterLists.get(guesses[position] + "").getFirst().split(":")[1]);
                System.out.println("positionResultToSet = " + positionResultToSet);
                results[positionResultToSet] = PARTIAL_MATCH;
                int j = Integer.parseInt(guessCharacterLists.get( guesses[position] + "").getLast().split(":")[1]);
                results[j] = NO_MATCH;
                System.out.println("results = " + Arrays.toString(results));
                return;

            }
            if ( guessCharacterLists.get( guesses[position] + "").size() == 1 && targetCharacterLists.get( guesses[position] + "").size() == 2 ) {
                if ( targetCharacterLists.get( guesses[position] + "").contains( guessCharacterLists.get( guesses[position] + "").getFirst())) {
                    System.out.println("1-2 Will set the result to EXACT_MATCH");
                    results[position] = EXACT_MATCH;
                    return;
                }
                else {
                    System.out.println("1-2 Will set the result to PARTIAL MATCH");
                    results[position] = PARTIAL_MATCH;
                    return ;
                }

            }
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
