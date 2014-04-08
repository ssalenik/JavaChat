package javachat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class JCFileReceiver {
	File currentFile;
	long currentFileSize;
	private long bytesReceived;
	private boolean receiveInProgress;
	private boolean receiveComplete;
	private FileOutputStream fileOutput;
	
	public JCFileReceiver() {
		receiveInProgress = false;
		receiveComplete = false;
		bytesReceived = -1;
	}
	
	public boolean startFileUpload(File file, long filesize) {
		if(receiveInProgress) {
			return false;
		}
		
		this.currentFile = file;
		
		// create file output stream, do not append, file should be empty
		try {
			fileOutput = new FileOutputStream(currentFile, false);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
			// delete file
			try {
				Files.deleteIfExists(currentFile.toPath());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return false;
		}
		
		// assume everything is OK
		receiveInProgress = true;
		receiveComplete = false;
		currentFileSize = filesize;
		bytesReceived = 0;
		return true;
	}
	
	public void receiveFileChunk(byte[] b) throws IOException {
		if (receiveInProgress) {
			if(bytesReceived + b.length > currentFileSize) {
				throw new IllegalArgumentException("attempted write exceeds expected file size");
			}
			
			fileOutput.write(b);
			bytesReceived += b.length;
			
			// check if we're done
			if(bytesReceived == currentFileSize) {
				receiveInProgress = false;
				receiveComplete = true;
				fileOutput.close();
			}
		} else {
			// upload not started
			return;
		}
	}
	public boolean isReceiveComplete() {
		return receiveComplete;
	}
	
	public boolean isReceiveInProgress() {
		return receiveInProgress;
	}
	
	public String getCurrentFilename() {
		return currentFile.getName();
	}
	
	public long getBytesWritten() {
		return bytesReceived;
	}
	
	public long getCurrentFileSize() {
		return currentFileSize;
	}
	
	public double getReceivedPercent() {
		return (double)bytesReceived/(double)currentFileSize * 100.0;
	}
	
	public void cancelReceive() {
		// only cancel if in progress
		if(receiveInProgress) {
			receiveInProgress = false;
			receiveComplete = false;
			
			// delete current file
			try {
				Files.deleteIfExists(currentFile.toPath());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				fileOutput.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// nothing
			return;
		}
		
	}
}
