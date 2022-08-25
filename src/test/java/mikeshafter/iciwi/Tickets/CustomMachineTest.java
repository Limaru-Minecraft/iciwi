package mikeshafter.iciwi.Tickets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class CustomMachineTest {
  @Test
  void testRelevance() {
    
    // Search contains match
    assertEquals(1f, relevance("Shiki", "Shiki"));
    assertEquals(0.5, relevance("Kim", "kimono"));
    assertEquals(0f, relevance("z", "hakase"));
    
    // Search contains only part of match
    assertEquals(1f/11, relevance("East", "Westminster"));
  }
  
  public float relevance(String search, String match) {
    // Ignore case
    search = search.toLowerCase();
    match = match.toLowerCase();
    
    // Optimisation
    if (match.equals(search)) return 1f;
    
    /*
    Search = the search term
    Match = a string containing the search term
    match.length() >= search.length()
    Relevance = percentage of letters equal to the sequence in searchResult.
    */
    
    // Required variables
    int searchLength = search.length();
    int matchLength = match.length();
    
    // If the match contains the search term, it is relevant, thus we give a positive score
    if (match.contains(search)) return ((float) searchLength)/matchLength;
    
    // If the match does not contain the search term, but contains parts of it, we give a divided score
    // The score is calculated by s_x/x*m where s is the search term length, x is the number of characters in the search term not matched,
    //   and m is the match term length.
    
    /* At this point match does not contain search */
    for (int i = searchLength; i >= 2; i--) { // i is length of substring
      for (int j = 0; j+i <= searchLength; j++) {
        String subSearch = search.substring(j, j+i);
        if (match.contains(subSearch)) {
          // found match, calculate relevance
          return ((float) i)/(searchLength-i)/matchLength;
        }
      }
    }
    
    // if no match found, return 0f (search failed)
    return 0f;
  }
}