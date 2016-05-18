package net.nativestruct;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MapReduceTest {
    @Test
    public void wordCount() {

        StructVector<WordCount> words = new StructVector<>(WordCount.class);

        List<String> lines = Arrays.asList("word count is a simple application",
                "that counts the number of occurrences of each word in a given input set",
                "quick brown fox jumps over a lazy dog");

        for (String line : lines) {
            for (String word : line.toLowerCase().split("\\s+")) {
                words.insertLast();
                words.accessor().setWord(word);
                words.accessor().setCount(1);
            }
        }

        StructVector<WordCount> wordCounts = words.reduceBy("word")
                .into(new StructVector<>(WordCount.class))
                .with((accumulator, value)
                    -> accumulator.setCount(accumulator.getCount() + value.getCount()));

        wordCounts.current(0);
        assertEquals("a", wordCounts.accessor().getWord());
        assertEquals(3, wordCounts.accessor().getCount());
        wordCounts.current(1);
        assertEquals("application", wordCounts.accessor().getWord());
        assertEquals(1, wordCounts.accessor().getCount());

        wordCounts.current(wordCounts.size() - 1);
        assertEquals("word", wordCounts.accessor().getWord());
        assertEquals(2, wordCounts.accessor().getCount());
    }

    public static abstract class WordCount extends AbstractStruct {
        @StructField
        public abstract String getWord();
        @StructField
        public abstract void setWord(String word);

        @StructField
        public abstract int getCount();
        @StructField
        public abstract void setCount(int count);
    }
}
