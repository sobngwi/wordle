package game;

import org.junit.jupiter.api.Test;

import java.util.List;
import static game.Wordle.evaluate;
import static game.MatchLetter.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
public class SuccessFullTest {

    @Test
    void evaluateForTargetFAVORAndGuestFAVORAtFirstAttempt() {

        var response =  evaluate("FAVOR", "FAVOR");

        assertThat(response, is (equalTo(List.of(EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH))));
    }

    @Test
    void strawIn4Attempts() {
        assertEquals(List.of(NO_MATCH, PARTIAL_MATCH, NO_MATCH, NO_MATCH, PARTIAL_MATCH), evaluate("STRAW", "FAVOR"));
        assertEquals(List.of(PARTIAL_MATCH, PARTIAL_MATCH, PARTIAL_MATCH, NO_MATCH, NO_MATCH), evaluate("STRAW", "TRAIN"));
        assertEquals(List.of(EXACT_MATCH, NO_MATCH, EXACT_MATCH, EXACT_MATCH, NO_MATCH), evaluate("STRAW", "SPRAY"));
        assertEquals(List.of(EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH), evaluate("STRAW", "STRAW"));
    }

}
