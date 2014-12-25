package scu.fly.main;
import java.io.IOException;

import org.cloudbus.cloudsim.examples.power.planetlab.Dvfs;
import org.cloudbus.cloudsim.examples.power.planetlab.PlanetLabRunner;


public class FlyTest {


		/**
		 * The main method.
		 * 
		 * @param args the arguments
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static void main(String[] args) throws IOException {
			boolean enableOutput = true;
			boolean outputToFile = false;
			String inputFolder = Dvfs.class.getClassLoader().getResource("workload/planetlab").getPath();
			String outputFolder = "output";
			String workload = "20110303"; // PlanetLab workload
			String vmAllocationPolicy = "fly"; // fly policy without VM migrations
			String vmSelectionPolicy = "";
			String parameter = "";

			new PlanetLabRunner(
					enableOutput,
					outputToFile,
					inputFolder,
					outputFolder,
					workload,
					vmAllocationPolicy,
					vmSelectionPolicy,
					parameter);
		}

}


