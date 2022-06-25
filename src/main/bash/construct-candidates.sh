#!/bin/bash -e

java \
	-cp target/ecir22-zero-shot-1.0-SNAPSHOT-jar-with-dependencies.jar \
	de.webis.zero_shot.query_similarity.QuerySimilarityApp \
	${1} \
	/mnt/ceph/storage/data-in-progress/data-research/web-search/ECIR-22/ecir22-zero-shot/similarities/${1}.jsonl

