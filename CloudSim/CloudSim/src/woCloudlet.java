
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.myCloudlet;


public class woCloudlet extends myCloudlet {
	
	private double waitTime;
	private double submitTime;
	
	public woCloudlet(
			int cloudletId, 
			long cloudletLength, 
			int pesNumber, 
			long cloudletFileSize, 
			long cloudletOutputSize,
			UtilizationModel utilizationModelCpu,
			UtilizationModel utilizationModelRam, 
			UtilizationModel utilizationModelBw){
		 super(
					cloudletId,
					cloudletLength,
					pesNumber,
					cloudletFileSize,
					cloudletOutputSize,
					utilizationModelCpu,
					utilizationModelRam,
					utilizationModelBw);
		
		waitTime = 0;
	}
	
	public double getWaitTime(){
		return waitTime;
	}


}
