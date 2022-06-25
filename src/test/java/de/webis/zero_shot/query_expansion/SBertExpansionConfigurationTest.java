package de.webis.zero_shot.query_expansion;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

public class SBertExpansionConfigurationTest {
	
	@Test
	public void approveParsingOfQueryIdToContentForSBert() {
		SearchArgs args = new SearchArgs();
		args.sBertFiles = new String[] {"src/test/resources/exampl-sbert-similarity-file.jsonl"};
		SBertQueryExpansionConfigurations config = new SBertQueryExpansionConfigurations(args);
		List<String> expected = Arrays.asList("D2837914", "D2837914","D2519486", "D2837914", "D2837914", "D2837914", "D3424516", "D2837914"); 
		
		Assert.assertEquals(new HashSet<>(Arrays.asList(301)), config.parseQueryIdToExpansionDocuments(0.8f).keySet());
		Assert.assertEquals(expected, config.parseQueryIdToExpansionDocuments(0.8f).get(301));
	}
	
	@Test
	public void approveParsingOfQueryIdToContentForSBertTooHighThreshold() {
		SearchArgs args = new SearchArgs();
		args.sBertFiles = new String[] {"src/test/resources/exampl-sbert-similarity-file.jsonl"};
		SBertQueryExpansionConfigurations config = new SBertQueryExpansionConfigurations(args);

		Assert.assertEquals(new HashSet<>(), config.parseQueryIdToExpansionDocuments(0.99f).keySet());
	}

	@Test
	public void approveParsingOfQueryIdToContentForSBertHighThreshold() {
		SearchArgs args = new SearchArgs();
		args.sBertFiles = new String[] {"src/test/resources/exampl-sbert-similarity-file.jsonl"};
		
		SBertQueryExpansionConfigurations config = new SBertQueryExpansionConfigurations(args);
		List<String> expected = Arrays.asList("D2519486"); 
		
		Assert.assertEquals(new HashSet<>(Arrays.asList(301)), config.parseQueryIdToExpansionDocuments(0.85f).keySet());
		Assert.assertEquals(expected, config.parseQueryIdToExpansionDocuments(0.85f).get(301));
	}
	
	@Test
	public void approveParsingOfQueryIdToContentForSBertHighThresholdMultipleFiles() {
		SearchArgs args = new SearchArgs();
		args.sBertFiles = new String[] {"src/test/resources/exampl-sbert-similarity-file.jsonl", "src/test/resources/exampl-sbert-similarity-file.jsonl"};
		SBertQueryExpansionConfigurations config = new SBertQueryExpansionConfigurations(args);
		List<String> expected = Arrays.asList("D2519486", "D2519486"); 
		
		Assert.assertEquals(new HashSet<>(Arrays.asList(301)), config.parseQueryIdToExpansionDocuments(0.85f).keySet());
		Assert.assertEquals(expected, config.parseQueryIdToExpansionDocuments(0.85f).get(301));
	}
	
	@Test
	public void approveGeneratedConfigurationsForMsMarco() {
		SearchArgs args = new SearchArgs();
		args.rm3 = true;
		args.sBertFiles = new String[] {"src/test/resources/exampl-sbert-similarity-file.jsonl"};
		
		SBertQueryExpansionConfigurations config = new SBertQueryExpansionConfigurations(args);
		String expected = "[rm3(fbTerms=10,fbDocs=10,originalQueryWeight=0.5,sBertThreshold=0.8)]";
		String actual = config.allConfigurations(args).stream().map(i -> i.tag()).collect(Collectors.toList()).toString();
		
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void approveGeneratedConfigurationsForMsMarcoWithMultipleExamples() {
		SearchArgs args = new SearchArgs();
		args.rm3 = true;
		args.sBertThreshold = new String[] {"0.7", "0.9"};
		args.sBertFiles = new String[] {"src/test/resources/exampl-sbert-similarity-file.jsonl"};
		
		SBertQueryExpansionConfigurations config = new SBertQueryExpansionConfigurations(args);
		String expected = "[rm3(fbTerms=10,fbDocs=10,originalQueryWeight=0.5,sBertThreshold=0.7), rm3(fbTerms=10,fbDocs=10,originalQueryWeight=0.5,sBertThreshold=0.9)]";
		String actual = config.allConfigurations(args).stream().map(i -> i.tag()).collect(Collectors.toList()).toString();
		
		Assert.assertEquals(expected, actual);
	}
}
