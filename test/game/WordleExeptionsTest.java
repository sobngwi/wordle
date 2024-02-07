package game;

import org.junit.jupiter.api.Test;

import static game.Wordle.evaluate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WordleExeptionsTest extends  WordleTest{

    @Test
    void theLengthOfTheTargetWordShouldBe5(){
        var ex = assertThrows(RuntimeException.class, () -> evaluate("POOR", "FAVOR"));

        assertThat("Target length should be 5.", is(equalTo( ex.getMessage())));
    }
    @Test
    void theLengthOfTheGuessWordShouldBe5(){
        var ex = assertThrows(RuntimeException.class, () -> evaluate("FAVOR", "POOR"));

        assertThat("Guess length should be 5.", is(equalTo( ex.getMessage())));
    }

    @Test
    void targetWordShouldNotBeNull() {
        var ex = assertThrows(RuntimeException.class, () -> evaluate(null, "POOR"));

        assertThat("Target must not be null or empty.", is(equalTo( ex.getMessage())));
    }
    @Test
    void targetWordShouldNotBeEmpty() {
        var ex = assertThrows(RuntimeException.class, () -> evaluate("", "POOR"));

        assertThat("Target must not be null or empty.", is(equalTo( ex.getMessage())));
    }
    @Test
    void guessWordShouldNotBeNull() {
        var ex = assertThrows(RuntimeException.class, () -> evaluate("FAVOR", null));

        assertThat("Guess must not be null or empty.", is(equalTo( ex.getMessage())));
    }
    @Test
    void guessWordShouldNotBeEmpty() {
        var ex = assertThrows(RuntimeException.class, () -> evaluate("FAVOR", null));

        assertThat("Guess must not be null or empty.", is(equalTo( ex.getMessage())));
    }
}
