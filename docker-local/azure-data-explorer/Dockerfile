# Use the Azure Data Explorer Kustainer image as the base
FROM mcr.microsoft.com/azuredataexplorer/kustainer-linux:latest@sha256:2e1875ad73513ae17ddd14302615bed9baf43f66eb1196d74deba5c1279f227c

# Set environment variables
ENV ACCEPT_EULA=Y

RUN yum install -y curl
COPY kusto-init.sh /
COPY start-kusto.sh /
RUN chmod +x /kusto-init.sh
RUN chmod +x /start-kusto.sh