# Generate GraphQL Client Maven Plugin

This project is currently in PoC state.

# Test
In a directory containing a pom.xml: 

``mvn poc.graphql:generate -DpathToSchema=src/main/resources/superhero.json -DlicenseHeaderFilePath=src/main/resources/License.erb -DoutputPackageName=com.myapp.superhero -DoutputDirPath=target/DTO``

# TODO

### Licensing
 
This project is licensed under the Apache V2 License. See [LICENSE](LICENSE) for more information.
