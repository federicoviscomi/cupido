Il docente dovrebbe iniziare la lettura dal file doc/master_document.pdf

build.sh
ricrea gli stub e la cartella cupido.

chech_copyright.sh
check if all source files have copyright in.

cupidoBackendImpl/ 
contiene il codice relativo ai game server che implementano la logica di gioco

cupidoGWT/
contiene il codice della servlet e del client

cupidoCommon/
è una libreria condivisa tra i progetti cupidoGWT e cupidoBackendImpl

cupido/
serve per il deploy della servlet

localTableManager.config
è un file di configurazione usato dal progetto cupidoBackendImpl

deploy.sh
script per il deployment in locale di tutto il backend

deploy_gtm.sh
script per il deployment del Global Table Manager

deploy_ltm.sh
script per il deployment di un Local Table Manager

doc/
contiene la documentazione su come è stato svolto tutto il lavoro del gruppo

how_to_deploy.txt
contains deploy instructions

recreate_db.sh 
crea ed inizializza il database (da utilizzare solo dopo aver installato mysql)

rmi_registry.sh
deploy_ltm.sh
script per il deployment del rmi registry
