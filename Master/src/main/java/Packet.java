import java.util.regex.Matcher;

public class Packet {
    private Command command;
    private String input;

    public Packet(Command command, String input) {
        this.command = command;
        this.input = input;
    }

    public Command getCommand() {
        return command;
    }

    public Matcher getMatcher() {
        return Command.getMatcher(input, command);
    }
}
