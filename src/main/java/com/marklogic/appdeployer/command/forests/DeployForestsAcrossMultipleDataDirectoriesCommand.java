package com.marklogic.appdeployer.command.forests;

import com.marklogic.appdeployer.command.AbstractCommand;
import com.marklogic.appdeployer.command.CommandContext;
import com.marklogic.mgmt.api.forest.Forest;
import com.marklogic.mgmt.api.forest.ForestReplica;

import java.util.*;

/**
 * For the replicas, if count is 1, then start creating replicas at host1/mount2. If count is 2, then create another
 * batch of replicas, this time starting at host1/mount3.
 *
 * TODO Can add support for large/fast data directories.
 */
public class DeployForestsAcrossMultipleDataDirectoriesCommand extends AbstractCommand {

	private List<HostConfiguration> hostConfigurations;
	private int forestsPerDataDirectory = 1;
	private int replicasPerPrimaryForest = 0;
	private String databaseName;

	public DeployForestsAcrossMultipleDataDirectoriesCommand(String databaseName) {
		this.databaseName = databaseName;
	}

	@Override
	public void execute(CommandContext context) {
		if (hostConfigurations == null || hostConfigurations.isEmpty()) {
			return;
		}
	}

	public static void main(String[] args) {
		List<HostConfiguration> hostConfigurations = new ArrayList<>();
		DeployForestsAcrossMultipleDataDirectoriesCommand command = new DeployForestsAcrossMultipleDataDirectoriesCommand("Documents");
		command.setHostConfigurations(hostConfigurations);
		command.setForestsPerDataDirectory(2);
		command.setReplicasPerPrimaryForest(2);
		hostConfigurations.add(new HostConfiguration("host1", "/mldata1", "/mldata2", "/mldata3"));
		hostConfigurations.add(new HostConfiguration("host2", "/mldata1", "/mldata2", "/mldata3"));
		hostConfigurations.add(new HostConfiguration("host3", "/mldata1", "/mldata2", "/mldata3"));

		List<Forest> forests = command.buildForests();
		for (Forest f : forests) {
			System.out.println(f.getHost() + ":" + f.getDataDirectory() + ":" + f.getForestName());
			for (ForestReplica r : f.getForestReplica()) {
				System.out.println(r.getHost() + ":" + r.getDataDirectory() + ":" + r.getReplicaName());
			}
			System.out.println("");
		}
	}

	public List<Forest> buildForests() {
		/**
		 * Now we can iterate over each of these HDD's and create N forests per HDD. We'll use counters for the host and the forest number.
		 */
		List<Forest> forests = new ArrayList<>();
		final int numberOfHosts = hostConfigurations.size();
		int forestCounter = 1;
		for (int hostIndex = 0; hostIndex < numberOfHosts; hostIndex++) {
			HostConfiguration hostConfiguration = hostConfigurations.get(hostIndex);
			final int numberOfDataDirectories = hostConfiguration.getDataDirectories().size();
			for (int dataDirectoryIndex = 0; dataDirectoryIndex < numberOfDataDirectories; dataDirectoryIndex++) {
				final String dataDirectory = hostConfiguration.getDataDirectories().get(dataDirectoryIndex);
				for (int j = 0; j < forestsPerDataDirectory; j++) {
					Forest f = new Forest();
					f.setHost(hostConfiguration.getHostName());
					f.setDataDirectory(dataDirectory);
					f.setDatabase(databaseName);
					// TODO Interface for customizing this?
					final String forestName = databaseName + "-" + forestCounter;
					forestCounter++;
					f.setForestName(forestName);
					forests.add(f);

					/**
					 * For replicas - we can create these over all of the other hosts. And when we do that, we'll start at
					 * j + 1.
					 */
					if (replicasPerPrimaryForest > 0) {
						List<HostConfiguration> replicaHostConfigurations = new ArrayList<>();
						int pointer = hostIndex + 1;
						for (int i = 0; i < numberOfHosts - 1; i++) {
							if (pointer == hostIndex) {
								pointer++;
							}
							if (pointer == numberOfHosts) {
								pointer = 0;
								if (pointer == hostIndex) {
									pointer++;
								}
							}
							replicaHostConfigurations.add(hostConfigurations.get(pointer));
							pointer++;
						}

						List<ForestReplica> replicas = new ArrayList<>();
						int hostPointer = 0;
						int dataDirectoryPointer = dataDirectoryIndex + 1;

						for (int replicaCounter = 0; replicaCounter < replicasPerPrimaryForest; replicaCounter++) {
							ForestReplica replica = new ForestReplica();
							// TODO Interface for customizing this?
							replica.setReplicaName(forestName + "-replica-" + (replicaCounter + 1));
							replicas.add(replica);

							if (hostPointer >= replicaHostConfigurations.size()) {
								hostPointer = 0;
							}

							HostConfiguration replicaHostConfiguration = replicaHostConfigurations.get(hostPointer);
							replica.setHost(replicaHostConfiguration.getHostName());

							if (dataDirectoryPointer == dataDirectoryIndex) {
								dataDirectoryPointer++;
							}
							if (dataDirectoryPointer == numberOfDataDirectories) {
								dataDirectoryPointer = 0;
								if (dataDirectoryPointer == dataDirectoryIndex) {
									dataDirectoryPointer++;
								}
							}
							replica.setDataDirectory(hostConfiguration.getDataDirectories().get(dataDirectoryPointer));

							hostPointer++;
							dataDirectoryPointer++;
						}
						f.setForestReplica(replicas);
					}

				}
			}
		}

		return forests;
	}

	public void setHostConfigurations(List<HostConfiguration> hostConfigurations) {
		this.hostConfigurations = hostConfigurations;
	}

	public void setForestsPerDataDirectory(int forestsPerDataDirectory) {
		this.forestsPerDataDirectory = forestsPerDataDirectory;
	}

	public void setReplicasPerPrimaryForest(int replicasPerPrimaryForest) {
		this.replicasPerPrimaryForest = replicasPerPrimaryForest;
	}
}
