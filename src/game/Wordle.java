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
            var targetInfoCharacter = searchForTargetCharacter(guessInfoCharacter, targetsInfo);
            if (targetInfoCharacter != null && guessInfoCharacter.positions.size() == 2 && targetInfoCharacter.positions.size()==1)
                applyMatchingCharacterIn1TargetFor2GuessCharactersRules(guessInfoCharacter, targetInfoCharacter, results);
            else if(targetInfoCharacter != null && targetInfoCharacter.positions.size() >1 && targetInfoCharacter.positions.size() >= guessInfoCharacter.positions.size())
                applyMatchingCharactersIn2TargetsFor1Or2GuessCharacterRules(guessInfoCharacter, targetInfoCharacter, results);
            else if (targetInfoCharacter != null)
                applyNormaRulesCharacter(guessInfoCharacter, targetInfoCharacter, results);
        }
        return Arrays.stream(results).toList();
    }

    private static void applyMatchingCharactersIn2TargetsFor1Or2GuessCharacterRules(InfoCharacter guessInfoCharacter, InfoCharacter targetInfoCharacter, MatchLetter[] results) {
        var guessPositions  = guessInfoCharacter.positions;
        var targetPositions  = targetInfoCharacter.positions;
        applyMatchingCharactersIn2TargetsFor1GuessCharacterRules(targetPositions,guessInfoCharacter, targetInfoCharacter, results);
        applyRuleForCharacterHavingSameLengthInBothSide(guessPositions, targetPositions,guessInfoCharacter, targetInfoCharacter, results);
    }

    private static void applyMatchingCharactersIn2TargetsFor1GuessCharacterRules ( List<Integer> targetPositions,
                                                                                   InfoCharacter guessInfoCharacter, InfoCharacter targetInfoCharacter, MatchLetter[] results){
        applyMatchingCharactersIn2TargetsFor1GuessPartialMatchCharacterRules(guessInfoCharacter, results);
        applyMatchingCharactersIn2TargetsFor1GuessExactMatchRuleCharacterRules(targetPositions,guessInfoCharacter, targetInfoCharacter, results );

    }
    private static void applyMatchingCharactersIn2TargetsFor1GuessExactMatchRuleCharacterRules ( List<Integer> targetPositions, InfoCharacter guessInfoCharacter,  InfoCharacter targetInfoCharacter, MatchLetter[] results){
        for (Integer targetPosition : targetPositions) {
            if (Objects.equals(targetPosition, guessInfoCharacter.positions.getFirst())) {
                results[Character.getNumericValue(guessInfoCharacter.key.charAt(2))] = EXACT_MATCH;
            }
        }
    }
    private static void applyMatchingCharactersIn2TargetsFor1GuessPartialMatchCharacterRules (  InfoCharacter guessInfoCharacter, MatchLetter[] results){
        results[Character.getNumericValue(guessInfoCharacter.key.charAt(2))] = PARTIAL_MATCH;
    }
    private static void applyRuleForSameCharacterHavingSameLengthInBothSide(List<Integer> guessPositions, List<Integer> targetPositions, InfoCharacter guessInfoCharacter, MatchLetter[] results){
        if (guessPositions.equals( targetPositions)) {
            for (int i = 0; i < guessPositions.size(); i++)
                results[guessInfoCharacter.positions.get(i)] = EXACT_MATCH ;
        }
    }
    private static void  applyRuleForSameCharacterHavingDifferentLengthInBothSide(List<Integer> guessPositions, InfoCharacter guessInfoCharacter, InfoCharacter targetInfoCharacter, MatchLetter[] results) {
        for (int i = 0; i < guessPositions.size(); i++) {
            for (int j = 0 ; j < guessPositions.size(); j ++ )
                if (Objects.equals(guessInfoCharacter.positions.get(i), targetInfoCharacter.positions.get(j))) {
                    results[guessInfoCharacter.positions.get(i)] = EXACT_MATCH;
                    break;
                }
                else results[guessInfoCharacter.positions.get(i)] = PARTIAL_MATCH;
        }
    }
    private static void applyRuleForCharacterHavingSameLengthInBothSide(List<Integer> guessPositions, List<Integer> targetPositions,
                                                                        InfoCharacter guessInfoCharacter, InfoCharacter targetInfoCharacter,  MatchLetter[] results) {
        if ( targetInfoCharacter.positions.size() == 2 && guessInfoCharacter.positions.size() == 2 ) {
            applyRuleForSameCharacterHavingSameLengthInBothSide(guessPositions, targetPositions, guessInfoCharacter, results);
            applyRuleForSameCharacterHavingDifferentLengthInBothSide(guessPositions, guessInfoCharacter,targetInfoCharacter,results);

        }
    }
    private static void applyNormaRulesCharacter(InfoCharacter guessInfoCharacter, InfoCharacter targetInfoCharacter, MatchLetter[] results) {
        if ( guessInfoCharacter.key.charAt(2) == targetInfoCharacter.key.charAt(2)) {
            results[Character.getNumericValue(guessInfoCharacter.key.charAt(2))] = EXACT_MATCH ;
        }
        else results[Character.getNumericValue(guessInfoCharacter.key.charAt(2))] = PARTIAL_MATCH;
    }

    private static void applyMatchingCharacterIn1TargetFor2GuessCharactersExactMatchRules(List<Integer> guessPositions, List<Integer> targetPositions,MatchLetter[] results ) {
        if ( guessPositions.contains(targetPositions.getFirst())) {
            for (Integer guessPosition : guessPositions) {
                if (Objects.equals(guessPosition, targetPositions.getFirst())) {
                    results[guessPosition] = EXACT_MATCH;
                    return;
                }
            }
        }
    }

    private static void applyMatchingCharacterIn1TargetFor2GuessCharactersPartMatchRules(List<Integer> guessPositions, List<Integer> targetPositions,MatchLetter[] results ) {
        if ( ! guessPositions.contains(targetPositions.getFirst())) {
            for (Integer guessPosition : guessPositions) {
                results[guessPosition] = PARTIAL_MATCH;
                return;
            }
        }
    }

    private static void applyMatchingCharacterIn1TargetFor2GuessCharactersRules(InfoCharacter guessInfoCharacter, InfoCharacter targetInfoCharacter, MatchLetter[] results) {
        var guessPositions  = guessInfoCharacter.positions;
        var targetPositions = targetInfoCharacter.positions;
        applyMatchingCharacterIn1TargetFor2GuessCharactersExactMatchRules(guessPositions, targetPositions,results);
        applyMatchingCharacterIn1TargetFor2GuessCharactersPartMatchRules (guessPositions,targetPositions, results);

    }

    private static InfoCharacter searchForTargetCharacter(InfoCharacter infoCharacter, List<InfoCharacter> targetsInfo) {
        for ( int i = 0 ; i < targetsInfo.size(); i++) {
            var targetChar = targetsInfo.get(i);
            if ( infoCharacter.key.charAt(0) == targetChar.key.charAt(0))
                return targetsInfo.get(i) ;
        }
        return null;
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
