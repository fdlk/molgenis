#!/bin/bash
mvn -DskipTests=true install
cd ../molgenis-app-omx
mvn jetty:run
