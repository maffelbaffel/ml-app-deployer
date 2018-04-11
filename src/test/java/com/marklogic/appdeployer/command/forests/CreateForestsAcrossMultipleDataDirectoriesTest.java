package com.marklogic.appdeployer.command.forests;

import com.marklogic.appdeployer.AbstractAppDeployerTest;
import com.marklogic.appdeployer.ConfigDir;
import com.marklogic.appdeployer.command.databases.DeployContentDatabasesCommand;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

public class CreateForestsAcrossMultipleDataDirectoriesTest extends AbstractAppDeployerTest {

	@Test
	public void test() {
		appConfig.setConfigDir(new ConfigDir(new File("src/test/resources/sample-app/db-only-config")));
		appConfig.setForestDataDirectories(Arrays.asList("build/forests/one", "build/forests/two"));
		initializeAppDeployer(new DeployContentDatabasesCommand());
		deploySampleApp();
	}
}
