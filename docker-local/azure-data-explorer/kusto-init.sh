#!/bin/sh

# Environment variable to check if the DB is already initialized
QUERY_URL="http://localhost:8080/v1/rest/query"
QUERY_DATA='{
    "db": "NetDefaultDB",
    "csl": ".show table KPI_RENDICONTAZIONI"
}'

# Environment variable to check DB status
MGMT_URL="http://localhost:8080/v1/rest/mgmt"
MGMT_DATA='{
    "db": "NetDefaultDB",
    "csl": ".show tables"
}'
# Define the timeout (300 seconds = 5 minutes)
TIMEOUT=300
START_TIME=$(date +%s)

# Loop to check every 5 seconds
while true; do
    # Calculate the elapsed time
    CURRENT_TIME=$(date +%s)
    ELAPSED_TIME=$((CURRENT_TIME - START_TIME))

    # Check if elapsed time exceeds the timeout
    if [ $ELAPSED_TIME -ge $TIMEOUT ]; then
        echo "Timeout reached: Exiting script after $((TIMEOUT / 60)) minutes."
        exit 1
    fi

    # Perform the curl request and capture the HTTP status code
    MGMT_STATUS_CODE=$(curl -s -o /dev/null -i -w "%{http_code}" --header 'Content-Type: application/json' --data "$MGMT_DATA" --location "$MGMT_URL")

    # Check if the status code is 200
    if [ "$MGMT_STATUS_CODE" -eq 200 ]; then
        echo "Kusto DB is ready"
        echo "Sending REST call to verify if KPI_RENDICONTAZIONI exists"
        QUERY_STATUS_CODE=$(curl -s -o /dev/null -i -w "%{http_code}" --header 'Content-Type: application/json' --data "$QUERY_DATA" --location "$QUERY_URL")
        if [ "$QUERY_STATUS_CODE" -ne 200 ]; then
            echo "KPI_RENDICONTAZIONI does not exist. Initiating DB setup"

            # Creating and populating the KPI_RENDICONTAZIONI table
            curl -s -o /dev/null --location 'http://localhost:8080/v1/rest/mgmt' \
            --header 'Content-Type: application/json' \
            --data '{
                "db": "NetDefaultDB",
                "csl": ".create table KPI_RENDICONTAZIONI (GIORNATA_PAGAMENTO: datetime,ID_BROKER_PSP: string,ID_PSP: string,TOTALE_FLUSSI: int,FLUSSI_ASSENTI: int,FLUSSI_PRESENTI: int,FDR_IN_RITARDO_FIRST_VERSION: int,FDR_IN_RITARDO_LAST_VERSION: int,TOTALE_DIFF_AMOUNT: int,TOTALE_DIFF_NUM: int)"
            }'
            echo "KPI_RENDICONTAZIONI table created"

            curl -s -o /dev/null --location 'http://localhost:8080/v1/rest/mgmt' \
            --header 'Content-Type: application/json' \
            --data '{
                "db": "NetDefaultDB",
                "csl": ".ingest inline into table KPI_RENDICONTAZIONI <| 2023-10-01T00:00:00Z, 01153230360,SARDIT31,3,0,3,0,0,0,0"
            }'
            echo "KPI_RENDICONTAZIONI table populated"

            # Creating and populating the KPI_RENDICONTAZIONI_DETAILS table
            curl -s -o /dev/null --location 'http://localhost:8080/v1/rest/mgmt' \
            --header 'Content-Type: application/json' \
            --data '{
                "db": "NetDefaultDB",
                "csl": ".create table KPI_RENDICONTAZIONI_DETAILS (DATA_PAGAMENTO: datetime,NOTICE_ID: string,IUV: string,IUR: string,PA_EMITTENTE: string,PA_TRANSFER: string,AMOUNT_TRANSFER: decimal,IBAN: string,ID_TRANSFER: int,IDSP: int,COD_ESITO: int,ID_PSP: string,ID_BROKER_PSP: string,MOD_TYPE: int,ID_FLUSSO: string,FIRST_VERSION: datetime,LAST_VERSION: datetime,IS_REND: bool,DIFF_DAYS_FIRST_VERSION: int,DIFF_DAYS_LAST_VERSION: int,LIMIT_DATE_KPI: datetime)"
            }'
            echo "KPI_RENDICONTAZIONI_DETAILS table created"

            curl -s -o /dev/null --location 'http://localhost:8080/v1/rest/mgmt' \
            --header 'Content-Type: application/json' \
            --data '{
                "db": "NetDefaultDB",
                "csl": ".ingest inline into table KPI_RENDICONTAZIONI_DETAILS <| 2023-11-08T14:00:00.021359Z,111362066159717101,11362066159717101,03436fab0316458a8eaa1db37df9f4c7,00488410010,00488410010,11.15,IT62Q0200809440000030043534,1,1,0,PPAYITR1XXX,06874351007,2,2023-11-09PPAYITR1XXX-S4004739269,202"
            }'
            echo "KPI_RENDICONTAZIONI_DETAILS table populated"
            echo "Kusto DB initialization completed"
            break
        else
            echo "Tables already exist. Kusto DB initialization skipped"
            break
        fi
    else
        echo "DB not started. Retrying in 5 seconds"
    fi
    # Wait for 5 seconds before retrying
    sleep 5
done
