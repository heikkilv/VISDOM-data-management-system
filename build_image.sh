#!/bin/sh

COMPONENT_NAME=$1
DOCKERFILE_NAME=$2
SOURCE_FOLDER=$3

IMAGE_TAG=${DOCKER_REGISTRY}/visdom/${COMPONENT_NAME}:${CI_COMMIT_REF_SLUG}

cd ${SOURCE_FOLDER}
docker pull ${IMAGE_TAG} || true
docker build --pull --cache-from ${IMAGE_TAG} --tag ${IMAGE_TAG} --file ${DOCKERFILE_NAME} .
docker push ${IMAGE_TAG}
