import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Command {

    CREATE_TASK("^\\s*create\\s+task\\s+--name=(?<name>\\S+)\\s*(--node=(?<node>\\S+))?\\s*$"),
    GET_TASKS("^\\s*get\\s+tasks\\s*$"),
    GET_NODES("^\\s*get\\s+nodes\\s*$"),
    DELETE_TASK("^\\s*delete\\s+task\\s+--name=(?<name>\\S+)\\s*$"),
    UNCORDON_NODE("^\\s*uncordon\\s+node\\s+(?<node>\\S+)\\s*$"),
    CORDON_NODE("^\\s*cordon\\s+node\\s+(?<node>\\S+)\\s*$")
    ;

    public static Matcher getMatcher(String input , Command command) {
        Matcher matcher = Pattern.compile(command.regex).matcher(input);
        return matcher.matches() ? matcher : null;
    }
    String regex;

    Command(String regex) {
        this.regex = regex;
    }
}
