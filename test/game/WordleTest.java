package game;

import org.junit.jupiter.api.Test;

import java.util.List;

import static game.MatchLetter.EXACT_MATCH;
import static game.Wordle.evaluate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class WordleTest {

    @Test
    void canary(){
        assertThat(true, is(equalTo(true)));
    }

    @Test
    void evaluateForTargetFAVORAndGuestFAVORAtFirstAttempt() {

       var response =  evaluate("FAVOR", "FAVOR");

       assertThat(response, is (equalTo(List.of(EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH))));
    }
}
