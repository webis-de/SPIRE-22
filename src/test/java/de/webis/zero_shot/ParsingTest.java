package de.webis.zero_shot;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import de.webis.zero_shot.query_similarity.QuerySimilarityApp;

public class ParsingTest {
	@Test
	public void approveParsingOfQueryIdToContentForMsMarco() {
		Path path = Paths.get("src/test/resources/msmarco-document-train-sample.jsonl"); 
		Map<String, Pair<String, List<String>>> parsed = QuerySimilarityApp.parseQueries(path);
		
		Assert.assertEquals(Pair.of("what is a flail chest", Arrays.asList("D576861")), parsed.get("683408"));
	}
	
	@Test
	public void approveParsingOfQueryIdToContentForRobust04() {
		Path path = Paths.get("src/test/resources/robust04-sample.jsonl"); 
		Map<String, Pair<String, List<String>>> parsed = QuerySimilarityApp.parseQueries(path);

		Assert.assertEquals("Argentina pegging dollar", parsed.get("686").getKey());		
		Assert.assertNull(parsed.get("686").getValue());
	}
}
