package my.anlights.data;

import org.json.JSONObject;

public class HueError {

	private int type;
	private String address;
	private String desc;

	// see http://developers.meethue.com/8_errormessages.html
	public static int ERROR_UNAUTHORIZED = 1;
	public static int ERROR_BUTTON_NOT_PRESSED = 101;


	public HueError(int type, String address, String desc) {
		this.type = type;
		this.address = address;
		this.desc = desc;
	}

	public int getType() {
		return type;
	}

	public String getAddress() {
		return address;
	}

	public String getDesc() {
		return desc;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("HueError[");
		sb.append("type:").append(type);
		sb.append(", address:").append(address);
		sb.append(", description:").append(desc);
		sb.append("]");
		return sb.toString();
	}
}
