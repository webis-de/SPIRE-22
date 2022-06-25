#!/bin/bash -e

# Run on epsilonweb010

FEEDBACK_DOCS="/mnt/ceph/storage/data-in-progress/data-research/web-search/ECIR-22/ecir22-zero-shot/similarities/msmarco-document-train.jsonl"
FEEDBACK_INDEX="/anserini-indexes/ms-marco-content"
INDEX="/anserini-indexes/robust04"

java -cp target/ecir22-zero-shot-1.0-SNAPSHOT-jar-with-dependencies.jar \
	de.webis.zero_shot.query_expansion.SearchCollection \
	-index $INDEX \
	-threads 40 \
	-bm25 \
	-topicreader Trec \
	-topics src/main/resources/topics-and-qrels/topics.robust04-small.txt \
	-rm3 -rm3.fbTerms 5 6 7 8 9 10 -rm3.fbDocs 8 9 10 11 12 -rm3.originalQueryWeight 0.3 0.4 0.5 0.6 0.7 0.8 \
	-output runs/run.robust04.bm25+rm3.topics.robust04.txt |tee logs/robust04.bm25+rm3.logs
	

java -cp target/ecir22-zero-shot-1.0-SNAPSHOT-jar-with-dependencies.jar \
	de.webis.zero_shot.query_expansion.SearchCollection \
	-index $INDEX \
	-threads 40 \
	-bm25 \
	-topicreader Trec \
	-topics src/main/resources/topics-and-qrels/topics.robust04-small.txt \
	-feedbackIndex $FEEDBACK_INDEX \
	-feedbackDocuments $FEEDBACK_DOCS \
	-rm3 -rm3.fbTerms 5 6 7 8 9 10 -rm3.fbDocs 0 -rm3.originalQueryWeight 0.3 0.4 0.5 0.6 0.7 0.8 \
	-output runs/run.robust04.bm25+explicit-rm3.topics.robust04.txt  |tee logs/robust04.bm25+explicit-rm3.logs
	
