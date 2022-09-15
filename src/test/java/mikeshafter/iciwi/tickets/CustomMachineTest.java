package mikeshafter.iciwi.tickets;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;


class CustomMachineTest {
  @Test
  void preRelevanceSort() {
    // test that the order of relevance is correct before putting the values into #relevanceSort
  }
  
  @Test
  void relevanceSort() {
    // test #relevanceSort here
  }
  
  @Test
  void relevance() {
    // test #relevance here
    
    // Search contains match
    assertEquals(1f, relevance("Shiki", "Shiki"));
    assertEquals(0.5, relevance("Kim", "kimono"));
    assertEquals(0f, relevance("z", "hakase"));
    
    // Search contains only part of match
    assertEquals(1f/11, relevance("East", "Westminster"));
  }
  
  /**
   * Relevance function
   */
  public float relevance(String pattern, String term) {
    // Ignore case
    pattern = pattern.toLowerCase();
    term = term.toLowerCase();
    
    // Optimisation
    if (term.equals(pattern)) return 1f;

    /*
    Search = the pattern term
    Match = a string containing the pattern term
    term.length() >= pattern.length()
    Relevance = percentage of letters equal to the sequence in searchResult.
    */
    
    // Required variables
    int searchLength = pattern.length();
    int matchLength = term.length();
    
    // If the term contains the pattern term, it is relevant, thus we give a full score
    if (term.contains(pattern)) return ((float) searchLength)/matchLength;
    
    // If the term does not contain the pattern term, but contains parts of it, we give a divided score
    // The score is calculated by s_x/x*m where s is the pattern term length, x is the number of characters in the pattern term not matched,
    //   and m is the term term length.
    
    /* At this point term does not contain pattern */
    for (int i = searchLength; i >= 2; i--) { // i is length of substring
      for (int j = 0; j+i <= searchLength; j++) {
        String subSearch = pattern.substring(j, j+i);
        if (term.contains(subSearch)) {
          // found term, calculate relevance
          return ((float) i)/(searchLength-i)/matchLength;
        }
      }
    }
    
    // if no term found, return 0f (pattern failed)
    return 0f;
  }
  
  // Copied methods
  public String[] relevanceSort(String pattern, String[] values) {
    Arrays.sort(values, (v1, v2) -> Float.compare(relevance(pattern, v1), relevance(pattern, v2)));
    return values;
  }
}