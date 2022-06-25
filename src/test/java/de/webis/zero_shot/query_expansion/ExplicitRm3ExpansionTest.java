package de.webis.zero_shot.query_expansion;

import io.anserini.index.IndexArgs;
import io.anserini.rerank.RerankerContext;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.LuceneTestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ExplicitRm3ExpansionTest<T> extends LuceneTestCase {
	protected Path tempDir1, tempDir2;

	// A very simple example of how to build an index.
	private void buildTestIndex() throws IOException {
		Directory dir = FSDirectory.open(tempDir1);

		Analyzer analyzer = new EnglishAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

		IndexWriter writer = new IndexWriter(dir, config);

		FieldType textOptions = new FieldType();
		textOptions.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
		textOptions.setStored(true);
		textOptions.setTokenized(true);
		textOptions.setStoreTermVectors(true);
		textOptions.setStoreTermVectorPositions(true);

		Document doc1 = new Document();
		String doc1Text = "here is some text here is some more text. city.";
		doc1.add(new StringField(IndexArgs.ID, "doc1", Field.Store.YES));
		doc1.add(new SortedDocValuesField(IndexArgs.ID, new BytesRef("doc1".getBytes())));
		doc1.add(new Field(IndexArgs.CONTENTS, doc1Text, textOptions));
		// specifically demonstrate how "contents" and "raw" might diverge:
		doc1.add(new StoredField(IndexArgs.RAW, String.format("{\"contents\": \"%s\"}", doc1Text)));
		writer.addDocument(doc1);

		Document doc2 = new Document();
		String doc2Text = "more texts";
		doc2.add(new StringField(IndexArgs.ID, "doc2", Field.Store.YES));
		doc2.add(new SortedDocValuesField(IndexArgs.ID, new BytesRef("doc2".getBytes())));
		doc2.add(new Field(IndexArgs.CONTENTS, doc2Text, textOptions)); // Note plural, to test stemming
		// specifically demonstrate how "contents" and "raw" might diverge:
		doc2.add(new StoredField(IndexArgs.RAW, String.format("{\"contents\": \"%s\"}", doc2Text)));
		writer.addDocument(doc2);

		Document doc3 = new Document();
		String doc3Text = "here is a test";
		doc3.add(new StringField(IndexArgs.ID, "doc3", Field.Store.YES));
		doc3.add(new SortedDocValuesField(IndexArgs.ID, new BytesRef("doc3".getBytes())));
		doc3.add(new Field(IndexArgs.CONTENTS, doc3Text, textOptions));
		// specifically demonstrate how "contents" and "raw" might diverge:
		doc3.add(new StoredField(IndexArgs.RAW, String.format("{\"contents\": \"%s\"}", doc3Text)));
		writer.addDocument(doc3);

		//prevent stopwords
		for(int i=0; i<10; i++) {
			Document doc4 = new Document();
			String doc4Text = "dummy documents ftw";
			doc4.add(new StringField(IndexArgs.ID, "doc" + i, Field.Store.YES));
			doc4.add(new SortedDocValuesField(IndexArgs.ID, new BytesRef(("doc" + i).getBytes())));
			doc4.add(new Field(IndexArgs.CONTENTS, doc4Text, textOptions));
			// specifically demonstrate how "contents" and "raw" might diverge:
			doc4.add(new StoredField(IndexArgs.RAW, String.format("{\"contents\": \"%s\"}", doc4Text)));
			writer.addDocument(doc4);
		}
		
		writer.commit();
		writer.forceMerge(1);
		writer.close();

		dir.close();
	}

	private void buildTestIndex2() throws IOException {
		Directory dir = FSDirectory.open(tempDir2);

		Analyzer analyzer = new EnglishAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

		IndexWriter writer = new IndexWriter(dir, config);

		FieldType textOptions = new FieldType();
		textOptions.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
		textOptions.setStored(true);
		textOptions.setTokenized(true);
		textOptions.setStoreTermVectors(true);
		textOptions.setStoreTermVectorPositions(true);

		Document doc1 = new Document();
		String doc1Text = "here is some text here is some more text. city.";
		doc1.add(new StringField(IndexArgs.ID, "doc-2-id-1", Field.Store.YES));
		doc1.add(new SortedDocValuesField(IndexArgs.ID, new BytesRef("doc1".getBytes())));
		doc1.add(new Field(IndexArgs.CONTENTS, doc1Text, textOptions));
		// specifically demonstrate how "contents" and "raw" might diverge:
		doc1.add(new StoredField(IndexArgs.RAW, String.format("{\"contents\": \"%s\"}", doc1Text)));
		writer.addDocument(doc1);

		Document doc2 = new Document();
		String doc2Text = "more texts";
		doc2.add(new StringField(IndexArgs.ID, "doc-2-id-2", Field.Store.YES));
		doc2.add(new SortedDocValuesField(IndexArgs.ID, new BytesRef("doc2".getBytes())));
		doc2.add(new Field(IndexArgs.CONTENTS, doc2Text, textOptions)); // Note plural, to test stemming
		// specifically demonstrate how "contents" and "raw" might diverge:
		doc2.add(new StoredField(IndexArgs.RAW, String.format("{\"contents\": \"%s\"}", doc2Text)));
		writer.addDocument(doc2);

		Document doc3 = new Document();
		String doc3Text = "here is a test";
		doc3.add(new StringField(IndexArgs.ID, "doc-2-id-3", Field.Store.YES));
		doc3.add(new SortedDocValuesField(IndexArgs.ID, new BytesRef("doc3".getBytes())));
		doc3.add(new Field(IndexArgs.CONTENTS, doc3Text, textOptions));
		// specifically demonstrate how "contents" and "raw" might diverge:
		doc3.add(new StoredField(IndexArgs.RAW, String.format("{\"contents\": \"%s\"}", doc3Text)));
		writer.addDocument(doc3);

		Document doc4 = new Document();
		String doc4Text = "only out of vocabulary";
		doc4.add(new StringField(IndexArgs.ID, "doc-2-id-4", Field.Store.YES));
		doc4.add(new SortedDocValuesField(IndexArgs.ID, new BytesRef("doc4".getBytes())));
		doc4.add(new Field(IndexArgs.CONTENTS, doc4Text, textOptions));
		// specifically demonstrate how "contents" and "raw" might diverge:
		doc4.add(new StoredField(IndexArgs.RAW, String.format("{\"contents\": \"%s\"}", doc4Text)));
		writer.addDocument(doc4);
		
		writer.commit();
		writer.forceMerge(1);
		writer.close();

		dir.close();
	}

	@Test
	public void testEmptyRelevanceFeedbackReturnsOriginalQuery() {
		String expected = "(contents:text)^0.5";
		
		Map<Integer, List<String>> queryIdToDocuments = Map.of(2, Arrays.asList("doc-2-id-3"));
		QueryExpansionConfiguration config = new QueryExpansionConfiguration("10", "-1", "0.5", 0, queryIdToDocuments, tempDir2.toString());
		String actual = actualRM3Expansion(2, "text", config);
		
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void testRelevanceFeedbackWithSingleExpansionTermOriginalQuery() {
		String expected = "(contents:test)^0.5 (contents:here)^0.5";
		
		Map<Integer, List<String>> queryIdToDocuments = Map.of(2, Arrays.asList("doc-2-id-3"));
		QueryExpansionConfiguration config = new QueryExpansionConfiguration("10", "-1", "0.5", 0, queryIdToDocuments, tempDir2.toString());
		String actual = actualRM3Expansion(2, "here", config);
		
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void testRelevanceFeedbackWithSingleExpansionTermOriginalQuery2() {
		String expected = "(contents:test)^0.5 (contents:here)^0.5";
		
		Map<Integer, List<String>> queryIdToDocuments = Map.of(2, Arrays.asList("doc-2-id-3", "doc-2-id-1"));
		QueryExpansionConfiguration config = new QueryExpansionConfiguration("10", "-1", "0.5", 0, queryIdToDocuments, tempDir2.toString());
		String actual = actualRM3Expansion(2, "here", config);
		
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void testRelevanceFeedbackWithSingleExpansionTermOriginalQuery3() {
		String expected = "(contents:some)^0.33333334 (contents:citi)^0.16666667 (contents:text)^0.5";
		
		Map<Integer, List<String>> queryIdToDocuments = Map.of(2, Arrays.asList("doc-2-id-3", "doc-2-id-1"));
		QueryExpansionConfiguration config = new QueryExpansionConfiguration("10", "-1", "0.5", 1, queryIdToDocuments, tempDir2.toString());
		String actual = actualRM3Expansion(2, "text", config);
		
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void testRelevanceFeedbackWithOutOfVocabularyTerms() {
		String expected = "(contents:vocabulari)^0.5";
		
		Map<Integer, List<String>> queryIdToDocuments = Map.of(2, Arrays.asList("doc-2-id-3", "doc-2-id-4"));
		QueryExpansionConfiguration config = new QueryExpansionConfiguration("10", "-1", "0.5", 1, queryIdToDocuments, tempDir2.toString());
		String actual = actualRM3Expansion(2, "vocabulary", config);
		
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void testRelevanceFeedbackWithoutMatchReturnsOriginalQuery() {
		String expected = "(contents:text)^0.5";
		
		Map<Integer, List<String>> queryIdToDocuments = Map.of(3, Arrays.asList());
		QueryExpansionConfiguration config = new QueryExpansionConfiguration("10", "-1", "0.5", 10, queryIdToDocuments, tempDir2.toString());
		String actual = actualRM3Expansion(3, "text", config);
		
		Assert.assertEquals(expected, actual);
	}
	
	private String actualRM3Expansion(int queryId, String originalQuery, QueryExpansionConfiguration config) {
		ExplicitRm3Reranker<T> reranker = new ExplicitRm3Reranker<>(new EnglishAnalyzer(), config);
		
		return reranker.feedbackQuery(context(queryId, originalQuery)).toString();
	}

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		tempDir1 = createTempDir();
		tempDir2 = createTempDir();
		buildTestIndex();
		buildTestIndex2();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected RerankerContext<T> context(Integer queryId, String originalQuery) {
		try {
			IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(tempDir1)));
				return new RerankerContext(searcher, queryId, null, null, originalQuery, null, null, null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@After
	@Override
	public void tearDown() throws Exception {
		// Call garbage collector for Windows compatibility
		System.gc();
		super.tearDown();
	}
}
