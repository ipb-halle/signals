#!/bin/bash
#
# Download the API definition of Signals Notebook
#
URL_BASE=https://ipb-halle-trial.signalsresearch.revvitycloud.eu/docs/extapi/apidoc/v1
RELEASE="$1"

# get the index document
curl -O https://ipb-halle-trial.signalsresearch.revvitycloud.eu/docs/extapi/apidoc/v1/index.yaml

# get the individual sub-documents
grep -E '  # .*\.yaml' index.yaml | cut -c4- | xargs -i curl -o "{}?$RELEASE" $URL_BASE/{}
