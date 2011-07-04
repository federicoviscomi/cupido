
echo Removing stubs...
find . -name '*_Stub.class' -delete

echo Generating new stubs...
CLASSPATH="cupidoGWT/war/WEB-INF/classes:cupidoBackendImpl/bin:cupidoCommon/bin:cupidoGWT/war/WEB-INF/lib/mysql-connector-java-5.1.16-bin.jar"
rmic -d cupidoGWT/war/WEB-INF/classes -classpath "$CLASSPATH" 'unibo.as.cupido.server.CupidoServlet$2'
rmic -d cupidoBackendImpl/bin -classpath "$CLASSPATH" 'unibo.as.cupido.backend.gtm.GlobalTableManager'
rmic -d cupidoBackendImpl/bin -classpath "$CLASSPATH" 'unibo.as.cupido.backend.ltm.LocalTableManager'
rmic -d cupidoBackendImpl/bin -classpath "$CLASSPATH" 'unibo.as.cupido.backend.gtm.GlobalChatImpl'
rmic -d cupidoBackendImpl/bin -classpath "$CLASSPATH" 'unibo.as.cupido.backend.playerUI.AutomaticServlet$RemoteBotNotificationInterface'
rmic -d cupidoBackendImpl/bin -classpath "$CLASSPATH" 'unibo.as.cupido.backend.playerUI.ViewerUI'
rmic -d cupidoGWT/war/WEB-INF/classes -classpath "$CLASSPATH" 'unibo.as.cupido.backend.gtm.GlobalChatImpl'
rmic -d cupidoGWT/war/WEB-INF/classes -classpath "$CLASSPATH" 'unibo.as.cupido.backend.gtm.GlobalTableManager'
rmic -d cupidoGWT/war/WEB-INF/classes -classpath "$CLASSPATH" 'unibo.as.cupido.backend.ltm.LocalTableManager'
rmic -d cupidoGWT/war/WEB-INF/classes -classpath "$CLASSPATH" 'unibo.as.cupido.backend.table.SingleTableManager'

echo "Removing the old war/ directory..."
rm -rf war/

echo "Creating an updated war/ directory..."
cp -R cupidoGWT/war/ .
cp -R cupidoCommon/bin/unibo/as/cupido/common/ war/WEB-INF/classes/unibo/as/cupido/
cp -R cupidoGWT/war/WEB-INF/classes/unibo/as/cupido/backend war/WEB-INF/classes/unibo/as/cupido/

echo Running rmiregistry...
rmiregistry -J-classpath -J"$CLASSPATH" &

RMIREGISTRY_PID="$!"
PIDS="$!"

sleep 1

java -classpath "$CLASSPATH" unibo.as.cupido.backend.gtm.GlobalTableManager &

PIDS="$PIDS $!"

sleep 2

java -classpath "$CLASSPATH" unibo.as.cupido.backend.ltm.LocalTableManager &

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
  kill "$pid"
done

sleep 1
