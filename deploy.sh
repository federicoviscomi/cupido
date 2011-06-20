echo Removing backend interfaces...
rm -rf cupidoGWT/war/WEB-INF/classes/unibo/as/cupido/common

echo Copying backend interfaces...
cp -r cupidoCommon/bin/unibo/as/cupido/common/ cupidoGWT/war/WEB-INF/classes/unibo/as/cupido/

echo Removing stubs...
find . -name '*_Stub.class' -delete

echo Generating new stubs...
CLASSPATH="cupidoGWT/war/WEB-INF/classes:cupidoBackendImpl/bin:cupidoCommon/bin:cupidoGWT/war/WEB-INF/lib/mysql-connector-java-5.1.16-bin.jar"
rmic -d cupidoGWT/war/WEB-INF/classes -classpath "$CLASSPATH" 'unibo.as.cupido.server.CupidoServlet$2'
rmic -d cupidoBackendImpl/bin -classpath "$CLASSPATH" 'unibo.as.cupido.backendInterfacesImpl.gtm.GlobalTableManager'
rmic -d cupidoBackendImpl/bin -classpath "$CLASSPATH" 'unibo.as.cupido.backendInterfacesImpl.ltm.LocalTableManager'
rmic -d cupidoBackendImpl/bin -classpath "$CLASSPATH" 'unibo.as.cupido.backendInterfacesImpl.GlobalChatImpl'
rmic -d cupidoGWT/war/WEB-INF/classes -classpath "$CLASSPATH" 'unibo.as.cupido.backendInterfacesImpl.GlobalChatImpl'
rmic -d cupidoGWT/war/WEB-INF/classes -classpath "$CLASSPATH" 'unibo.as.cupido.backendInterfacesImpl.gtm.GlobalTableManager'
rmic -d cupidoGWT/war/WEB-INF/classes -classpath "$CLASSPATH" 'unibo.as.cupido.backendInterfacesImpl.table.SingleTableManager'

echo Running rmiregistry...
rmiregistry -J-classpath -J"$CLASSPATH" &

RMIREGISTRY_PID="$!"
PIDS="$!"

sleep 1

java -classpath "$CLASSPATH" unibo.as.cupido.backendInterfacesImpl.gtm.GlobalTableManager &

PIDS="$PIDS $!"

sleep 2

java -classpath "$CLASSPATH" unibo.as.cupido.backendInterfacesImpl.ltm.LocalTableManager &

PIDS="$PIDS $!"

sleep 1

echo "*****************************"
echo "* All processes started.    *"
echo "* Press Enter to kill them. *"
echo "*****************************"

read

echo "Terminating child processes, please wait..."

for pid in $PIDS
do
  kill -9 "$pid"
done
