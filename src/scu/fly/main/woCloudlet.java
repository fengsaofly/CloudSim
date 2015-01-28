package scu.fly.main;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.Cloudlet;

public class woCloudlet extends Cloudlet implements Cloneable,
		Comparable<woCloudlet> {

	private double waitTime;
	public void setWaitTime(double waitTime) {
		this.waitTime = waitTime;
	}

	private int hostID = -1;
	private myVm vm = null;
	private int vmType = -1;
	private int hostType = -1;
	private double cpuS = -1;
	private double memS = -1;

	public double getCpuS() {
		return cpuS;
	}

	public void setCpuS(double cpuS) {
		this.cpuS = cpuS;
	}

	public double getMemS() {
		return memS;
	}

	public void setMemS(double memS) {
		this.memS = memS;
	}

	public int getHostType() {
		return hostType;
	}

	public void setHostType(int hostType) {
		this.hostType = hostType;
	}

	private int submitTime = -1;

	public int getSubmitTime() {
		return submitTime;
	}

	public void setSubmitTime(int submitTime) {
		this.submitTime = submitTime;
	}

	public myVm getVm() {
		return vm;
	}

	public void setVm(myVm vm) {
		this.vm = vm;
	}

	public int getVmType() {
		return vmType;
	}

	public void setVmType(int vmT) {
		this.vmType = vmT;
	}

	public int getHostID() {
		return hostID;
	}

	public void setHostID(int hostID) {
		this.hostID = hostID;
	}

	public woCloudlet(int cloudletId, long cloudletLength, int pesNumber,
			long cloudletFileSize, long cloudletOutputSize,
			UtilizationModel utilizationModelCpu,
			UtilizationModel utilizationModelRam,
			UtilizationModel utilizationModelBw) {
		super(cloudletId, cloudletLength, pesNumber, cloudletFileSize,
				cloudletOutputSize, utilizationModelCpu, utilizationModelRam,
				utilizationModelBw);

		waitTime = 0;
	}

	public double getWaitTime() {
		return waitTime;
	}

	@Override
	public woCloudlet clone() throws CloneNotSupportedException {

		woCloudlet cloudlet = new woCloudlet(getCloudletId(),
				getCloudletLength(), getNumberOfPes(), getCloudletFileSize(),
				getCloudletOutputSize(), getUtilizationModelCpu(),
				getUtilizationModelRam(), getUtilizationModelBw());

		cloudlet.setUserId(this.getUserId());
		return cloudlet;

	}

	@Override
	public int compareTo(woCloudlet o) {
		if (getHostID() < o.getHostID()) {
			return -1;
		} else if (getHostID() == o.getHostID()) {
			if (getFinishTime() < o.getFinishTime()) {
				return -1;
			} else if (getFinishTime() == o.getFinishTime()) {
				if (getExecStartTime() < o.getExecStartTime()) {
					return -1;
				}
				return 1;
			}
			return 1;

		}
		return 1;

	}

}
