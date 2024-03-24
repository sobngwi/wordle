package game;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static game.MatchLetter.*;
import static game.MatchLetter.NO_MATCH;
import static game.Wordle.evaluate;
import static game.Wordle.play;
import static game.Wordle.Response;
import static game.Wordle.Status.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Nested
    class Play{
        @Test
        void playFirstAttemptWithWinningGuess(){
            var response = play("FAVOR", "FAVOR", 0);

            assertEquals(
                    new Response(1, WON, List.of(EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH), "Amazing"),
                    response);
        }
        @Test
        void playFirstAttemptInvalidGuess(){
            var ex = assertThrows(RuntimeException.class, () -> play("FAVOR", "FOR", 1));

            assertEquals("Guess length should be 5.", ex.getMessage());
        }
        @Test
        void playFirstAttemptWithNonWinningGuess(){
            var response = play("FAVOR", "RIVER", 0);

            assertEquals(
                    new Response(1, INPROGRESS, List.of(NO_MATCH, NO_MATCH, EXACT_MATCH, NO_MATCH, EXACT_MATCH), ""),
                    response);
        }

        @Test
        void playSecondAttemptWithWinningGuess(){
            var response = play("FAVOR", "FAVOR", 1);

            assertEquals(
                    new Response(2, WON, List.of(EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH), "Splendid"),
                    response);
        }
        @Test
        void playThirdAttemptWithWinningGuess(){
            var response = play("FAVOR", "FAVOR", 2);

            assertEquals(
                    new Response(3, WON, List.of(EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH), "Awesome"),
                    response);
        }
        @Test
        void playThirdAttemptWithNonWinningGuess(){
            var response = play("FAVOR", "RIVER", 2);

            assertEquals(
                    new Response(3, INPROGRESS, List.of(NO_MATCH, NO_MATCH, EXACT_MATCH, NO_MATCH, EXACT_MATCH), ""),
                    response);
        }
        @Test
        void playFourthAttemptWithWinningGuess(){
            var response = play("FAVOR", "FAVOR", 3);

            assertEquals(
                    new Response(4, WON, List.of(EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH), "Yay"),
                    response);
        }

        @Test
        void playFourthAttemptWithNonWinningGuess(){
            var response = play("FAVOR", "RIVER", 3);

            assertEquals(
                    new Response(4, INPROGRESS, List.of(NO_MATCH, NO_MATCH, EXACT_MATCH, NO_MATCH, EXACT_MATCH), ""),
                    response);
        }
        @Test
        void playFiftyAttemptWithWinningGuess(){
            var response = play("FAVOR", "FAVOR", 4);

            assertEquals(
                    new Response(5, WON, List.of(EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH), "Yay"),
                    response);
        }
        @Test
        void playFifthAttemptWithNonWinningGuess(){
            var response = play("FAVOR", "RIVER", 4);

            assertEquals(
                    new Response(5, INPROGRESS, List.of(NO_MATCH, NO_MATCH, EXACT_MATCH, NO_MATCH, EXACT_MATCH), ""),
                    response);
        }
        @Test
        void playSixthAttemptWithWinningGuess(){
            var response = play("FAVOR", "FAVOR", 5);

            assertEquals(
                    new Response(6, WON, List.of(EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH, EXACT_MATCH), "Yay"),
                    response);
        }
        @Test
        void playSixthAttemptWithNonWinningGuess(){
            var response = play("FAVOR", "RIVER", 5);

            assertEquals(
                    new Response(6, LOST, List.of(NO_MATCH, NO_MATCH, EXACT_MATCH, NO_MATCH, EXACT_MATCH), ""),
                    response);
        }
        @Test
        void playSeventhAttemptWithWinningGuess(){
            var ex = assertThrows(RuntimeException.class,
                    () -> play("FAVOR", "FAVOR", 6));

            assertEquals("Game Over", ex.getMessage());
        }
        @Test
        void playEightAttemptWithNonWinningGuess(){
            var ex = assertThrows(RuntimeException.class,
                    () -> play("FAVOR", "RAPID", 7));

            assertEquals("Game Over", ex.getMessage());
        }

    }

}
