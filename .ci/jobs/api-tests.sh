#!/bin/bash

# initialize minio configuration for MOLGENIS API tests
export MINIO_ACCESS_KEY=molgenis
export MINIO_SECRET_KEY=molgenis

export MAVEN_SKIP_RC=true
export MAVEN_OPTS="-XX:TieredStopAtLevel=1 -noverify"

# run the api-tests
mvn verify -pl molgenis-api-tests --batch-mode --quiet \
  -Dmaven.test.redirectTestOutputToFile=true \
  -Dit_db_user=postgres \
  -Dit_db_password=molgenis \
  -Dit_db_port=5432