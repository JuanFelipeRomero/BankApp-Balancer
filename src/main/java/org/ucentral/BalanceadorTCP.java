package org.ucentral;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BalanceadorTCP {
    private static final int BALANCEADOR_PORT = 6000;
    private final AtomicInteger indiceServidor = new AtomicInteger(0);

    public static void main(String[] args) {
        BalanceadorTCP balanceador = new BalanceadorTCP();
        balanceador.iniciarMonitorStatus();
        balanceador.iniciar();
    }

    public void iniciarMonitorStatus() {
        Thread monitorStatusThread = new Thread(() -> {
            while (true) {
                try {
                    Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                    MonitorInterface monitor = (MonitorInterface) registry.lookup("MonitorService");
                    List<ServidorInfo> servidores = monitor.obtenerEstadoServidores();
                    System.out.println("=== Estado actual de los servidores ===");
                    for (ServidorInfo s : servidores) {
                        System.out.println(s);
                    }
                    System.out.println("=========================================");
                    Thread.sleep(10000);
                } catch (Exception e) {
                    System.err.println("Error al consultar el Monitor: " + e.getMessage());
                }
            }
        });
        monitorStatusThread.setDaemon(true);
        monitorStatusThread.start();
    }

    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(BALANCEADOR_PORT)) {
            System.out.println("Balanceador escuchando en el puerto " + BALANCEADOR_PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());
                new Thread(new ManejadorCliente(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ManejadorCliente implements Runnable {
        private Socket clientSocket;

        public ManejadorCliente(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (
                    PrintWriter outCliente = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                ServidorInfo servidorElegido = elegirServidorRoundRobin();
                if (servidorElegido == null) {
                    outCliente.println("ERROR: No hay servidores activos.");
                    return;
                }

                // Enviar la dirección del servidor al cliente
                outCliente.println(servidorElegido.getIp() + ":" + servidorElegido.getPuerto());
                System.out.println("Cliente asignado a servidor: " + servidorElegido.getIp() + ":" + servidorElegido.getPuerto());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private ServidorInfo elegirServidorRoundRobin() {
            try {
                Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                MonitorInterface monitor = (MonitorInterface) registry.lookup("MonitorService");
                List<ServidorInfo> servidores = monitor.obtenerEstadoServidores().stream()
                        .filter(ServidorInfo::isActivo)
                        .toList();

                if (servidores.isEmpty()) return null;

                int index = indiceServidor.getAndIncrement() % servidores.size();
                return servidores.get(index);
            } catch (Exception e) {
                System.err.println("Error al consultar el Monitor: " + e.getMessage());
                return null;
            }
        }

    }
}
