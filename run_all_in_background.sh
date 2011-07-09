
#!/bin/bash

./build.sh

for script in rmiregistry gtm chat ltm
do
  ./run_${script}.sh >${script}.log &
  sleep 2
done
