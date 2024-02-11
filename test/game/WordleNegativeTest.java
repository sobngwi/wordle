package game;

import org.junit.jupiter.api.Test;

import java.util.List;
import static game.Wordle.evaluate;
import static game.MatchLetter.*;
import static org.junit.jupiter.api.Assertions.*;
public class WordleNegativeTest {
    @Test
    void allInOneAssertions(){
        assertAll(
                () -> assertEquals(List.of(PARTIAL_MATCH, EXACT_MATCH, NO_MATCH, NO_MATCH, NO_MATCH), evaluate("FAVOR", "RAPID")),
                () -> assertEquals(List.of(NO_MATCH, NO_MATCH, EXACT_MATCH, NO_MATCH, EXACT_MATCH), evaluate("FAVOR", "RIVER")),
                () -> assertEquals(List.of(NO_MATCH, EXACT_MATCH, NO_MATCH, EXACT_MATCH, EXACT_MATCH), evaluate("FAVOR", "MAYOR")),
                () -> assertEquals(List.of(PARTIAL_MATCH, NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH), evaluate("FAVOR", "AMAST")),
                () -> assertEquals(List.of(EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH), evaluate("SKILL", "SKILL")),
                () -> assertEquals(List.of(EXACT_MATCH, NO_MATCH, EXACT_MATCH, NO_MATCH, EXACT_MATCH), evaluate("SKILL", "SWIRL")),
                () -> assertEquals(List.of(NO_MATCH, PARTIAL_MATCH, NO_MATCH, NO_MATCH, EXACT_MATCH), evaluate("SKILL", "CIVIL")),
                () -> assertEquals(List.of(EXACT_MATCH, NO_MATCH, EXACT_MATCH, NO_MATCH, NO_MATCH), evaluate("SKILL", "SHIMS")),
                () -> assertEquals(List.of(EXACT_MATCH, PARTIAL_MATCH, PARTIAL_MATCH, EXACT_MATCH, NO_MATCH), evaluate("SKILL", "SILLY")),
                () -> assertEquals(List.of(EXACT_MATCH, PARTIAL_MATCH, EXACT_MATCH, NO_MATCH, NO_MATCH), evaluate("SKILL", "SLICE")),
                () -> assertEquals(List.of(PARTIAL_MATCH, NO_MATCH, PARTIAL_MATCH, PARTIAL_MATCH, NO_MATCH), evaluate("SAGAS", "ABASE"))
        );
    }

    @Test
    void createAlgorithmWithThisScenarioTest(){
        assertEquals(List.of(NO_MATCH, NO_MATCH, EXACT_MATCH, NO_MATCH, EXACT_MATCH), evaluate("FAVOR", "RIVER"));
        assertEquals(List.of(PARTIAL_MATCH, NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH), evaluate("FAVOR", "AMAST"));
        assertEquals(List.of(NO_MATCH, EXACT_MATCH, NO_MATCH, EXACT_MATCH, EXACT_MATCH), evaluate("FAVOR", "MAYOR"));
        assertEquals(List.of(PARTIAL_MATCH, EXACT_MATCH, NO_MATCH, NO_MATCH, NO_MATCH), evaluate("FAVOR", "RAPID"));

        assertEquals(List.of(EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH), evaluate("SKILL", "SKILL"));
        assertEquals(List.of(NO_MATCH, PARTIAL_MATCH, NO_MATCH, NO_MATCH, EXACT_MATCH), evaluate("SKILL", "CIVIL"));
        assertEquals(List.of(EXACT_MATCH, NO_MATCH, EXACT_MATCH, NO_MATCH, EXACT_MATCH), evaluate("SKILL", "SWIRL"));
        assertEquals(List.of(EXACT_MATCH, NO_MATCH, EXACT_MATCH, NO_MATCH, NO_MATCH), evaluate("SKILL", "SHIMS"));
        assertEquals(List.of(EXACT_MATCH, PARTIAL_MATCH, EXACT_MATCH, NO_MATCH, NO_MATCH), evaluate("SKILL", "SLICE"));
        assertEquals(List.of(EXACT_MATCH, PARTIAL_MATCH, PARTIAL_MATCH, EXACT_MATCH, NO_MATCH), evaluate("SKILL", "SILLY"));
        assertEquals(List.of(PARTIAL_MATCH, NO_MATCH, PARTIAL_MATCH, PARTIAL_MATCH, NO_MATCH), evaluate("SAGAS", "ABASE"));
        assertEquals(List.of(PARTIAL_MATCH, PARTIAL_MATCH, PARTIAL_MATCH, PARTIAL_MATCH, PARTIAL_MATCH), evaluate("PANIC", "NICAP"));


    }
}
