#!/bin/bash

echo "Running LocalTableManager on localhost..."

CLASSPATH="cupidoGWT/war/WEB-INF/classes:cupidoBackendImpl/bin:cupidoCommon/bin:cupidoGWT/war/WEB-INF/lib/mysql-connector-java-5.1.16-bin.jar"

# The "exec" is needed so that killing this process also kills the LTM.
exec java -classpath "$CLASSPATH" unibo.as.cupido.backend.ltm.LocalTableManager
