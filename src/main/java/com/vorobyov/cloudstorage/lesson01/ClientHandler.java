package com.vorobyov.cloudstorage.lesson01;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
	private final Socket socket;

	public ClientHandler(Socket socket) {
		this.socket = socket;
	}


	@Override
	public void run() {
		try (
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				DataInputStream in = new DataInputStream(socket.getInputStream())
		) {
			System.out.printf("Client %s connected\n", socket.getInetAddress());

			while (true) {
				String command = in.readUTF();
				System.out.println(command);

				if ("upload".equals(command)) {
					try {
						File file = new File("server"  + File.separator + in.readUTF());
						if (!file.exists()) {
							 file.createNewFile();
						}

						FileOutputStream fos = new FileOutputStream(file);

						long size = in.readLong();
						byte[] buffer = new byte[8 * 1024];

						long remainingSize = size;

						while (remainingSize != 0) {
							int read = in.read(buffer);
							remainingSize -= read;
							fos.write(buffer, 0, read);
						}

						fos.close();

						String status = null;
						if (size == file.length()) {
							status = "OK";
						} else {
							status = "Error. File was corrupted";
						}

						System.out.println("Upload status: " + status);
						out.writeUTF(status);
					} catch (Exception e) {
						//out.writeUTF("FATAL ERROR");
					}
				}

				if ("download".equals(command)) {
					try {
						File file = new File("server" + File.separator + in.readUTF());
						if (!file.exists()) {
							out.writeUTF("File not found");
						} else {
							out.writeUTF("File OK");
						}

						long fileLength = file.length();
						FileInputStream fis = new FileInputStream(file);

						out.writeUTF(file.getName());
						out.writeLong(fileLength);

						int read = 0;
						byte[] buffer = new byte[8 * 1024];
						while ((read = fis.read(buffer)) != -1) {
							out.write(buffer, 0, read);
						}
						out.flush();
						fis.close();

						String status = in.readUTF();
						System.out.println("Downloading status: " + status);

					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if ("exit".equals(command)) {
					System.out.printf("Client %s disconnected correctly\n", socket.getInetAddress());
					break;
				}


			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
