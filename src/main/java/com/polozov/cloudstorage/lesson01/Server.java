package com.polozov.cloudstorage.lesson01;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
	// TODO: 14.06.2021
	// организовать корректный вывод статуса - выполнено
	// подумать почему так реализован цикл в ClientHandler
	/*
	Ответ: мы передаём данные из буфера в сеть, поэтому нам нужно количество буферов ("пакетов"), которое и
	получается при делении (size + (buffer.length - 1)) / (buffer.length)
	К сожалению я не догадался, как аккуратно поправить цикл, поэтому написал новый
	 */

	public Server() {
		ExecutorService service = Executors.newFixedThreadPool(4);
		try (ServerSocket server = new ServerSocket(5678)){
			System.out.println("Server started");
			while (true) {
				service.execute(new ClientHandler(server.accept()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Server();
	}
}
