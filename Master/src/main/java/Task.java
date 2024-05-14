public class Task {
    private final String name;
    private TaskStatus status;
    private Worker requestedWorker;
    private Worker worker;

    public Task(String name) {
        this.name = name;
        this.status = TaskStatus.PENDING;
        DataBase.addTask(this);
        DataBase.addPendingTask(this);
    }

    public void setRequestedWorker(Worker requestedWorker) {
        this.requestedWorker = requestedWorker;
    }

    public Worker getRequestedWorker() {
        return requestedWorker;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public String getName() {
        return name;
    }

    public TaskStatus getStatus() {
        return status;
    }
}
