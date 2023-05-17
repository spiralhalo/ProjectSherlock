//
//    Copyright 2020 spiralhalo <re.nanashi95@gmail.com>
//
//    This file is part of Project Sherlock.
//
//    Project Sherlock is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Project Sherlock is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Project Sherlock.  If not, see <https://www.gnu.org/licenses/>.
//

package xyz.spiralhalo.sherlock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import xyz.spiralhalo.sherlock.persist.settings.AppConfig;

public class WebServer {

	public static final int SUCCESS = 0;
	public static final int ALREADY_RUNNING = 1;
	public static final int FAILED = 2;
	private static ServerSocket serverSocket;
	private static Thread serverThread;
	private static boolean rareFailure = false;
	private static final int MAX_ATTEMPT = 64000;
	private static int clientCount = 0;

	public static int start() {
		if (serverSocket != null) {
			Debug.log(new Error("Server socket is already running!"));
			return FAILED;
		}
		if (rareFailure) {
			Debug.log(new Error("Can't reserve a port!"));
			return FAILED;
		}
		int port = AppConfig.getSocketPort();
		int attempt = 0;
		while (attempt < MAX_ATTEMPT) {
			// debug
			long time = System.nanoTime();
			try {
				serverSocket = new ServerSocket(port);
				break;
			} catch (IOException e) {
				if (askIsRunning(port)) {
					return ALREADY_RUNNING;
				}
				Debug.log(e);
			} catch (IllegalArgumentException e) {
				if (port > 65535) {
					port = 1024;
				}
			}
			attempt++;
			System.out.println("time used for 1 attempt (nanosecond):" + (System.nanoTime() - time));
		}
		if (attempt >= MAX_ATTEMPT) {
			rareFailure = true;
			Debug.log(new Error("Can't use any ports to open a server socket! (This should never happen unless you're running ProjectSherlock in a web server)"));
		}
		if (serverSocket != null) {
			if (port != AppConfig.getSocketPort()) {
				AppConfig.setSocketPort(port);
			}
			serverThread = new Thread(serverTask);
			serverThread.start();
			return SUCCESS;
		}
		return FAILED;
	}

	private static boolean askIsRunning(int port) {
		try (Socket askSocket = new Socket("localhost", port);
			 PrintWriter out = new PrintWriter(askSocket.getOutputStream(), true);
			 BufferedReader in = new BufferedReader(new InputStreamReader(askSocket.getInputStream()))) {
			if (in.readLine().equalsIgnoreCase(SHERLOCK_HELLO)) {
				out.println(SHERLOCK_Q_IS_RUNNING);
				if (in.readLine().equalsIgnoreCase(SHERLOCK_A_YES)) {
					out.println(SHERLOCK_BYE);
					return true;
				}
			}
		} catch (IOException e) {
			Debug.log(e);
		}
		return false;
	}

	private static Runnable serverTask = new Runnable() {
		@Override
		public void run() {
			while (serverSocket != null) {
				try {
					Socket clientSocket = serverSocket.accept();
					ClientThread clientThread = new ClientThread(clientSocket);
					clientThread.start();
				} catch (IOException e) {
					Debug.log(e);
				}
			}
		}
	};

	private static class ClientThread {
		private Socket socket;

		ClientThread(Socket clientSocket) {
			socket = clientSocket;
		}

		public void start() {
			new Thread(this::clientServerTask).start();
		}

		private void clientServerTask() {
			clientCount++;
			try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
				out.println(SHERLOCK_HELLO);
				String clientInput;
				while ((clientInput = in.readLine()) != null) {
					String output = processInput(clientInput);
					if (output == null) {
						break;
					}
					out.println(output);
					if (clientInput.equalsIgnoreCase(SHERLOCK_BYE)) {
						break;
					}
				}
				socket.close();
			} catch (IOException e) {
				Debug.log(e);
			}
			clientCount--;
		}

		private String processInput(String input) {
			if (input.equalsIgnoreCase(SHERLOCK_Q_IS_RUNNING)) {
				return SHERLOCK_A_YES;
			}
			if (input.equalsIgnoreCase(SHERLOCK_BYE)) {
				return SHERLOCK_BYE;
			}
			return null;
		}
	}

	private static final String SHERLOCK_HELLO = "VASAAQ";
	private static final String SHERLOCK_Q_IS_RUNNING = "HOW ARE YOU?";
	private static final String SHERLOCK_A_YES = "I'M DOING WELL";
	private static final String SHERLOCK_BYE = "SAV'ORQ";
}
