package de.webis.zero_shot.query_expansion;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

public class SBertQueryExpansionConfiguration {
	public final int fbTerms, fbDocs;
	public final float originalQueryWeight;
	public final String sBertThreshold;
	private final String feedbackIndex;
	
	private final Map<Integer, List<String>> queryIdToDocuments;
	
	public SBertQueryExpansionConfiguration(String fbTerms, String fbDocs, String originalQueryWeight, String sBertThreshold, Map<Integer, List<String>> queryIdToDocuments, String feedbackIndex) {
		this.fbTerms = Integer.parseInt(fbTerms);
		this.fbDocs = Integer.parseInt(fbDocs);
		this.sBertThreshold = sBertThreshold;
		
		this.queryIdToDocuments = queryIdToDocuments;
		this.originalQueryWeight = Float.parseFloat(originalQueryWeight);
		this.feedbackIndex = feedbackIndex;
	}

	public String tag() {
		return "rm3(fbTerms=" + fbTerms + ",fbDocs=" + fbDocs + ",originalQueryWeight=" + originalQueryWeight + ",sBertThreshold=" + sBertThreshold + ")";
	}

	public List<String> feedbackForQuery(Integer queryId) {
		if(!queryIdToDocuments.containsKey(queryId) || queryIdToDocuments.get(queryId) == null) {
			return new ArrayList<>();
		}

		return new ArrayList<>(queryIdToDocuments.get(queryId));
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
