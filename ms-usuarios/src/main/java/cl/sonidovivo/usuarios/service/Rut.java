package cl.sonidovivo.usuarios.service;

// RUT chileno: misma lógica que src/utilidades/validaciones.ts del frontend (dígito verificador módulo 11).
// El backend vuelve a validar porque cualquiera podría llamar a la API sin pasar por el formulario.
public final class Rut {

    private Rut() {
    }

    // "12.345.678-5" -> "123456785"
    public static String limpiar(String rut) {
        return rut == null ? "" : rut.replaceAll("[^0-9kK]", "").toUpperCase();
    }

    public static String calcularDigitoVerificador(String cuerpo) {
        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }
        int resto = 11 - (suma % 11);
        if (resto == 11) return "0";
        if (resto == 10) return "K";
        return String.valueOf(resto);
    }

    public static boolean esValido(String rut) {
        String limpio = limpiar(rut);
        if (limpio.length() < 8 || limpio.length() > 9) {
            return false;
        }
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        String dv = limpio.substring(limpio.length() - 1);
        return cuerpo.matches("[0-9]+") && calcularDigitoVerificador(cuerpo).equals(dv);
    }

    // "123456785" -> "12.345.678-5"
    public static String formatear(String rut) {
        String limpio = limpiar(rut);
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        String conPuntos = cuerpo.replaceAll("\\B(?=(\\d{3})+(?!\\d))", ".");
        return conPuntos + "-" + limpio.substring(limpio.length() - 1);
    }
}
