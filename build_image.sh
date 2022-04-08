#!/bin/sh

COMPONENT_NAME=$1
DOCKERFILE_NAME=$2
SOURCE_FOLDER=$3
USE_CACHE=$4

IMAGE_TAG=${DOCKER_REGISTRY}/visdom/${COMPONENT_NAME}:${CI_COMMIT_REF_SLUG}

cd ${SOURCE_FOLDER}

if [[ "$USE_CACHE" == "true" ]]
then
    docker pull ${IMAGE_TAG} || true
    docker build --pull --cache-from ${IMAGE_TAG} --tag ${IMAGE_TAG} --file ${DOCKERFILE_NAME} .
else
    docker build --tag ${IMAGE_TAG} --file ${DOCKERFILE_NAME} .
fi

docker push ${IMAGE_TAG}
