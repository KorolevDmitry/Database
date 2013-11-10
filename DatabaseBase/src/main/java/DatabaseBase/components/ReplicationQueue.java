package DatabaseBase.components;

import DatabaseBase.commands.service.ReplicateCommand;
import DatabaseBase.entities.EvaluationResult;
import DatabaseBase.entities.Query;
import DatabaseBase.exceptions.ConnectionException;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/31/13
 * Time: 8:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReplicationQueue implements Runnable {
    ConcurrentLinkedQueue<ReplicateCommand> _queue;
    TcpSender _sender;
    Thread _thread;
    boolean _isStopRequested;
    boolean _isWorking;
    int _timeout = 100;

    public ReplicationQueue(TcpSender sender) {
        _sender = sender;
        _queue = new ConcurrentLinkedQueue<ReplicateCommand>();
        _isWorking = true;
        _thread = new Thread(this);
        _thread.start();
    }

    public void AddToExecution(ReplicateCommand command) {
        _queue.add(command);
    }

    public void ExecuteSync(ReplicateCommand command) {
        Execute(command);
    }

    private void Execute(ReplicateCommand command) {
        try {
            Query query = new Query();
            query.Command = command;
            EvaluationResult evaluationResult = _sender.Send(query, command.ToRoute);
        } catch (ConnectionException e) {
        }
    }

    @Override
    public void run() {
        while (!_isStopRequested) {
            if (!_queue.isEmpty()) {
                Execute(_queue.peek());
                _queue.poll();
            } else {
                try {
                    Thread.sleep(_timeout);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
        _isWorking = false;
    }

    public void Stop() {
        _isStopRequested = true;
        while (_isWorking) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}