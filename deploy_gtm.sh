echo Deploying GlobalTableManager on localhost...

CLASSPATH="cupidoGWT/war/WEB-INF/classes:cupidoBackendImpl/bin:cupidoCommon/bin:cupidoGWT/war/WEB-INF/lib/mysql-connector-java-5.1.16-bin.jar"

java -classpath "$CLASSPATH" unibo.as.cupido.backend.gtm.GlobalTableManager &

sleep 2
GTM_PID="$!"

read

echo "Terminating GlobalTableManager..."

kill "$GTM_PID"

sleep 1
