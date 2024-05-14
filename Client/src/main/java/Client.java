import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;

public class Client {
    private NodeType nodeType;
    final DataInputStream dataInputStream;
    final DataOutputStream dataOutputStream;

    {
        nodeType = NodeType.UNKNOWN;
    }

    public Client(String host, int port) throws IOException {
        System.out.println("Starting Client service...");
        System.out.println("please specify your client type: [worker/client]");
        Scanner scanner = new Scanner(System.in);

        while (nodeType == NodeType.UNKNOWN) {
            String input = scanner.nextLine();
            if (input.equals("worker"))
                nodeType = NodeType.WORKER;
            else if (input.equals("client"))
                nodeType = NodeType.CLIENT;
            else
                System.out.println("Invalid client type.");
        }

        System.out.println(nodeType + " type was set for your client.");

        Socket socket = new Socket(host, port);
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());

        if (nodeType == NodeType.CLIENT) {
            dataOutputStream.writeUTF("client");
            System.out.println(dataInputStream.readUTF());
            System.out.println("""
                    Command list:
                    create task --name=task1 (--node=worker1)?
                    get tasks
                    get nodes
                    delete task --name=task1
                    cordon node <node>
                    uncordon node <node>""");
            while (true)
                handleClient(scanner);
        } else {
            dataOutputStream.writeUTF("worker");
            handleWorker(scanner, socket);
            while (true) {
                System.out.println(dataInputStream.readUTF());
            }
        }

    }

    private void handleWorker(Scanner scanner, Socket socket) throws IOException {
        System.out.println("Please enter worker capacity:");
        int number = scanner.nextInt();

        dataOutputStream.writeUTF(String.valueOf(number));
        System.out.println(dataInputStream.readUTF());
    }

    private synchronized void handleClient(Scanner scanner) throws IOException {
        Packet packet;
        Matcher matcher;
        String input = scanner.nextLine();
        if ((matcher = Command.getMatcher(input, Command.CREATE_TASK)) != null) {
            packet = new Packet(Command.CREATE_TASK, input);
        } else if ((matcher = Command.getMatcher(input, Command.GET_NODES)) != null) {
            packet = new Packet(Command.GET_NODES, input);
        } else if ((matcher = Command.getMatcher(input, Command.GET_TASKS)) != null) {
            packet = new Packet(Command.GET_TASKS, input);
        } else if ((matcher = Command.getMatcher(input, Command.DELETE_TASK)) != null) {
            packet = new Packet(Command.DELETE_TASK, input);
        } else if ((matcher = Command.getMatcher(input, Command.UNCORDON_NODE)) != null) {
            packet = new Packet(Command.UNCORDON_NODE, input);
        } else if ((matcher = Command.getMatcher(input, Command.CORDON_NODE)) != null) {
            packet = new Packet(Command.CORDON_NODE, input);
        } else {
            System.out.println("Wrong command!");
            return;
        }
        String convertedPacket = new Gson().toJson(packet);
        dataOutputStream.writeUTF(convertedPacket);
        System.out.println(dataInputStream.readUTF());
    }
}
