package de.webis.zero_shot.query_similarity;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.AfterEffectL;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BasicModelIn;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.DFRSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.NormalizationH2;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import io.anserini.index.IndexArgs;
import io.anserini.search.SearchArgs;
import io.anserini.search.query.BagOfWordsQueryGenerator;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
class CandidateRetrievalUtil {
	static final Map<String, Supplier<Similarity>> SIMILARITIES = Map.of("BM25",
			() -> new BM25Similarity(0.9f, 0.4f), "TF-IDF", () -> new ClassicSimilarity(), "TF",
			() -> new TfSimilarity(), "PL2",
			() -> new DFRSimilarity(new BasicModelIn(), new AfterEffectL(), new NormalizationH2(0.1f)), "QLJM",
			() -> new LMJelinekMercerSimilarity(0.1f), "QL", () -> new LMDirichletSimilarity(1000f));

	static Query similarityInBody(String queryString) {
		return new BagOfWordsQueryGenerator().buildQuery(IndexArgs.CONTENTS, AnalyzerUtil.analyzer(new SearchArgs()),
				queryString);
	}

	@SneakyThrows
	static IndexSearcher searcher(String similarity, Path indexPath) {
		IndexReader reader = DirectoryReader.open(FSDirectory.open(indexPath));
		IndexSearcher ret = new IndexSearcher(reader);
		ret.setSimilarity(SIMILARITIES.get(similarity).get());

		return ret;
	}
}
