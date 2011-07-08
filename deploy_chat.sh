echo Deploying the global chat on localhost...

CLASSPATH="cupidoGWT/war/WEB-INF/classes:cupidoBackendImpl/bin:cupidoCommon/bin:cupidoGWT/war/WEB-INF/lib/mysql-connector-java-5.1.16-bin.jar"

java -classpath "$CLASSPATH" unibo.as.cupido.backend.gtm.GlobalChatImpl
