# Notes on Signals OpenAPI
The OpenAPI schema support feature "oneOf" is not supported for Java
or jaxrs (in fact we only found it supported for Python). The Signals 
API makes ample use of this feature and cannot be converted to Java 
code in a straightforward manner.
Additionally, we found several redefinitions of basic objects 
(JsonApiData, JsonApiResponse, etc.), which introduces additional 
difficulties.

