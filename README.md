# SPIRE-22

This repository contains the code and the data for our paper (currently under single-blind review at SPIRE'22) on unintended train--test leakage with neural retrieval models.

We studied the effects of unintended train--test leakage between MS MARCO/ORCAS and Robust04 and two Common Core tracks, identifying that 69% of the Robust04 queries have near-duplicates in MS MARCO / ORCAS (74% of the TREC 2017 Common Core track and 76% of the TREC 2018 Common Core track). We then trained five neural retrieval models on a fixed number of MS MARCO/ORCAS queries that are highly similar to the actual test queries and an increasing number of other queries to study the effects of such leaked instances.

## Data

- Sentence-BERT embeddings for all MS MARCO/ORCAS, Robust04, and Common Core queries and nearest neighbors (all together 10GB) are available at [https://files.webis.de/corpora/corpora-webis/corpus-webis-leaking-queries](https://files.webis.de/corpora/corpora-webis/corpus-webis-leaking-queries)
- Our manual annotated data on query reformulation types and near-duplicate thresholds is located in [src/main/resources/manual-annotations/](src/main/resources/manual-annotations/).

## Setup

- Compile everything (including running unit tests): `mvn clean install`
- First, create the query-datasets with the notebook [src/main/jupyter/construction-of-query-datasets.ipynb](src/main/jupyter/construction-of-query-datasets.ipynb).

- Build Anserini indexes for the query datasets by running:
  ```
  ./src/main/bash/index-query-dataset.sh msmarco-document-train
  ./src/main/bash/index-query-dataset.sh msmarco-document-orcas
  ```
- Construct candidates for leaking queries by running:
  ```
  ./src/main/bash/construct-candidates.sh msmarco-document-train
  ./src/main/bash/construct-candidates.sh msmarco-document-orcas
  ```

## Training of Models

The scripts in [./src/main/jupyter/model-training](./src/main/jupyter/model-training) were used to train all the models in the different scenarious.

## Evaluation

Start the jupyter notebook with:

```
docker run --rm -ti -p 8888:8888 -v "${PWD}":/home/jovyan/work jupyter/datascience-notebook
```

The scripts in [./src/main/jupyter/reports-paper](./src/main/jupyter/reports-paper) contain all the evaluations and experiments reported in the paper.



