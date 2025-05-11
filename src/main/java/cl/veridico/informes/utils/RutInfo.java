package cl.veridico.informes.utils;

public class RutInfo {
    private final int numero;
    private final String digitoVerificador;
    private final boolean esValido;

    public RutInfo(int numero, String digitoVerificador, boolean esValido) {
        this.numero = numero;
        this.digitoVerificador = digitoVerificador;
        this.esValido = esValido;
    }

    public int getRut() {
        return numero;
    }

    public String getDigitoVerificador() {
        return digitoVerificador;
    }

    public boolean isEsValido() {
        return esValido;
    }
}
