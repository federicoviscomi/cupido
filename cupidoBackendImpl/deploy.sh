
echo Removing stubs...
find . -name '*_Stub.class' -delete

echo Generating new stubs...
CLASSPATH="cupidoGWT/war/WEB-INF/classes:cupidoBackendImpl/bin:cupidoBackendInterfaces/bin"
rmic -d cupidoGWT/war/WEB-INF/classes -classpath "$CLASSPATH" 'unibo.as.cupido.server.CupidoServlet$1'
rmic -d cupidoBackendImpl/bin -classpath "$CLASSPATH" 'unibo.as.cupido.server.CupidoServlet$1'
rmic -d cupidoBackendImpl/bin -classpath "$CLASSPATH" 'unibo.as.cupido.backend.gtm.GlobalTableManager'
rmic -d cupidoBackendImpl/bin -classpath "$CLASSPATH" 'unibo.as.cupido.backend.ltm.LocalTableManager'

echo Running rmiregistry...
COMMAND="rmiregistry -J-classpath -J$CLASSPATH"
echo "$COMMAND"
$COMMAND
