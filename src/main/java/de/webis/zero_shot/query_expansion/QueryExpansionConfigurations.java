package de.webis.zero_shot.query_expansion;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

public class QueryExpansionConfigurations {
	public final Map<Integer, List<String>> queryIdToDocuments;

	public QueryExpansionConfigurations(String path) {
		queryIdToDocuments = parseQueryIdToExpansionDocuments(Paths.get(path));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<Integer, List<String>> parseQueryIdToExpansionDocuments(Path path) {
		try {
			Map<Integer, List<String>> ret = new LinkedHashMap<>();

			for (String line : Files.readAllLines(path)) {
				Map<String, Object> parsed = new ObjectMapper().readValue(line, Map.class);

				Integer topic = (Integer) parsed.get("sourceQueryId");
				List<String> targetDocuments = (List<String>) ((Map) parsed.get("target")).get("targetDocuments");

				if (!ret.containsKey(topic)) {
					ret.put(topic, new ArrayList<>());
				}

				ret.get(topic).addAll(targetDocuments);
			}

			return unmodifiable(ret);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static Map<Integer, List<String>> unmodifiable(Map<Integer, List<String>> a) {
		Map<Integer, List<String>> ret = new LinkedHashMap<>();

		for (Integer k : a.keySet()) {
			ret.put(k, Collections.unmodifiableList(a.get(k)));
		}

		return Collections.unmodifiableMap(ret);
	}

	@SneakyThrows
	public void persistQueryIdToExpansionDocuments() {
		new ObjectMapper().writeValue(new File("query-id-to-expansion-documents.json"), queryIdToDocuments);
	}
	
	public List<QueryExpansionConfiguration> allConfigurations(SearchArgs args) {
		if (args.rm3 == false) {
			throw new RuntimeException("invalid input");
		}

		List<QueryExpansionConfiguration> ret = new ArrayList<>();

		if (args.rm3) {
			for (String fbTerms : args.rm3_fbTerms) {
				for (String fbDocs : args.rm3_fbDocs) {
					for (String originalQueryWeight : args.rm3_originalQueryWeight) {
						for (int explicitFeedbackDocNumber = 0; explicitFeedbackDocNumber < maxFeedbackDocs(); explicitFeedbackDocNumber++) {
							ret.add(new QueryExpansionConfiguration(fbTerms, fbDocs, originalQueryWeight, explicitFeedbackDocNumber, queryIdToDocuments, args.feedbackIndex));
						}
					}
				}
			}
		}

		return ret;
	}

	private int maxFeedbackDocs() {
		return queryIdToDocuments.values().stream()
			.mapToInt(i -> i.size())
			.max().getAsInt();
	}
}
