package scu.fly.main;

import org.cloudbus.cloudsim.power.models.PowerModelSpecPower;

public class PowerModelSpecPowerFlyOne extends PowerModelSpecPower {

	private final double[] power = {95.895 ,108.005, 120.115 ,132.225 ,144.335, 156.445,168.555 ,180.665, 192.775,204.885 ,216.995};
	@Override
	protected double getPowerData(int index) {
		// TODO Auto-generated method stub
		return power[index];
	}
	

}
