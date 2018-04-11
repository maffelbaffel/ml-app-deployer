package com.marklogic.appdeployer.command.forests;

import java.util.Arrays;
import java.util.List;

public class HostConfiguration {

	private String hostName;
	private List<String> dataDirectories;

	public HostConfiguration(String hostName, String... dataDirectories) {
		this.hostName = hostName;
		this.dataDirectories = Arrays.asList(dataDirectories);
	}

	public String getHostName() {
		return hostName;
	}

	public List<String> getDataDirectories() {
		return dataDirectories;
	}
}
