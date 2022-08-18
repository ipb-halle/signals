# Helper Tool for Signals Notebook

This project bundles code for migration of IPB data sources, for backup, maintenance and external services for the Perkin Elmer Signals Notebook. The command line tool relies on the following two configuration files:

- <code>conf/openejb.xml</code> for data source setup (see OpenEJB documentation)
- <code>config.json</code> (path provided as command line argument) for API-KEY and the Signals Notebook base URL 

To run the code, build the project 

    mvn package

adjust the config files and start the tool with the following command line:

    java -jar target/signals-1.0.jar conf/config.json

