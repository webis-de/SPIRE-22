VERSION := 0.0.2
IMAGE := ecir22-zero-shot
REPO := webis

srun-jupyter-notebook:
	srun
	--container-name=ecir22-zero-shot --container-writable --mem=200G -c 5 \
	--container-mounts=/mnt/ceph/storage/data-tmp/2021/kibi9872/ecir22-zero-shot/src/main/jupyter:/workspace,/mnt/ceph/storage/data-tmp/2021/kibi9872/.ir_datasets:/root/.ir_datasets,/mnt/ceph/storage/data-in-progress/data-research/web-search/ECIR-22/,/mnt/ceph/storage/data-in-progress/data-teaching/theses/wstud-thesis-probst \
	--pty \
	--container-workdir /workspace \
	jupyter notebook --ip 0.0.0.0 --allow-root

# Available notebooks:trec-dl-21-health-misinfo-delete-me,sigir22-zero-shot-leakage-monot5-notebook-01
srun-monot5-jupyter-notebook:
	srun \
	--container-name=trec-dl-21-health-misinfo-delete-me \
	--container-writable --mem=130G -c 5 --gres=gpu:ampere:1 \
	--container-mounts=/mnt/ceph/storage/data-tmp/2021/kibi9872/ecir22-zero-shot/src/main/jupyter:/workspace,/mnt/ceph/storage/data-tmp/2021/kibi9872/.ir_datasets:/root/.ir_datasets,/mnt/ceph/storage/data-in-progress/data-research/web-search/ECIR-22/,/mnt/ceph/storage/data-in-progress/data-teaching/theses/wstud-thesis-probst \
	--pty \
	--container-workdir /workspace \
	jupyter notebook --ip 0.0.0.0 --allow-root

# Available are ecir22-zero-shot-capreolus-framework ecir22-zero-shot-capreolus-framework-02 ecir22-zero-shot-capreolus-framework-03 ecir22-zero-shot-capreolus-framework-04 ecir22-zero-shot-capreolus-framework-05
srun-capreolus-notebook:
	srun -c 25  --mem=100G --gres=gpu:ampere:1 \
		--container-writable \
		--container-image=pytorch/pytorch:1.8.0-cuda11.1-cudnn8-devel \
		--container-name=ecir22-zero-shot-capreolus-framework-05 --pty \
		--chdir ${PWD} \
		bash -c 'cd ~/ecir22-zero-shot/ && jupyter-lab --ip 0.0.0.0 --allow-root'


srun-t5-bash:
	srun \
		--container-image=tensorflow/tensorflow:2.6.0-gpu \
		--container-name=ecir22-zero-shot-t5-tensorflow \
		--gres=gpu:ampere:1 --mem=50G -c 2 \
		--container-mounts=/mnt/ceph/storage/data-tmp/2021/kibi9872/ecir22-zero-shot/src/main/jupyter:/workspace,/mnt/ceph/storage/data-tmp/2021/kibi9872/.ir_datasets:/root/.ir_datasets,/mnt/ceph/storage/data-in-progress/data-research/web-search/ECIR-22/,/mnt/ceph/storage/data-in-progress/data-teaching/theses/wstud-thesis-probst \
		--pty \
		--container-workdir /workspace \
		bash

build: clean
	docker build -f dockerfiles/Dockerfile.tf.gpu -t ${REPO}/${IMAGE}:${VERSION} -t ${REPO}/${IMAGE}:latest .

run: build
	docker run --rm -it -p 8888:8888 -p 6006:6006 --gpus all --mount type=bind,source=${PWD}/src/main/jupyter,target=/tf --mount type=bind,source=/mnt/ceph/storage,target=/mnt/ceph/storage ${REPO}/${IMAGE}:${VERSION} && make -s clean

run-cpu: build
	docker run --rm -it -p 8888:8888 -p 6006:6006 --mount type=bind,source=${PWD},target=/tf ${REPO}/${IMAGE}:${VERSION} && make -s clean

clean:
	sudo chown -R 1000:1000 .
