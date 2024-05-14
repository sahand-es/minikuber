import java.net.InetAddress;
import java.util.ArrayList;

public class Worker {

    private int port;
    private InetAddress ip;
    private int MAX_TASK_NUMBER;
    private String name;
    private static int workersCount = 1;
    private ArrayList<Task> allTasks;
    private boolean isActive;
    private boolean isConnected;

    {
        allTasks = new ArrayList<>();
    }

    public Worker(int MAX_TASK_NUMBER, int port, InetAddress ip) {
        this.port = port;
        this.ip = ip;
        this.MAX_TASK_NUMBER = MAX_TASK_NUMBER;
        this.name = "worker" + workersCount;
        this.isActive = true;
        isConnected = true;
        DataBase.addWorker(this);
        workersCount++;
    }

    public void uncordon() {
        isActive = false;
        for (Task task : allTasks) {
            task.setStatus(TaskStatus.PENDING);
            DataBase.addPendingTask(task);
            task.setWorker(null);
        }
    }

    public void cordon() {
        isActive = true;
    }

    public void dissconnect() {
        isConnected = false;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void addTask(Task task) {
        task.setWorker(this);
        task.setStatus(TaskStatus.SCHEDULED);
        task.setRequestedWorker(null);
        DataBase.getPendingTasks().remove(task);
        allTasks.add(task);
    }

    public void removeTask(Task task) {
        allTasks.remove(task);
    }

    public ArrayList<Task> getAllTasks() {
        return allTasks;
    }

    public void deleteAllTasks() {
        allTasks = new ArrayList<>();
    }

    public int getMAX_TASK_NUMBER() {
        return MAX_TASK_NUMBER;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public InetAddress getIp() {
        return ip;
    }

    public static int getWorkersCount() {
        return workersCount;
    }

    public boolean isFull() {
        return MAX_TASK_NUMBER == allTasks.size();
    }

    public boolean isActive() {
        return isActive;
    }
}
