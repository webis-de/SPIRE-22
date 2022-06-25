package de.webis.zero_shot.query_similarity;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.SneakyThrows;

public class QuerySimilarityApp {
	private QuerySimilarityConfiguration config;

	public static void main(String[] args) {
		QuerySimilarityConfiguration config = new QuerySimilarityConfiguration();
		config.dataDirectory = Paths.get("/mnt/ceph/storage/data-in-progress/data-research/web-search/ECIR-22/ecir22-zero-shot");
		config.sourceApproach = "trec-robust04";
		config.targetApproach = args[0];
		config.out = args[1];
		
		QuerySimilarityApp app = new QuerySimilarityApp();
		app.config = config;
		app.run();
	}
	
	
	@SneakyThrows
	private void run() {
		Map<String, Pair<String, List<String>>> sourceQueries = QuerySimilarityApp.parseQueries(config.dataDirectory.resolve(config.sourceApproach + ".jsonl"));
		try(PrintWriter fw= new PrintWriter(new FileWriter(new File(config.out)))) {
			for(Map.Entry<String, Pair<String, List<String>>> sourceQuery: sourceQueries.entrySet()) {
				int queryId = Integer.parseInt(sourceQuery.getKey());
				String query = sourceQuery.getValue().getLeft();
				
				System.out.println("Process '" + query + "' (Id=" + queryId + ")");
				
				for(String i: processQuery(queryId, query)) {
					fw.write(i + "\n");
				}
			}
		}
	}



	public List<String> processQuery(int queryId, String query) {
		return processQuery(queryId, query, CandidateRetrievalUtil.similarityInBody(query));
	}
	
	@SneakyThrows
	public List<String> processQuery(int queryId, String originalQuery, Query query) {
		Map<String, QueryMatch> ret = new LinkedHashMap<>();
		Map<String, Pair<String, List<String>>> queryIdToContent = parseQueries(config.dataDirectory.resolve(config.targetApproach + ".jsonl"));
		
		for(String similarity: CandidateRetrievalUtil.SIMILARITIES.keySet()) {
			IndexSearcher searcher = CandidateRetrievalUtil.searcher(similarity, config.dataDirectory.resolve("indexes").resolve(config.targetApproach));
			
			TopDocs matches = searcher.search(query, config.candidatesPerApproach);
			for(int i=0; i< matches.scoreDocs.length; i++) {
				String id = searcher.doc(matches.scoreDocs[i].doc).get("id");
				if(!ret.containsKey(id)) {
					Pair<String, List<String>> tmp = queryIdToContent.get(id);
					if(tmp == null || tmp.getLeft() == null || tmp.getRight() == null) {
						throw new RuntimeException("");
					}
					
					ret.put(id, new QueryMatch(id, tmp.getLeft(), tmp.getRight()));
				}
				
				ret.get(id).scores.put(similarity, (double) matches.scoreDocs[i].score);
			}
		}
		
		return ret.entrySet().stream()
			.map(i -> formatResult(queryId, originalQuery, i))
			.collect(Collectors.toList());
	}
	
	@SneakyThrows
	private String formatResult(int sourceQueryId, String sourceQuery, Entry<String, QueryMatch> i) {
		if(!i.getKey().equals(i.getValue().queryId)) {
			throw new RuntimeException("");
		}
		
		Map<String, Object> ret = new LinkedHashMap<>();
		ret.put("sourceQueryId", sourceQueryId);
		ret.put("sourceQuery", sourceQuery);
		ret.put("target", i.getValue());
		
		return new ObjectMapper().writeValueAsString(ret);
	}

	@Data
	private class QueryMatch {
		final String queryId;
		final String query;
		final List<String> targetDocuments;
		final Map<String, Double> scores = new LinkedHashMap<>();
		
		QueryMatch(String queryId, String query, List<String> targetDocuments) {
			this.queryId = queryId;
			this.query = query;
			this.targetDocuments = targetDocuments;
		}
	}

	@SneakyThrows
	public static Map<String, Pair<String, List<String>>> parseQueries(Path path) {
		return Files.readAllLines(path, StandardCharsets.UTF_8).stream()
			.collect(Collectors.toMap(i-> parseId(i), i -> parseQueryAndTarget(i)));
	}

	@SneakyThrows
	@SuppressWarnings("unchecked")
	private static String parseId(String src) {
		Map<String, Object> ret = new ObjectMapper().readValue(src, Map.class);
		
		return (String) ret.get("id");
	}
	
	@SneakyThrows
	@SuppressWarnings("unchecked")
	private static Pair<String, List<String>> parseQueryAndTarget(String src) {
		Map<String, Object> ret = new ObjectMapper().readValue(src, Map.class);
		
		return Pair.of((String) ret.get("contents"), (List<String>) ret.get("target_document"));
	}
}
