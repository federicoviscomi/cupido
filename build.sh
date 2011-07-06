echo Removing stubs...
find . -name '*_Stub.class' -delete

echo Generating new stubs...
CLASSPATH="cupidoGWT/war/WEB-INF/classes:cupidoBackendImpl/bin:cupidoCommon/bin:cupidoGWT/war/WEB-INF/lib/mysql-connector-java-5.1.16-bin.jar"
rmic -d cupidoGWT/war/WEB-INF/classes -classpath "$CLASSPATH" 'unibo.as.cupido.server.CupidoServlet$2'
rmic -d cupidoBackendImpl/bin -classpath "$CLASSPATH" 'unibo.as.cupido.backend.gtm.GlobalTableManager'
rmic -d cupidoBackendImpl/bin -classpath "$CLASSPATH" 'unibo.as.cupido.backend.ltm.LocalTableManager'
rmic -d cupidoBackendImpl/bin -classpath "$CLASSPATH" 'unibo.as.cupido.backend.gtm.GlobalChatImpl'
rmic -d cupidoBackendImpl/bin -classpath "$CLASSPATH" 'unibo.as.cupido.backend.playerUI.AutomaticServlet$AutomaticServletNotificationInterface'
rmic -d cupidoBackendImpl/bin -classpath "$CLASSPATH" 'unibo.as.cupido.backend.playerUI.ViewerUI'
rmic -d cupidoGWT/war/WEB-INF/classes -classpath "$CLASSPATH" 'unibo.as.cupido.backend.gtm.GlobalChatImpl'
rmic -d cupidoGWT/war/WEB-INF/classes -classpath "$CLASSPATH" 'unibo.as.cupido.backend.gtm.GlobalTableManager'
rmic -d cupidoGWT/war/WEB-INF/classes -classpath "$CLASSPATH" 'unibo.as.cupido.backend.ltm.LocalTableManager'
rmic -d cupidoGWT/war/WEB-INF/classes -classpath "$CLASSPATH" 'unibo.as.cupido.backend.table.SingleTableManager'

echo "Removing the old cupido/ directory..."
rm -rf cupido/

echo "Creating an updated cupido/ directory..."
cp -R cupidoGWT/war/ .
cp -R cupidoCommon/bin/unibo/as/cupido/common/ war/WEB-INF/classes/unibo/as/cupido/
cp -R cupidoGWT/war/WEB-INF/classes/unibo/as/cupido/backend war/WEB-INF/classes/unibo/as/cupido/
mv war/ cupido

sleep 1
