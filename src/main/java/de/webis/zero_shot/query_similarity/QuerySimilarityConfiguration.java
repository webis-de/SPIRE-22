package de.webis.zero_shot.query_similarity;

import java.nio.file.Path;

import lombok.Data;

@Data
public class QuerySimilarityConfiguration {
	public Path dataDirectory;
	public String sourceApproach, targetApproach;
	public String out;
	public int candidatesPerApproach = 10000;
}
