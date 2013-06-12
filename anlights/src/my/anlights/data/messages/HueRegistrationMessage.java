package my.anlights.data.messages;

/**
 * Created by Andreas on 09.06.13.
 */
public class HueRegistrationMessage implements HueMessage {

	private String deviceType;
	private String userName;

	public HueRegistrationMessage(String deviceType, String userName){
		this.deviceType = deviceType;
		this.userName = userName;
	}

	@Override
	public boolean isImportant() {
		return true;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public String getUserName() {
		return userName;
	}
}
