package my.anlights.data.messages;

/**
 * Created by Andreas on 09.06.13.
 */

public class HueReadConfigMessage implements HueMessage {

	public HueReadConfigMessage() {
	}

	public boolean isImportant() {
		return true;
	}
}
