package at.sparklingscience.urbantrees.domain;

import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;
import at.sparklingscience.urbantrees.domain.validator.annotation.DateRange;
import at.sparklingscience.urbantrees.domain.validator.annotation.DateRange.Range;

/**
 * Describes settings of a {@link Beacon}.
 * 
 * @author Laurenz Fiala
 * @since 2018/06/05
 */
public class BeaconSettings {

	/**
	 * The settings' database identifier.
	 */
	@Min(value = 1, groups = {ValidationGroups.Read.class})
	private long id;
	
	/**
	 * ID of associated {@link Beacon}.
	 */
	@Min(value = 1, groups = {ValidationGroups.Read.class})
	private int beaconId;
	
	/**
	 * Device name.
	 */
	private String deviceName = "";
	
	/**
	 * Version id of the current beacons firmware.
	 */
	private int firmwareVersionCode;
	
	/**
	 * Beacon bluetooth transmission strength mode.
	 */
	private double transmitPowerDb;
	
	/**
	 * Battery level in percent from 0-100.
	 */
	private int batteryLevel;
	
	/**
	 * Temperature units used by the beacon.
	 * Either C or F.
	 */
	private String temperatureUnits = "";
	
	/**
	 * Amount of logs which can be stored
	 * per logging metric.
	 */
	@Min(0)
	private int memoryCapacity;
	
	/**
	 * Reference date set on the device.
	 */
	private Date refTime;
	
	/**
	 * Extra device ID of the device.
	 */
	private int deviceId;
	
	/**
	 * Whether the physical button is enabled or not.
	 */
	private boolean physicalButtonEnabled;
	
	/**
	 * Relative temperature offset.
	 */
	private double temperatureCalibration;
	
	/**
	 * Relative humidity offset.
	 */
	private double humidityCalibration;
	
	/**
	 * Logging interval in seconds.
	 */
	private int loggingIntervalSec;
	
	/**
	 * Sensor capturing interval in seconds.
	 */
	private int sensorIntervalSec;
	
	/**
	 * Advertising frequency in milliseconds.
	 */
	private int advertisingFrequencyMs;
	
	/**
	 * PIN set on the device.
	 */
	private int pin;
	
	/**
	 * Date of settings check.
	 */
	@NotNull
	@DateRange(Range.PAST_AND_PRESENT)
	private Date checkDate = new Date();
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getBeaconId() {
		return beaconId;
	}

	public void setBeaconId(int beaconId) {
		this.beaconId = beaconId;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public int getFirmwareVersionCode() {
		return firmwareVersionCode;
	}

	public void setFirmwareVersionCode(int firmwareVersionCode) {
		this.firmwareVersionCode = firmwareVersionCode;
	}

	public double getTransmitPowerDb() {
		return transmitPowerDb;
	}

	public void setTransmitPowerDb(double transmitPowerDb) {
		this.transmitPowerDb = transmitPowerDb;
	}

	public int getBatteryLevel() {
		return batteryLevel;
	}

	public void setBatteryLevel(int batteryLevel) {
		this.batteryLevel = batteryLevel;
	}

	public String getTemperatureUnits() {
		return temperatureUnits;
	}

	public void setTemperatureUnits(String temperatureUnits) {
		this.temperatureUnits = temperatureUnits;
	}

	public int getMemoryCapacity() {
		return memoryCapacity;
	}

	public void setMemoryCapacity(int memoryCapacity) {
		this.memoryCapacity = memoryCapacity;
	}

	public Date getRefTime() {
		return refTime;
	}

	public void setRefTime(Date refTime) {
		this.refTime = refTime;
	}

	public int getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}

	public boolean isPhysicalButtonEnabled() {
		return physicalButtonEnabled;
	}

	public void setPhysicalButtonEnabled(boolean physicalButtonEnabled) {
		this.physicalButtonEnabled = physicalButtonEnabled;
	}

	public double getTemperatureCalibration() {
		return temperatureCalibration;
	}

	public void setTemperatureCalibration(double temperatureCalibration) {
		this.temperatureCalibration = temperatureCalibration;
	}

	public double getHumidityCalibration() {
		return humidityCalibration;
	}

	public void setHumidityCalibration(double humidityCalibration) {
		this.humidityCalibration = humidityCalibration;
	}

	public int getLoggingIntervalSec() {
		return loggingIntervalSec;
	}

	public void setLoggingIntervalSec(int loggingIntervalSec) {
		this.loggingIntervalSec = loggingIntervalSec;
	}

	public int getSensorIntervalSec() {
		return sensorIntervalSec;
	}

	public void setSensorIntervalSec(int sensorIntervalSec) {
		this.sensorIntervalSec = sensorIntervalSec;
	}

	public int getAdvertisingFrequencyMs() {
		return advertisingFrequencyMs;
	}

	public void setAdvertisingFrequencyMs(int advertisingFrequencyMs) {
		this.advertisingFrequencyMs = advertisingFrequencyMs;
	}

	public int getPin() {
		return pin;
	}

	public void setPin(int pin) {
		this.pin = pin;
	}

	public Date getCheckDate() {
		return checkDate;
	}

	public void setCheckDate(Date checkDate) {
		this.checkDate = checkDate;
	}

}
