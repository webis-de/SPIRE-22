package de.webis.zero_shot.query_similarity;

import org.apache.lucene.search.similarities.ClassicSimilarity;

/**
 * TF-Similarity as to identify similar queries.
 * 
 * @author Maik Fröbe
 *
 */
public class TfSimilarity extends ClassicSimilarity {
  @Override
  public float idf(long docFreq, long docCount) {
    return 1.0f;
  }
  
  @Override
  public float lengthNorm(int numTerms) {
    return 1.0f;
  }
  
  @Override
  public float tf(float freq) {
    return freq;
  }
}