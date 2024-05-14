import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.regex.Matcher;

public class Connection extends Thread {
    private Socket socket;
    final DataInputStream dataInputStream;
    final DataOutputStream dataOutputStream;

    public Connection(Socket socket) throws IOException {
        System.out.println("New connection form: " + socket.getInetAddress() + ":" + socket.getPort());
        this.socket = socket;
        this.dataInputStream = new DataInputStream(socket.getInputStream());
        this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public synchronized void run() {
        super.run();
        try {
            NodeType nodeType;
            while ((nodeType = getIntro()) == NodeType.UNKNOWN) ;
            if (nodeType == NodeType.CLIENT) {
                while (true) {
                    handleClient();
                }
            } else {
                while (handleWorker()) ;
            }
        } catch (IOException e) {
            Worker worker = null;
            for (Worker workers : DataBase.getAllWorkers()) {
                if (workers.getPort() == socket.getPort()) {
                    worker = workers;
                    break;
                }
            }
            if (worker != null) {
                System.out.println("Connection " + socket.getInetAddress() + " : " + socket.getPort() + " with " +
                        worker.getName() + " has been lost!");
                worker.uncordon();
                worker.dissconnect();
                String message = Master.distributeOldTasks(worker.getAllTasks());
                worker.deleteAllTasks();
            }
            else {
                System.out.println("Connection " + socket.getInetAddress() + " : " + socket.getPort() + " has been lost!");
            }
        }
    }

    private NodeType getIntro() throws IOException {
        String intro = dataInputStream.readUTF();
        switch (intro) {
            case "client" -> {
                dataOutputStream.writeUTF("200: You are registered as a client.");
                return NodeType.CLIENT;
            }
            case "worker" -> {
                dataOutputStream.writeUTF("200: You are registered as a worker.");
                return NodeType.WORKER;
            }
            default -> {
                dataOutputStream.writeUTF("400: Invalid intro.");
                return NodeType.UNKNOWN;
            }
        }
    }

    private synchronized boolean handleWorker() throws IOException {
        String input = dataInputStream.readUTF();
        int capacity = Integer.parseInt(input);

        Worker worker = new Worker(capacity, socket.getPort(), socket.getInetAddress());
        String message = Master.distributeAllPendingTasks();
        dataOutputStream.writeUTF("200: " + worker.getName() + " connected..." + "\n" + message);
        return true;
    }

    private synchronized void handleClient() throws IOException {
        String data = dataInputStream.readUTF();

        Packet packet = new Gson().fromJson(data, Packet.class);

        Matcher matcher = packet.getMatcher();
        switch (packet.getCommand()) {
            case CREATE_TASK -> {
                String node = null;
                String name = matcher.group("name");
                if (DataBase.getTaskByName(name) != null) {
                    dataOutputStream.writeUTF("400: Duplicated task name.");
                    return;
                }
                if (matcher.group("node") != null) {
                    node = matcher.group("node");
                    if (DataBase.getWorkerByName(node) == null) {
                        dataOutputStream.writeUTF("400: Invalid worker name.");
                        return;
                    }
                    if (!DataBase.getWorkerByName(node).isConnected()) {
                        dataOutputStream.writeUTF("400: This worker has been disconnected.");
                        return;
                    }
                }
                Task task = new Task(matcher.group("name"));
                if (node != null) {
                    task.setRequestedWorker(DataBase.getWorkerByName(node));
                }
                String message = Master.distributeAllPendingTasks();
                dataOutputStream.writeUTF("200: Task " + task.getName() + " created.\n" + message);
            }
            case GET_NODES -> {
                dataOutputStream.writeUTF(DataBase.getWorkersData());
            }
            case GET_TASKS -> {
                dataOutputStream.writeUTF(DataBase.getTasksData());
            }
            case DELETE_TASK -> {
                String name = matcher.group("name");
                if (DataBase.getTaskByName(name) == null) {
                    dataOutputStream.writeUTF("400: There is no task with this name.");
                    return;
                }

                Task taskToDelete = DataBase.getTaskByName(name);
                if (taskToDelete.getWorker() != null) {
                    taskToDelete.getWorker().removeTask(taskToDelete);
                }
                DataBase.removeTask(taskToDelete);
                String message = Master.distributeTasks();
                dataOutputStream.writeUTF("200: Task removed successfully.\n" + message);
            }
            case CORDON_NODE -> {
                String node = matcher.group("node");
                if (DataBase.getWorkerByName(node) == null) {
                    dataOutputStream.writeUTF("400: Invalid worker name.");
                    return;
                }
                Worker worker = DataBase.getWorkerByName(node);

                if (!worker.isConnected()) {
                    dataOutputStream.writeUTF("400: This worker has been disconnected.");
                    return;
                }
                if (worker.isActive()) {
                    dataOutputStream.writeUTF("400: " + worker.getName() + " is already active.");
                    return;
                }

                worker.cordon();
                String message = Master.distributeAllPendingTasks();

                dataOutputStream.writeUTF("200: " + worker.getName() + " successfully activated.\n" + message);
            }
            case UNCORDON_NODE -> {
                String node = matcher.group("node");
                if (DataBase.getWorkerByName(node) == null) {
                    dataOutputStream.writeUTF("400: Invalid worker name.");
                    return;
                }
                Worker worker = DataBase.getWorkerByName(node);
                if (!worker.isConnected()) {
                    dataOutputStream.writeUTF("400: This worker has been disconnected.");
                    return;
                }
                if (!worker.isActive()) {
                    dataOutputStream.writeUTF("400: " + worker.getName() + " is already deactivated.");
                    return;
                }

                worker.uncordon();
                String message = Master.distributeOldTasks(worker.getAllTasks());
                worker.deleteAllTasks();

                dataOutputStream.writeUTF("200: " + worker.getName() + " successfully deactivated.\n" + message);
            }
            default -> dataOutputStream.writeUTF("400: Invalid command.");
        }
    }
}
