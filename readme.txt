Il docente dovrebbe iniziare la lettura dal file doc/master_document.pdf

cupidoBackendImpl/ 
contiene il codice relativo ai game server che implementano la logica di gioco

cupidoGWT/
contiene il codice della servlet e del client

cupidoCommon/
è una libreria condivisa tra i progetti cupidoGWT e cupidoBackendImpl

doc/
contiene la documentazione su come è stato svolto tutto il lavoro del gruppo

war/
deve essere copiata dento ad un server apache-tomcat per avviare la servlet

localTableManager.config
è un file di configurazione usato dal progetto cupidoBackendImpl

deploy.sh
è uno script per il deployment in locale di tutto il backend

recreate_db.sh 
crea ed inizializza il database (da utilizzare solo dopo aver installato mysql 5.1)
