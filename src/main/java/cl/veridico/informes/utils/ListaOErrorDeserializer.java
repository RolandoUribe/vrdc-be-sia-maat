package cl.veridico.informes.utils;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import java.io.IOException;
import java.util.List;

public class ListaOErrorDeserializer<T> extends JsonDeserializer<ListaOError<T>> implements ContextualDeserializer {

    private JavaType valueType;

    public ListaOErrorDeserializer() {
    }

    public ListaOErrorDeserializer(JavaType valueType) {
        this.valueType = valueType;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        JavaType wrapperType = property.getType();
        JavaType contentType = wrapperType.containedType(0); // ListaOError<T>
        return new ListaOErrorDeserializer<>(contentType);
    }

    @Override
    public ListaOError<T> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectCodec codec = p.getCodec();
        JsonNode node = codec.readTree(p);
        ListaOError<T> wrapper = new ListaOError<>();

        if (node.isArray()) {
            ObjectMapper mapper = (ObjectMapper) codec;
            List<T> list = mapper.readValue(
                    node.traverse(codec),
                    mapper.getTypeFactory().constructCollectionType(List.class, valueType.getRawClass()));
            wrapper.setData(list);
        } else if (node.isObject()) {
            JsonNode msg = node.get("Mensaje");
            if (msg != null) {
                wrapper.setMensaje(msg.asText());
            }
        }

        return wrapper;
    }
}
