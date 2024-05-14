import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

public class DataBase {
    private static ArrayList<Worker> allWorkers;
    private static ArrayList<Task> allTasks;
    private static ArrayList<Connection> allConnections;
    private static Queue<Task> pendingTasks;

    static {
        allWorkers = new ArrayList<>();
        allTasks = new ArrayList<>();
        allConnections = new ArrayList<>();
        pendingTasks = new LinkedList<>();
    }

    public static void addTask(Task task) {
        allTasks.add(task);
    }
    public static void addPendingTask(Task task) {
        pendingTasks.add(task);
    }
    public static void removeTask(Task task) {
        allTasks.remove(task);
        pendingTasks.remove(task);
    }
    public static void addConnection(Connection Connection) {
        allConnections.add(Connection);
    }
    public static void addWorker(Worker Worker) {
        allWorkers.add(Worker);
    }

    public static ArrayList<Worker> getAllWorkers() {
        return allWorkers;
    }

    public static ArrayList<Task> getAllTasks() {
        return allTasks;
    }

    public static ArrayList<Connection> getAllConnections() {
        return allConnections;
    }

    public static Queue<Task> getPendingTasks() {
        return pendingTasks;
    }

    public static void setPendingTasks(Queue<Task> pendingTasks) {
        DataBase.pendingTasks = pendingTasks;
    }

    public static Worker getWorkerByName(String name) {
        for (Worker worker : allWorkers) {
            if (name.equals(worker.getName()))
                return worker;
        }
        return null;
    }

    public static Task getTaskByName(String name) {
        for (Task task : allTasks) {
            if (name.equals(task.getName()))
                return task;
        }
        return null;
    }

    public static String getWorkersData() {
        StringBuilder output = new StringBuilder();

        if (allWorkers.isEmpty()) {
            return "There is no worker available.";
        }

        output.append("      Id      ")
                .append("     Capacity     ")
                .append("    Free space    ")
                .append("   Port    ")
                .append("    IP   ")
                .append("   Activated   ")
                .append("   Connected")
                .append("\n");

        for (Worker worker : allWorkers) {
            double freeSpace = (worker.getMAX_TASK_NUMBER() - worker.getAllTasks().size())
                    /
                    (double) worker.getMAX_TASK_NUMBER();
            String freeString = String.format("%.2f", freeSpace * 100);
            output.append("    ").append(worker.getName()).append("            ")
                    .append(worker.getMAX_TASK_NUMBER())
                    .append("             ").append(freeString).append("%")
                    .append("         ").append(worker.getPort())
                    .append("    ").append(worker.getIp())
                    .append("   ").append(worker.isActive())
                    .append("          ").append(worker.isConnected())
                    .append("\n")
            ;
        }
        output.replace(output.length() - 1, output.length(),"");
        return output.toString();
    }

    public static String getTasksData() {
        StringBuilder output = new StringBuilder();

        if (allTasks.isEmpty()) {
            return "There is no task available.";
        }
        output.append("     Name        ").append("Status     ").append("    Worker    ").append("").append("\n");

        for (Task task : allTasks) {
            output.append("    ").append(task.getName()).append("      ")
                    .append(task.getStatus()).append("      ");
            if (task.getWorker() != null) {
                    output.append(task.getWorker().getName());
            }
            output.append("\n");
        }
        output.replace(output.length() - 1, output.length(),"");
        return output.toString();
    }
}
