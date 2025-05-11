package cl.veridico.informes.utils;

import java.util.ArrayList;
import java.util.List;

public class ListaOError<T> {
    private List<T> data = new ArrayList<>();
    private String mensaje;

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public boolean isVacio() {
        return data.isEmpty() && mensaje != null;
    }

    public T primero() {
        return data != null && !data.isEmpty() ? data.get(0) : null;
    }
    

    public static <T> T obtenerPrimero(ListaOError<T> lista) {
        if (lista != null && lista.getData() != null && !lista.getData().isEmpty()) {
            return lista.getData().get(0);
        }
        return null;
    }
}
