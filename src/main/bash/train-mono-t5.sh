#!/bin/bash -e

IN_FILE="/mnt/ceph/storage/data-in-progress/data-research/web-search/ECIR-22/ecir22-zero-shot/training-data/${TRAINING_DATASET}/training-data.txt"
OUT_FILE="/mnt/ceph/storage/data-in-progress/data-research/web-search/ECIR-22/ecir22-zero-shot/training-data/${TRAINING_DATASET}/trained-t5-models"


if [ ! -f "${IN_FILE}" ]
then
	echo "Does not exist: ${IN_FILE}"
	exit 1
fi


echo "Use as input $(ls -lh ${IN_FILE})"
echo "Write results to: ${OUT_FILE}"


t5_mesh_transformer \
	--gin_file="/mnt/ceph/storage/data-in-progress/data-research/web-search/ECIR-22/ecir22-zero-shot/t5-pretrained-base/pretrained_models_base_operative_config.gin" \
	--gin_param="utils.run.mesh_devices = ['gpu:0']" \
	--gin_param="utils.run.mesh_shape = 'model:1,batch:1'" \
	--gin_param="utils.tpu_mesh_shape.model_parallelism = 1" \
	--gin_param="utils.run.train_dataset_fn = @t5.models.mesh_transformer.tsv_dataset_fn" \
	--gin_param="tsv_dataset_fn.filename = '${IN_FILE}'" \
	--gin_file="learning_rate_schedules/constant_0_001.gin" \
	--gin_param="run.train_steps = 1100000" \
	--gin_param="run.save_checkpoints_steps = 10000" \
	--model_dir="${OUT_FILE}"

