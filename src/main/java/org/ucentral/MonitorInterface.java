package org.ucentral;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface MonitorInterface extends Remote {
    List<ServidorInfo> obtenerEstadoServidores() throws RemoteException;
}
