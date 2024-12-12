#!/bin/bash 
if [[ "$ACCEPT_EULA" == "Y" ]]; then
    ./kusto-init.sh &
	  mkdir /kusto/tmp
	  export TMP=/kusto/tmp/
	  export TEMP=$TMP
	  export TMPDIR=$TMP
	  pushd /kusto/Kusto.Personal/;
	  ./Kusto.Personal -gw -https:false -AutomaticallyDetachCorruptDatabases:true -enableRowStore:true $@
else
	  echo "EULA Not Accepted"; 
	  exit 13
fi
