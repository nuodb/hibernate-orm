#!/bin/bash

SCRIPT_DIR=$(cd `dirname $0` && pwd)
IMG_NAME=${NUODB_IMAGE:-"nuodb/nuodb:5.1"}

# Since we don't have sudo on the build servers, we need to use
# docker container to run as original user, mount the directories
# we need to clean and remove the old volX directories.
docker run --rm --volume "$SCRIPT_DIR:/var/opt/nuodb" "$IMG_NAME" /bin/bash -c 'rm -fr /var/opt/nuodb/vol{1,2}'

mkdir "$SCRIPT_DIR"/{vol1,vol2}
chmod a+rw "$SCRIPT_DIR"/{vol1,vol2}

docker rm -f te1 sm1 ad1

docker network create nuodb-net

docker run -d --name ad1 --rm \
    --hostname ad1 \
    --net nuodb-net \
    -p 8888:8888 \
    -p 48004:48004 \
    -p 48005:48005 \
    -e "NUODB_DOMAIN_ENTRYPOINT=ad1" \
    "${IMG_NAME}" nuoadmin

sleep 3
docker run -d --name sm1 --hostname sm1 --rm \
    --volume "$SCRIPT_DIR/vol1":/var/opt/nuodb/backup \
    --volume "$SCRIPT_DIR/vol2":/var/opt/nuodb/archive \
    --net nuodb-net "${IMG_NAME}" nuodocker \
    --api-server ad1:8888 start sm \
    --db-name hibernate_orm_test --server-id ad1 \
    --dba-user hibernate_orm_test --dba-password hibernate_orm_test \
    --labels "zone east node localhost" \
    --archive-dir /var/opt/nuodb/archive

sleep 3
docker run -d --name te1 --hostname te1 --rm \
    --net nuodb-net "${IMG_NAME}" nuodocker \
    --api-server ad1:8888 start te \
    --db-name hibernate_orm_test --server-id ad1

sleep 5
docker run -t --net nuodb-net "${IMG_NAME}" \
    nuocmd --api-server ad1:8888 show domain
