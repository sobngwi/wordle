package game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static game.MatchLetter.*;
import static game.Wordle.*;
import static game.Wordle.Status.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WordleTest {

    @Nested
    class WordleAlgorithm{
        @Test
        void generateAlgorithm(){
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


    @Nested
    class Play{
        private Wordle.SpellChecker spellChecker = guess -> true;

        @BeforeEach
        void init(){
            Wordle.setSpellCheckerService(spellChecker);
        }
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


    @Nested
    @ExtendWith(MockitoExtension.class)
    class PlayWithDoubles{
        @Mock
        private Wordle.SpellChecker spellChecker;

        @BeforeEach
        void init(){
            Wordle.setSpellCheckerService(spellChecker);
        }
        @Test
        void playFirstAttemptWithCorrectSpellingForGuess(){
            when(spellChecker.isSpellingCorrect(anyString())).thenReturn(true);

            var response = play("FAVOR", "RIVER", 0);

            assertEquals(
                    new Response(1, INPROGRESS, List.of(NO_MATCH, NO_MATCH, EXACT_MATCH, NO_MATCH, EXACT_MATCH), ""),
                    response);

            verify(spellChecker, times(1)).isSpellingCorrect("RIVER");
        }
        @Test
        void playFirstAttemptWithIncorrectSpellingForGuess(){
            when(spellChecker.isSpellingCorrect("RIVRE")).thenReturn(false);

            var response = play("FAVOR", "RIVRE", 0);

            assertEquals(
                    new Response(0, WRONGSPELLING, List.of(NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH), "Incorrect spelling"),
                    response);

            verify(spellChecker, times(1)).isSpellingCorrect("RIVRE");
        }
        @Test
        void playSecondAttemptWithIncorrectSpellingForGuess(){
            when(spellChecker.isSpellingCorrect("RIVER")).thenReturn(false);

            var response = play("FAVOR", "RIVER", 1);

            assertEquals(
                    new Response(1, WRONGSPELLING, List.of(NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH), "Incorrect spelling"),
                    response);

            verify(spellChecker, times(1)).isSpellingCorrect("RIVER");
        }
        @Test
        void playSecondAttemptWithFailureToCheckSpelling(){
            when(spellChecker.isSpellingCorrect("RIVER")).thenThrow(new RuntimeException("Network failure"));

            var ex =
                    assertThrows(RuntimeException.class, () -> play("FAVOR", "RIVER", 1));

            assertEquals("Network failure", ex.getMessage());

            verify(spellChecker, times(1)).isSpellingCorrect("RIVER");
        }
    }

    @Nested
    class AgileDeveloperSpellChecker{
        private Wordle.AgileDeveloperSpellChecker agileDeveloperSpellChecker;

        @BeforeEach
        void init() {
            agileDeveloperSpellChecker = spy(new Wordle.AgileDeveloperSpellChecker());
        }
        @Test
        void getResponseFromServiceForAWord() throws IOException {
            assertFalse(agileDeveloperSpellChecker.getResponse("RIVER").isEmpty());
        }
        @Test
        void isSpellingCorrectForCorrectSpelling() throws IOException {
            assertTrue(agileDeveloperSpellChecker.isSpellingCorrect("good"));

            verify(agileDeveloperSpellChecker, times(1)).getResponse("good");
        }
        @Test
        void isSpellingCorrectForIncorrectSpelling() throws IOException {
           // when(agileDeveloperSpellChecker.isSpellingCorrect("gddo")).thenReturn(false);

            assertFalse(agileDeveloperSpellChecker.isSpellingCorrect("gddo"));

            verify(agileDeveloperSpellChecker, times(1)).getResponse("gddo");
        }
        @Test
        void isSpellingCorrectWithException() throws IOException {
            when(agileDeveloperSpellChecker.getResponse("gddo")).thenThrow(new IOException("Network failure"));

            var ex = assertThrows(RuntimeException.class, () -> agileDeveloperSpellChecker.isSpellingCorrect("gddo"));

            assertEquals("Network failure", ex.getMessage());
            verify(agileDeveloperSpellChecker, times(1)).getResponse("gddo");
        }
    }

    @Nested
    class Exceptions {

        @Test
        void theLengthOfTheTargetWordShouldBe5() {
            var ex = assertThrows(RuntimeException.class, () -> evaluate("POOR", "FAVOR"));

            assertThat("Target length should be 5.", is(equalTo(ex.getMessage())));
        }

        @Test
        void theLengthOfTheGuessWordShouldBe5() {
            var ex = assertThrows(RuntimeException.class, () -> evaluate("FAVOR", "POOR"));

            assertThat("Guess length should be 5.", is(equalTo(ex.getMessage())));
        }

        @Test
        void targetWordShouldNotBeNull() {
            var ex = assertThrows(RuntimeException.class, () -> evaluate(null, "POOR"));

            assertThat("Target must not be null or empty.", is(equalTo(ex.getMessage())));
        }

        @Test
        void targetWordShouldNotBeEmpty() {
            var ex = assertThrows(RuntimeException.class, () -> evaluate("", "POOR"));

            assertThat("Target must not be null or empty.", is(equalTo(ex.getMessage())));
        }

        @Test
        void guessWordShouldNotBeNull() {
            var ex = assertThrows(RuntimeException.class, () -> evaluate("FAVOR", null));

            assertThat("Guess must not be null or empty.", is(equalTo(ex.getMessage())));
        }

        @Test
        void guessWordShouldNotBeEmpty() {
            var ex = assertThrows(RuntimeException.class, () -> evaluate("FAVOR", null));

            assertThat("Guess must not be null or empty.", is(equalTo(ex.getMessage())));
        }
    }
}
