package de.webis.zero_shot.query_expansion;


import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

public class SearchArgs extends io.anserini.search.SearchArgs {
  @Option(name = "-feedbackIndex", metaVar = "[path]", required = false, usage = "Path to Lucene index for explicit feedback")
  public String feedbackIndex = null;
  
  @Option(name = "-feedbackDocuments", metaVar = "[path]", required = false, usage = "Path to feedback documents file")
  public String feedbackDocuments = null;
  
  @Option(name = "-sBertFiles", metaVar = "[file]", handler = StringArrayOptionHandler.class, required = false, usage = "files with sentence bert similarities for SBertRM3 Expansion")
  public String[] sBertFiles = null;

  @Option(name = "-sBertThreshold", metaVar = "[file]", handler = StringArrayOptionHandler.class, required = false, usage = "Bert similarity thresholds for SBertRM3 Expansion")
  public String[] sBertThreshold = new String[] {"0.8"};

}
