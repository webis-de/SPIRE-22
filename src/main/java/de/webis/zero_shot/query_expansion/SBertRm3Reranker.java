package de.webis.zero_shot.query_expansion;

import io.anserini.rerank.Reranker;
import io.anserini.rerank.RerankerContext;
import io.anserini.rerank.ScoredDocuments;
import io.anserini.search.query.BagOfWordsQueryGenerator;
import io.anserini.analysis.AnalyzerUtils;
import io.anserini.index.IndexArgs;
import io.anserini.util.FeatureVector;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.anserini.index.IndexArgs.CONTENTS;
import static io.anserini.search.SearchCollection.BREAK_SCORE_TIES_BY_DOCID;
import static io.anserini.search.SearchCollection.BREAK_SCORE_TIES_BY_TWEETID;

public class SBertRm3Reranker<T> implements Reranker<T> {
	private static final Logger LOG = LogManager.getLogger(ExplicitRm3Reranker.class);

	private final Analyzer analyzer;
	private final String field;
	private final SBertQueryExpansionConfiguration config;

	public SBertRm3Reranker(Analyzer analyzer, SBertQueryExpansionConfiguration config) {
		this.analyzer = analyzer;
		this.field = IndexArgs.CONTENTS;
		this.config = config;
	}

	public Query feedbackQuery(RerankerContext<T> context) {
    try {
      Similarity similarity = context.getIndexSearcher().getSimilarity();
      Query query = similarityInBody(context.getQueryText());
      List<Pair<Terms, Double>> relevanceFeedback = new ArrayList<>();
      IndexSearcher searcher = config.feedbackSearcher();
      searcher.setSimilarity(similarity);
      
      for(String docId: config.feedbackForQuery((Integer) context.getQueryId())) {
    	  ScoreDoc doc = calculate(searcher, query, docId);
    	  if(doc == null) {
    		  continue;
    	  }
    	  
    	  Terms terms = searcher.getIndexReader().getTermVector(doc.doc, field);
    	  relevanceFeedback.add(Pair.of(terms, (double) doc.score));
      }

      return feedbackQuery(sortedRelevanceFeedback(relevanceFeedback, config.fbDocs), context);	
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static List<Pair<Terms, Double>> sortedRelevanceFeedback(List<Pair<Terms, Double>> relevanceFeedback, int feedbackDocs) {
	  List<Pair<Terms, Double>> ret = new ArrayList<>(relevanceFeedback);
	  Collections.sort(ret, (a,b) -> b.getRight().compareTo(a.getRight()));
	  
	  return ret.stream().limit(feedbackDocs).collect(Collectors.toList());
  }

	public static ScoreDoc calculate(IndexSearcher searcher, Query query, String documentId) {
		BooleanQuery scoreForDoc = new BooleanQuery.Builder().add(idIs(documentId), BooleanClause.Occur.FILTER)
				.add(query, BooleanClause.Occur.MUST).build();

		return retrieveScoreOrFail(searcher, scoreForDoc, documentId);
	}

	private static ScoreDoc retrieveScoreOrFail(IndexSearcher searcher, Query query, String docId) {
		try {
			return retrieveScore(searcher, query, docId);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static ScoreDoc retrieveScore(IndexSearcher searcher, Query query, String docId) throws IOException {
		TopDocs ret = searcher.search(query, 10);

		if (ret.scoreDocs.length == 0) {
			return null;
		} else if (ret.scoreDocs.length == 1) {
			return ret.scoreDocs[0];
		}

		Document firstDoc = searcher.getIndexReader().document(ret.scoreDocs[0].doc);
		String actualId = firstDoc.get(IndexArgs.ID);

		if (docId == null || !docId.equals(actualId)) {
			throw new RuntimeException("I expected a document with id '" + docId + "', but got '" + actualId + "'.");
		}

		return ret.scoreDocs[0];
	}

	private static Query idIs(String documentId) {
		return new TermQuery(new Term(IndexArgs.ID, documentId));
	}

	private Query similarityInBody(String queryString) {
		return new BagOfWordsQueryGenerator().buildQuery(IndexArgs.CONTENTS, analyzer, queryString);
	}

	public Query feedbackQuery(List<Pair<Terms, Double>> relevanceFeedback, RerankerContext<T> context) {
		IndexSearcher searcher = context.getIndexSearcher();
		IndexReader reader = searcher.getIndexReader();

		FeatureVector qfv = FeatureVector.fromTerms(AnalyzerUtils.analyze(analyzer, context.getQueryText()))
				.scaleToUnitL1Norm();

		FeatureVector rm = estimateRelevanceModel(relevanceFeedback, reader, false);

		rm = FeatureVector.interpolate(qfv, rm, config.originalQueryWeight);

		BooleanQuery.Builder feedbackQueryBuilder = new BooleanQuery.Builder();

		Iterator<String> terms = rm.iterator();
		while (terms.hasNext()) {
			String term = terms.next();
			float prob = rm.getFeatureWeight(term);
			feedbackQueryBuilder.add(new BoostQuery(new TermQuery(new Term(this.field, term)), prob),
					BooleanClause.Occur.SHOULD);
		}

		return feedbackQueryBuilder.build();
	}

	@Override
	public ScoredDocuments rerank(ScoredDocuments docs, RerankerContext<T> context) {
		assert (docs.documents.length == docs.scores.length);
		IndexSearcher searcher = context.getIndexSearcher();
		Query feedbackQuery = feedbackQuery(context);

		LOG.info("QID: " + context.getQueryId() + "; Original Query: " + context.getQuery().toString(this.field)
				+ "; New query: " + feedbackQuery.toString(this.field) + "; tag: " + tag());

		TopDocs rs;
		try {
			Query finalQuery = feedbackQuery;
			// If there's a filter condition, we need to add in the constraint.
			// Otherwise, just use the feedback query.
			if (context.getFilter() != null) {
				BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
				bqBuilder.add(context.getFilter(), BooleanClause.Occur.FILTER);
				bqBuilder.add(feedbackQuery, BooleanClause.Occur.MUST);
				finalQuery = bqBuilder.build();
			}

			// Figure out how to break the scoring ties.
			if (context.getSearchArgs().arbitraryScoreTieBreak) {
				rs = searcher.search(finalQuery, context.getSearchArgs().hits);
			} else if (context.getSearchArgs().searchtweets) {
				rs = searcher.search(finalQuery, context.getSearchArgs().hits, BREAK_SCORE_TIES_BY_TWEETID, true);
			} else {
				rs = searcher.search(finalQuery, context.getSearchArgs().hits, BREAK_SCORE_TIES_BY_DOCID, true);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return ScoredDocuments.fromTopDocs(rs, searcher);
	}

	private FeatureVector estimateRelevanceModel(List<Pair<Terms, Double>> relevanceFeedback, IndexReader reader,
			boolean tweetsearch) {
		FeatureVector f = new FeatureVector();

		Set<String> vocab = new HashSet<>();
		FeatureVector[] docvectors = new FeatureVector[relevanceFeedback.size()];

		for (int i = 0; i < relevanceFeedback.size(); i++) {
			try {
				FeatureVector docVector = createdFeatureVector(relevanceFeedback.get(i).getLeft(), reader, tweetsearch);
				docVector.pruneToSize(config.fbTerms);

				vocab.addAll(docVector.getFeatures());
				docvectors[i] = docVector;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		// Precompute the norms once and cache results.
		float[] norms = new float[docvectors.length];
		for (int i = 0; i < docvectors.length; i++) {
			norms[i] = (float) docvectors[i].computeL1Norm();
		}

		for (String term : vocab) {
			float fbWeight = 0.0f;
			for (int i = 0; i < docvectors.length; i++) {
				// Avoids zero-length feedback documents, which causes division by zero when
				// computing term weights.
				// Zero-length feedback documents occur (e.g., with CAR17) when a document has
				// only terms
				// that accents (which are indexed, but not selected for feedback).
				if (norms[i] > 0.001f) {
					fbWeight += (docvectors[i].getFeatureWeight(term) / norms[i]) * relevanceFeedback.get(i).getRight();
				}
			}
			f.addFeatureWeight(term, fbWeight);
		}

		f.pruneToSize(config.fbTerms);
		f.scaleToUnitL1Norm();

		return f;
	}

	private FeatureVector createdFeatureVector(Terms terms, IndexReader reader, boolean tweetsearch) {
		FeatureVector f = new FeatureVector();

		try {
			int numDocs = reader.numDocs();
			TermsEnum termsEnum = terms.iterator();

			BytesRef text;
			while ((text = termsEnum.next()) != null) {
				String term = text.utf8ToString();

				if (term.length() < 2 || term.length() > 20)
					continue;
				if (!term.matches("[a-z0-9]+"))
					continue;

				// This seemingly arbitrary logic needs some explanation. See following PR for
				// details:
				// https://github.com/castorini/Anserini/pull/289
				//
				// We have long known that stopwords have a big impact in RM3. If we include
				// stopwords
				// in feedback, effectiveness is affected negatively. In the previous
				// implementation, we
				// built custom stopwords lists by selecting top k terms from the collection. We
				// only
				// had two stopwords lists, for gov2 and for Twitter. The gov2 list is used on
				// all
				// collections other than Twitter.
				//
				// The logic below instead uses a df threshold: If a term appears in more than n
				// percent
				// of the documents, then it is discarded as a feedback term. This heuristic has
				// the
				// advantage of getting rid of collection-specific stopwords lists, but at the
				// cost of
				// introducing an additional tuning parameter.
				//
				// Cognizant of the dangers of (essentially) tuning on test data, here's what I
				// (@lintool) did:
				//
				// + For newswire collections, I picked a number, 10%, that seemed right. This
				// value
				// actually increased effectiveness in most conditions across all newswire
				// collections.
				//
				// + This 10% value worked fine on web collections; effectiveness didn't change
				// much.
				//
				// Since this was the first and only heuristic value I selected, we're not
				// really tuning
				// parameters.
				//
				// The 10% threshold, however, doesn't work well on tweets because tweets are
				// much
				// shorter. Based on a list terms in the collection by df: For the Tweets2011
				// collection,
				// I found a threshold close to a nice round number that approximated the length
				// of the
				// current stopwords list, by eyeballing the df values. This turned out to be
				// 1%. I did
				// this again for the Tweets2013 collection, using the same approach, and
				// obtained a value
				// of 0.7%.
				//
				// With both values, we obtained effectiveness pretty close to the old values
				// with the
				// custom stopwords list.
				int df = reader.docFreq(new Term(CONTENTS, term));
				if (df <= 0) {
					continue;
				}

				float ratio = (float) df / numDocs;
				if (tweetsearch) {
					if (numDocs > 100000000) { // Probably Tweets2013
						if (ratio > 0.007f)
							continue;
					} else {
						if (ratio > 0.01f)
							continue;
					}
				} else if (ratio > 0.1f)
					continue;

				int freq = (int) termsEnum.totalTermFreq();
				f.addFeatureWeight(term, (float) freq);
			}
		} catch (Exception e) {
			e.printStackTrace();
			// Return empty feature vector
			return f;
		}

		return f;
	}

	@Override
	public String tag() {
		return config.tag();
	}
}
