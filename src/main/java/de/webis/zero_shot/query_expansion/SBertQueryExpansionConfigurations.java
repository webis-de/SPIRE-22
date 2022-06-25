package de.webis.zero_shot.query_expansion;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SBertQueryExpansionConfigurations {

	private final SearchArgs args;
	
	public SBertQueryExpansionConfigurations(SearchArgs args) {
		this.args = args;
	}

	@SuppressWarnings({ "unchecked" })
	public Map<Integer, List<String>> parseQueryIdToExpansionDocuments(float similarityThreshold) {
		try {
			Map<Integer, List<String>> ret = new LinkedHashMap<>();
			for(String path: args.sBertFiles) {
				
				for (String line : Files.readAllLines(Paths.get(path))) {
					Map<String, Object> parsed = new ObjectMapper().readValue(line, Map.class);
	
					Integer topic = Integer.parseInt((String) parsed.get("originalId"));
					List<String> targetDocuments = (List<String>) parsed.get("target_document");
					if(((Double) parsed.get("cosine_similarity")) < similarityThreshold) {
						continue;
					}
					
					if (!ret.containsKey(topic)) {
						ret.put(topic, new ArrayList<>());
					}
	
					ret.get(topic).addAll(targetDocuments);
				}
			}

			return QueryExpansionConfigurations.unmodifiable(ret);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<SBertQueryExpansionConfiguration> allConfigurations(SearchArgs args) {
		if (args.rm3 == false) {
			throw new RuntimeException("invalid input");
		}

		List<SBertQueryExpansionConfiguration> ret = new ArrayList<>();

		if (args.rm3) {
			for (String sbertThreshold: args.sBertThreshold) {
				Map<Integer, List<String>> queryIdToDocuments = parseQueryIdToExpansionDocuments(Float.parseFloat(sbertThreshold));
				
				for (String fbTerms : args.rm3_fbTerms) {
					for (String fbDocs : args.rm3_fbDocs) {
						for (String originalQueryWeight : args.rm3_originalQueryWeight) {
							ret.add(new SBertQueryExpansionConfiguration(fbTerms, fbDocs, originalQueryWeight, sbertThreshold, queryIdToDocuments, args.feedbackIndex));
						}
					}
				}
			}
		}

		return ret;
	}
}
