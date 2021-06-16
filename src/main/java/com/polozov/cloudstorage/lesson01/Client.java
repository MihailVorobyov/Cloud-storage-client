package com.polozov.cloudstorage.lesson01;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;

public class Client extends JFrame {
	private final Socket socket;
	private final DataOutputStream out;
	private final DataInputStream in;

	public Client() throws IOException {
		socket = new Socket("localhost", 5678);
		out = new DataOutputStream(socket.getOutputStream());
		in = new DataInputStream(socket.getInputStream());

		setSize(300, 300);
		JPanel panel = new JPanel(new GridLayout(2, 1));

		JButton btnSend = new JButton("SEND");
		JTextField textField = new JTextField();

		btnSend.addActionListener(a -> {
			// upload 1.txt
			// download img.png
			String[] cmd = textField.getText().split(" ", 2); // limit: 2 добавлено для корректной работы с файлами,
																		// содержащими пробелы в названии
			if ("upload".equals(cmd[0])) {
				sendFile(cmd[1]);
			} else if ("download".equals(cmd[0])) {
				getFile(cmd[1]);
			}
		});

		panel.add(textField);
		panel.add(btnSend);

		add(panel);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				sendMessage("exit");
			}
		});

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}

	private void getFile(String filename) {
		// TODO: 14.06.2021
		try {
			out.writeUTF("download");
			out.writeUTF(filename);
			if ("File not found".equals(in.readUTF())) {
				throw new FileNotFoundException("File not found");
			}

			File file = new File("client" + File.separator + in.readUTF());
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

			System.out.println("Downloading status: " + status);
			out.writeUTF(status);
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendFile(String filename) {
		try {
			File file = new File("client" + File.separator + filename);
			if (!file.exists()) {
				throw  new FileNotFoundException();
			}

			long fileLength = file.length();
			System.out.println("File size: " + fileLength);	// для дебага
			FileInputStream fis = new FileInputStream(file);

			out.writeUTF("upload");
			out.writeUTF(filename);
			out.writeLong(fileLength);

			int read = 0;
			byte[] buffer = new byte[8 * 1024];
			while ((read = fis.read(buffer)) != -1) {
				out.write(buffer, 0, read);
				System.out.println("read = " + read); 	// для дебага
			}

			out.flush();

			String status = in.readUTF();
			System.out.println("sending status: " + status);
			fis.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendMessage(String message) {
		try {
			out.writeUTF(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		new Client();
	}
}
