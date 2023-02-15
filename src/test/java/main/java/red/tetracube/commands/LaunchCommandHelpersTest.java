package main.java.red.tetracube.commands;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusMainTest
public class LaunchCommandHelpersTest {

    @Test
    @Launch("--help")
    public void testTestHelpOption(LaunchResult result){
        Assertions.assertTrue(result.getOutput().contains("Usage: tms [-hV] [COMMAND]"));
    }

    @Test
    @Launch(value = {"install", "--help"}, exitCode = 0)
    public void testInstallHelpOption(LaunchResult result) {
        Assertions.assertTrue(result.getOutput().contains("Usage: tms install"));
    }
}
