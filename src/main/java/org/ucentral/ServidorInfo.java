package org.ucentral;

import java.io.Serializable;

public class ServidorInfo implements Serializable {

    private static final long serialVersionUID = 1L; // Se asegura compatibilidad entre versiones

    private String ip;
    private int puerto;
    private boolean activo;

    public ServidorInfo(String ip, int puerto, boolean activo) {
        this.ip = ip;
        this.puerto = puerto;
        this.activo = activo;
    }

    public String getIp() {
        return ip;
    }

    public int getPuerto() {
        return puerto;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return String.format("ServidorInfo[ip=%s, puerto=%d, activo=%b]", ip, puerto, activo);
    }
}
