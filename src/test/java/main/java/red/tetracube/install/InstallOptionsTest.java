package main.java.red.tetracube.install;

import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import red.tetracube.install.InstallOptions;

import java.io.File;
import java.io.IOException;

@QuarkusMainTest
public class InstallOptionsTest {

    @BeforeAll
    public static void prepareTests() throws IOException {
        var tmpDir = System.getProperty("java.io.tmpdir");

        var k8sFile = new File(tmpDir + "/k8sFile.json");
        k8sFile.createNewFile();

        var certKeyFile = new File(tmpDir + "/certKey.pem");
        certKeyFile.createNewFile();

        var certFile = new File(tmpDir + "/cert.pem");
        certFile.createNewFile();
    }

    @Test
    public void testTestHelpOption(QuarkusMainLauncher launcher) {
        var tmpDir = System.getProperty("java.io.tmpdir");
        var result = launcher.launch(
                "install",
                "--k8s-config=" + tmpDir + "/k8sFile.json",
                "--cert-files=" + tmpDir + "/certKey.pem," + tmpDir + "/cert.pem",
                "--installation-name=test",
                "--hostname=tetracube.red"
        );
        Assertions.assertTrue(result.getOutput().contains("K8s config -> k8sFile.json"));
        Assertions.assertTrue(result.getOutput().contains("Hostname config -> tetracube.red"));
        Assertions.assertTrue(result.getOutput().contains("Installation name config -> test"));
        Assertions.assertTrue(result.getOutput().contains("DB password config ->"));
        Assertions.assertTrue(result.getOutput().contains("Cert files config: certKey.pem, cert.pem"));
    }
}
