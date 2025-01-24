# Use the Azure Data Explorer Kustainer image as the base
FROM mcr.microsoft.com/azuredataexplorer/kustainer-linux:latest@sha256:8e14a6fea2e49a238934b50939e8bba11d3513bd34d849712fc97ef9bc10f602

# Set environment variables
ENV ACCEPT_EULA=Y

RUN yum install -y curl
COPY kusto-init.sh /
COPY start-kusto.sh /
RUN chmod +x /kusto-init.sh
RUN chmod +x /start-kusto.sh