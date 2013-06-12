package my.anlights.data.messages;

import my.anlights.data.HueLight;

public class HueReadStateMessage implements HueMessage {

	private HueLight light;

	public HueReadStateMessage(HueLight light) {
		this.light = light;
	}

	public boolean isImportant() {
		return true;
	}

	public HueLight getLight() {
		return light;
	}

}
