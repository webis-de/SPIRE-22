#!/bin/bash -e

java \
	-cp target/ecir22-zero-shot-1.0-SNAPSHOT-jar-with-dependencies.jar \
	io.anserini.index.IndexCollection \
	-collection JsonCollection \
	-threads 1 \
	-index /mnt/ceph/storage/data-in-progress/data-research/web-search/ECIR-22/ecir22-zero-shot/indexes/${1} \
	-input /mnt/ceph/storage/data-in-progress/data-research/web-search/ECIR-22/ecir22-zero-shot/${1}.jsonl

