package mikeshafter.iciwi.Tickets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class CustomMachineTest {
  @Test
  void testRelevance() {
    CustomMachine machine = new CustomMachine(null, null);
    
    // Search contains match
    assertEquals(1f, machine.relevance("Shiki", "Shiki"));
    assertEquals(0.5, machine.relevance("kimono", "Kim"));
    assertEquals(0f, machine.relevance("hakase", "z"));
    
    // Search contains only part of match
    assertEquals(1f/11, machine.relevance("Westminster", "East"));
  }
}