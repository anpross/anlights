package my.anlights.data.messages;

/**
 * Created by Andreas on 09.06.13.
 */

import my.anlights.data.HueLight;

public class HueReadConfigMessage implements HueMessage {

	private HueLight light;

	public HueReadConfigMessage() {
	}

	public boolean isImportant() {
		return true;
	}
}
