package my.anlights.data.messages;

import my.anlights.data.HueLight;
import my.anlights.data.HueState;

public class HueWriteStateMessage implements HueMessage {


	private HueState state;
	private HueLight light;
	private boolean important;
	private int action;

	public HueWriteStateMessage(HueLight light, HueState state, boolean important) {

		this.light = light;
		this.state = state;
		this.important = important;
	}

	public HueWriteStateMessage(HueLight light, HueState state) {
		this.light = light;
		this.state = state;
		this.important = true;
	}

	public HueState getState() {
		return state;
	}

	public void setState(HueState state) {
		this.state = state;
	}

	public boolean isImportant() {
		return important;
	}

	public void setImportant(boolean important) {
		this.important = important;
	}

	public HueLight getLight() {
		return light;
	}

	public void setLight(HueLight light) {
		this.light = light;
	}

}
