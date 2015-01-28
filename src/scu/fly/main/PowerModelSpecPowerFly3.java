package scu.fly.main;

import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPower;

public class PowerModelSpecPowerFly3 extends PowerModelSpecPower implements
		PowerModel {
	private double factor = 16.72;
	private double contant = 86.495;
	@Override
	public double getPower(double utilization) throws IllegalArgumentException {
		if (utilization < 0 || utilization > 1) {
			throw new IllegalArgumentException("Utilization value must be between 0 and 1");
		}
		
		return utilization*factor+contant;
	}
	@Override
	protected double getPowerData(int index) {
		// TODO Auto-generated method stub
		return 0;
	}

}
