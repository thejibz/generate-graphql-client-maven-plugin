require 'ruby-graphql-java-client-generator'
require 'graphql_schema'
require 'json'
require 'fileutils'

def generate(pathToSchema, licenseHeaderFilePath, outputPackageName, outputDirPath)
    puts "### WORKDIR: #{Dir.pwd}"

    ## Convert Windows paths to Unix paths
    pathToSchema = pathToSchema.gsub!(File::ALT_SEPARATOR, File::SEPARATOR) if File::ALT_SEPARATOR
    licenseHeaderFilePath = licenseHeaderFilePath.gsub!(File::ALT_SEPARATOR, File::SEPARATOR) if File::ALT_SEPARATOR
    outputDirPath = outputDirPath.gsub!(File::ALT_SEPARATOR, File::SEPARATOR) if File::ALT_SEPARATOR
    #

    ## Create the outputDirPath if it doesn't exist
    FileUtils.mkdir_p outputDirPath
    #

    ## Parse the graphql schema
    introspection_result = File.read(pathToSchema)
    schema = GraphQLSchema.new(JSON.parse(introspection_result))
    #

    GraphQLJavaGen.new(schema,
      package_name: outputPackageName,
      license_header_file: licenseHeaderFilePath,
      nest_under: 'Schema', # Not used, but must be defined
      custom_scalars: [
        GraphQLJavaGen::Scalar.new(
          type_name: 'Decimal',
          java_type: 'BigDecimal',
          deserialize_expr: ->(expr) { "new BigDecimal(jsonAsString(#{expr}, key))" },
          imports: ['java.math.BigDecimal'],
        ),
      ]
    ).save_granular(outputDirPath)
end