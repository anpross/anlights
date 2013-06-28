package my.anlights.data.messages;

/**
 * Created by user on 6/27/13.
 */
public class HueDeleteUserMessage implements HueMessage {

    private String user;

    @Override
    public boolean isImportant() {
        return true;
    }

    public HueDeleteUserMessage(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }
}
