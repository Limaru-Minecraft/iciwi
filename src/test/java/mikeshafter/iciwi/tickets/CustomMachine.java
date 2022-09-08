package mikeshafter.iciwi.tickets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class CustomMachine {
  @Test
  void testRelevance() {
    
    // Search contains match
    assertEquals(1f, relevance("Shiki", "Shiki"));
    assertEquals(0.5, relevance("Kim", "kimono"));
    assertEquals(0f, relevance("z", "hakase"));
    
    // Search contains only part of match
    assertEquals(1f/11, relevance("East", "Westminster"));
  }

  @Test
  void testSort() {
    
  }
}