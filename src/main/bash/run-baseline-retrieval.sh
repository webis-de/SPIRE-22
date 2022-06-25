#!/bin/bash -e

# Run on epsilonweb010

INDEX="/anserini-indexes/robust04"

java -cp target/ecir22-zero-shot-1.0-SNAPSHOT-jar-with-dependencies.jar \
	de.webis.zero_shot.query_expansion.SearchCollection \
	-index $INDEX \
	-threads 40 \
	-bm25 \
	-topicreader Trec \
	-topics src/main/resources/topics-and-qrels/topics.robust04.txt \
	-output /mnt/ceph/storage/data-in-progress/data-research/web-search/ECIR-22/ecir22-zero-shot/runs/run.robust04.bm25.topics.robust04.txt

