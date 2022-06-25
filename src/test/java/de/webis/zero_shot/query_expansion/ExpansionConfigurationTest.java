package de.webis.zero_shot.query_expansion;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

public class ExpansionConfigurationTest {
	@Test
	public void approveParsingOfQueryIdToContentForMsMarco() {
		QueryExpansionConfigurations config = new QueryExpansionConfigurations("src/test/resources/example-similarity-file.jsonl");
		List<String> expected = Arrays.asList("D2723033", "D1858617", "D1509648", "D3071763", "D197946"); 
		
		Assert.assertEquals(new HashSet<>(Arrays.asList(350)), config.queryIdToDocuments.keySet());
		Assert.assertEquals(expected, config.queryIdToDocuments.get(350));
	}
	
	@Test
	public void approveGeneratedConfigurationsForMsMarco() {
		SearchArgs args = new SearchArgs();
		args.rm3 = true;
		args.rm3_fbDocs = new String[] {"-1"};
		
		QueryExpansionConfigurations config = new QueryExpansionConfigurations("src/test/resources/example-similarity-file.jsonl");
		String expected = "[rm3(fbTerms=10,explicitFeedbackDocNumber=0,originalQueryWeight=0.5), rm3(fbTerms=10,explicitFeedbackDocNumber=1,originalQueryWeight=0.5), rm3(fbTerms=10,explicitFeedbackDocNumber=2,originalQueryWeight=0.5), rm3(fbTerms=10,explicitFeedbackDocNumber=3,originalQueryWeight=0.5), rm3(fbTerms=10,explicitFeedbackDocNumber=4,originalQueryWeight=0.5)]";
		String actual = config.allConfigurations(args).stream().map(i -> i.tag()).collect(Collectors.toList()).toString();
		
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void approveGeneratedConfigurationsForMsMarco2() {
		SearchArgs args = new SearchArgs();
		args.rm3 = true;
		args.rm3_fbTerms = new String[] {"9", "10"};
		args.rm3_fbDocs = new String[] {"-1"};
		
		QueryExpansionConfigurations config = new QueryExpansionConfigurations("src/test/resources/example-similarity-file.jsonl");
		String expected = "[rm3(fbTerms=9,explicitFeedbackDocNumber=0,originalQueryWeight=0.5), rm3(fbTerms=9,explicitFeedbackDocNumber=1,originalQueryWeight=0.5), rm3(fbTerms=9,explicitFeedbackDocNumber=2,originalQueryWeight=0.5), rm3(fbTerms=9,explicitFeedbackDocNumber=3,originalQueryWeight=0.5), rm3(fbTerms=9,explicitFeedbackDocNumber=4,originalQueryWeight=0.5), rm3(fbTerms=10,explicitFeedbackDocNumber=0,originalQueryWeight=0.5), rm3(fbTerms=10,explicitFeedbackDocNumber=1,originalQueryWeight=0.5), rm3(fbTerms=10,explicitFeedbackDocNumber=2,originalQueryWeight=0.5), rm3(fbTerms=10,explicitFeedbackDocNumber=3,originalQueryWeight=0.5), rm3(fbTerms=10,explicitFeedbackDocNumber=4,originalQueryWeight=0.5)]";
		String actual = config.allConfigurations(args).stream().map(i -> i.tag()).collect(Collectors.toList()).toString();
		
		Assert.assertEquals(expected, actual);
	}
}
