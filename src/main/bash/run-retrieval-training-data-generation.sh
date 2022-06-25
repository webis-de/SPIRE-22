#!/bin/bash -e

# Run on epsilonweb010

INDEX="/anserini-indexes/ms-marco-content"
OUT_DIR="/mnt/ceph/storage/data-in-progress/data-research/web-search/ECIR-22/ecir22-zero-shot/training-data/"

java -cp target/ecir22-zero-shot-1.0-SNAPSHOT-jar-with-dependencies.jar \
	de.webis.zero_shot.query_expansion.SearchCollection \
	-index $INDEX \
	-threads 40 \
	-bm25 \
	-topicreader TsvInt \
	-topics ${OUT_DIR}/4k-queries-only-paraphrases/topics.tsv \
	-output ${OUT_DIR}/4k-queries-only-paraphrases/bm25-run.txt \

java -cp target/ecir22-zero-shot-1.0-SNAPSHOT-jar-with-dependencies.jar \
	de.webis.zero_shot.query_expansion.SearchCollection \
	-index $INDEX \
	-threads 40 \
	-bm25 \
	-topicreader TsvInt \
	-topics ${OUT_DIR}/10k-random-no-overlap/topics.tsv \
	-output ${OUT_DIR}/10k-random-no-overlap/bm25-run.txt \

java -cp target/ecir22-zero-shot-1.0-SNAPSHOT-jar-with-dependencies.jar \
	de.webis.zero_shot.query_expansion.SearchCollection \
	-index $INDEX \
	-threads 40 \
	-bm25 \
	-topicreader TsvInt \
	-topics ${OUT_DIR}/10k-queries-with-0.9-overlap/topics.tsv \
	-output ${OUT_DIR}/10k-queries-with-0.9-overlap/bm25-run.txt \

java -cp target/ecir22-zero-shot-1.0-SNAPSHOT-jar-with-dependencies.jar \
	de.webis.zero_shot.query_expansion.SearchCollection \
	-index $INDEX \
	-threads 40 \
	-bm25 \
	-topicreader TsvInt \
	-topics ${OUT_DIR}/100k-random-ms-marco-no-overlap/topics.tsv \
	-output ${OUT_DIR}/100k-random-ms-marco-no-overlap/bm25-run.txt \

java -cp target/ecir22-zero-shot-1.0-SNAPSHOT-jar-with-dependencies.jar \
	de.webis.zero_shot.query_expansion.SearchCollection \
	-index $INDEX \
	-threads 40 \
	-bm25 \
	-topicreader TsvInt \
	-topics ${OUT_DIR}/100k-random-orcas-no-overlap/topics.tsv \
	-output ${OUT_DIR}/100k-random-orcas-no-overlap/bm25-run.txt \

