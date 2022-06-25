package de.webis.zero_shot.query_expansion;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

public class QueryExpansionConfiguration {
	public final int fbTerms, explicitFeedbackDocNumber;
	public final float originalQueryWeight;
	private final String feedbackIndex;
	
	private final Map<Integer, List<String>> queryIdToDocuments;
	
	public QueryExpansionConfiguration(String fbTerms, String fbDocs, String originalQueryWeight, int explicitFeedbackDocNumber, Map<Integer, List<String>> queryIdToDocuments, String feedbackIndex) {
		this.fbTerms = Integer.parseInt(fbTerms);
		if(!fbDocs.equals("-1") && !fbDocs.equals("0")) {
			throw new RuntimeException("I do not really handly fb-docs...");
		}
		
		this.queryIdToDocuments = queryIdToDocuments;
		this.originalQueryWeight = Float.parseFloat(originalQueryWeight);
		this.explicitFeedbackDocNumber = explicitFeedbackDocNumber;
		this.feedbackIndex = feedbackIndex;
	}

	public String tag() {
		return "rm3(fbTerms=" + fbTerms + ",explicitFeedbackDocNumber=" + explicitFeedbackDocNumber + ",originalQueryWeight=" + originalQueryWeight + ")";
	}

	public List<String> feedbackForQuery(Integer queryId) {
		List<String> ret = queryIdToDocuments.get(queryId);
		if(ret.size() > explicitFeedbackDocNumber) {
			return Arrays.asList(ret.get(explicitFeedbackDocNumber));
		}
		
		return Collections.emptyList();
	}

	public IndexSearcher feedbackSearcher() {
		try {
			IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(feedbackIndex)));
			
			return new IndexSearcher(reader);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
