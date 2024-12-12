# Use the Azure Data Explorer Kustainer image as the base
FROM mcr.microsoft.com/azuredataexplorer/kustainer-linux:latest

# Set environment variables
ENV ACCEPT_EULA=Y

RUN yum install -y curl
COPY kusto-init.sh /
COPY start-kusto.sh /
RUN chmod +x /kusto-init.sh
RUN chmod +x /start-kusto.sh