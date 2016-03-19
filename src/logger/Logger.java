package logger;

import main.ThreadHandler;

public class Logger implements Runnable{

	ThreadHandler threadHandler;
	
	public void setThreadHandler(ThreadHandler threadHandler){
		this.threadHandler = threadHandler;
	}
	
	@Override
	public void run() {
		String logEntry = null;
		while(!threadHandler.getThreadKillMessage()){
			logEntry = threadHandler.getLogEntry();
			try {
				threadHandler.getLogFile().write(logEntry);
				threadHandler.getLogFile().newLine();
				threadHandler.getLogFile().flush();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

}
