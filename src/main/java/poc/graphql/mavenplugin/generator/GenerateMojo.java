package poc.graphql.mavenplugin.generator;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jruby.embed.ScriptingContainer;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.PACKAGE)
public class GenerateMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    /**
     * The Json file containing the result of an introspection query against the server.
     * Use https://github.com/prisma-labs/get-graphql-schema ?
     */
    @Parameter(property = "pathToSchema")
    File pathToSchema;

    /**
     * The license header that will be added to all Java files
     * ERB ruby file.
     */
    @Parameter(property = "licenseHeaderFilePath")
    File licenseHeaderFilePath;

    /**
     * The Java package of the generated classes
     */
    @Parameter(property = "outputPackageName")
    String outputPackageName;

    /**
     * The directory where the generated Java classes will be output.
     */
    @Parameter(property = "outputDirPath")
    File outputDirPath;


    private void generateDTOClasses() throws IOException, ScriptException {
        getLog().info("### generate-graphql-client :: generate :: BEGIN ###");
        getLog().info("pathToSchema: " + pathToSchema.getCanonicalPath());
        getLog().info("licenseHeaderFilePath: " + licenseHeaderFilePath.getCanonicalPath());
        getLog().info("outputPackageName: " + outputPackageName);
        getLog().info("outputDirPath: " + outputDirPath.getCanonicalPath());

        // For debug purpose
        this.listScriptingEngines();

        // How to call Ruby from Java: https://github.com/jruby/jruby/wiki/RedBridge
        // Here we use the "Core" approach (vs JSR223 or Apache BSF)
        ScriptingContainer container = new ScriptingContainer();

        InputStream rubyGeneratorInputStream = getClass().getResourceAsStream("/generator.rb");

        Object receiver = container.runScriptlet(new BufferedReader(new InputStreamReader(rubyGeneratorInputStream)), "src/main/ruby/generator.rb");
        Object[] args = new Object[4];
        args[0] = pathToSchema.getCanonicalPath();
        args[1] = licenseHeaderFilePath.getCanonicalPath();
        args[2] = outputPackageName;
        args[3] = outputDirPath.getCanonicalPath();

        container.callMethod(receiver, "generate", args);

        getLog().info("### generate-graphql-client :: generate :: END ###");
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("### generate-graphql-client :: BEGIN ###");

        try {
            this.generateDTOClasses();
        } catch (IOException | ScriptException e) {
            /*
                Unrecoverable exceptions here so we rethrow a MojoExecutionException.
                MojoFailureException in the other hand, is for exceptions that can be ignored for the build to continue.
                Cf. https://books.sonatype.com/mvnref-book/reference/writing-plugins-sect-custom-plugin.html
             */
            getLog().error(e);
            throw new MojoExecutionException(e.getMessage());
        }

        getLog().info("### generate-graphql-client :: END ###");
    }


    /**
     * List all the scripting engine available to the plugin.
     * Should return at least "jruby".
     */
    private void listScriptingEngines() {
        ScriptEngineManager mgr = new ScriptEngineManager();
        for (ScriptEngineFactory factory : mgr.getEngineFactories()) {
            getLog().debug("ScriptEngineFactory Info:");
            getLog().debug(String.format("\tScript Engine: %s (%s)\n", factory.getEngineName(), factory.getEngineVersion()));
            getLog().debug(String.format("\tLanguage: %s (%s)\n", factory.getLanguageName(), factory.getLanguageVersion()));
            for (String name : factory.getNames()) {
                getLog().debug( String.format("\tEngine Alias: %s\n", name));
            }
        }
    }
}