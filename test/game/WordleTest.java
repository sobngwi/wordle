package game;

import org.junit.jupiter.api.Test;

import java.util.List;

import static game.MatchLetter.*;
import static game.MatchLetter.NO_MATCH;
import static game.Wordle.evaluate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WordleTest {

    @Test
    void canary(){
        //assertThat(true, is(equalTo(true)));
        assertEquals(List.of(NO_MATCH, PARTIAL_MATCH, NO_MATCH, NO_MATCH, EXACT_MATCH), evaluate("SKILL", "CIVIL"));
        assertEquals(List.of(EXACT_MATCH, NO_MATCH, EXACT_MATCH, NO_MATCH, EXACT_MATCH), evaluate("SKILL", "SWIRL"));
        assertEquals(List.of(EXACT_MATCH, PARTIAL_MATCH, EXACT_MATCH, NO_MATCH, NO_MATCH), evaluate("SKILL", "SLICE"));
        assertEquals(List.of(EXACT_MATCH, NO_MATCH, EXACT_MATCH, NO_MATCH, NO_MATCH), evaluate("SKILL", "SHIMS"));
        assertEquals(List.of(EXACT_MATCH, PARTIAL_MATCH, PARTIAL_MATCH, EXACT_MATCH, NO_MATCH), evaluate("SKILL", "SILLY"));
        assertEquals(List.of(EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH), evaluate("SKILL", "SKILL"));

        assertEquals(List.of(PARTIAL_MATCH, NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH), evaluate("FAVOR", "AMAST"));
        assertEquals(List.of(NO_MATCH, EXACT_MATCH, NO_MATCH, EXACT_MATCH, EXACT_MATCH), evaluate("FAVOR", "MAYOR"));
        assertEquals(List.of(PARTIAL_MATCH, EXACT_MATCH, NO_MATCH, NO_MATCH, NO_MATCH), evaluate("FAVOR", "RAPID"));
        assertEquals(List.of(PARTIAL_MATCH, NO_MATCH, PARTIAL_MATCH, PARTIAL_MATCH, NO_MATCH), evaluate("SAGAS", "ABASE"));
        assertEquals(List.of(PARTIAL_MATCH, PARTIAL_MATCH, PARTIAL_MATCH, PARTIAL_MATCH, PARTIAL_MATCH), evaluate("PANIC", "NICAP"));
        assertEquals(List.of(NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH), evaluate("GUEST", "FAVOR"));

        assertEquals(List.of(NO_MATCH, NO_MATCH, EXACT_MATCH, NO_MATCH, EXACT_MATCH), evaluate("FAVOR", "RIVER"));
    }

}
