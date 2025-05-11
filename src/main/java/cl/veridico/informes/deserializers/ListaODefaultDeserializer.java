package cl.veridico.informes.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListaODefaultDeserializer<T> extends JsonDeserializer<List<T>> {
    private final Class<T> type;

    // Constructor sin argumentos requerido por Jackson
    protected ListaODefaultDeserializer(Class<T> type) {
        this.type = type;
    }

    @Override
    public List<T> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        List<T> result = new ArrayList<>();

        if (node.isArray()) {
            for (JsonNode item : node) {
                result.add(p.getCodec().treeToValue(item, type));
            }
        } else if (node.isObject() && node.has("Mensaje")) {
            return result;
        }

        return result;
    }
}
