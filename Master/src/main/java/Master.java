import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class Master {
    public Master(int port) {
        System.out.println("Starting Master service...");

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                Connection connection = new Connection(socket);
                DataBase.addConnection(connection);
                connection.start();
            }
        } catch (IOException e) {

        }
    }

    public static String distributeTasks() {
        Task task = DataBase.getPendingTasks().peek();
        if (task == null)
            return "400: No pending task to schedule.";
        else {
            if (task.getRequestedWorker() != null) {
                if (task.getRequestedWorker().isFull())
                    return "400: Requested worker is full.";
                else {
                    task.getRequestedWorker().addTask(task);
                    return "200: Task " + task.getName() +
                            " successfully scheduled on worker " +
                            task.getRequestedWorker().getName() + ".";
                }
            } else {
                Worker worker = findBestWorker(task);
                if (worker == null)
                    return "400: All workers are full.";
                else {
                    worker.addTask(task);
                    return "200: Task " + task.getName() +
                            " successfully scheduled on worker " +
                            worker.getName() + ".";
                }
            }
        }
    }

    public static Worker findBestWorker(Task task) {
        double bestFreeSpace = Double.MIN_VALUE;
        Worker bestWorker = null;
        for (Worker worker : DataBase.getAllWorkers()) {
            double freeSpace = (worker.getMAX_TASK_NUMBER() - worker.getAllTasks().size()) / (double) worker.getMAX_TASK_NUMBER();
            if (!worker.isFull() && worker.isActive() && worker.isConnected()
                    && freeSpace > bestFreeSpace &&
                    (task.getRequestedWorker() == null || task.getRequestedWorker().equals(worker))) {
                bestFreeSpace = freeSpace;
                bestWorker = worker;
            }
        }

        return bestWorker;
    }

    public static String distributeOldTasks(ArrayList<Task> uncordonTasks) {
        StringBuilder output = new StringBuilder();
        for (Task task : uncordonTasks) {
            Worker worker = findBestWorker(task);
            if (worker != null) {
                worker.addTask(task);
                output.append("200: Task ").append(task.getName()).
                        append(" successfully scheduled on worker ").
                        append(worker.getName()).append(".\n");
            }
        }
        if (!output.isEmpty())
            output.replace(output.length() - 1, output.length(), "");
        return output.toString();
    }

    public static String distributeAllPendingTasks() {
        StringBuilder output = new StringBuilder();
        Queue<Task> pendingTasks = DataBase.getPendingTasks();
        Queue<Task> stillPendingTasks = new LinkedList<>();
        Task task;
        while (!pendingTasks.isEmpty()) {
            task = pendingTasks.poll();
            Worker worker = findBestWorker(task);
            if (worker != null) {
                worker.addTask(task);
                output.append("200: Task ").append(task.getName()).
                        append(" successfully scheduled on worker ").
                        append(worker.getName()).append(".\n");
            }
            else {
                stillPendingTasks.add(task);
            }
        }

        DataBase.setPendingTasks(stillPendingTasks);

        if (!output.isEmpty())
            output.replace(output.length() - 1, output.length(), "");
        return output.toString();
    }
}
