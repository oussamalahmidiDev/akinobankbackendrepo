package com.akinobank.app.utilities;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;

import static org.jsoup.parser.Parser.unescapeEntities;

public class JsonHTMLEscape extends JsonDeserializer<String> implements ContextualDeserializer {

    public static final PolicyFactory POLICY_FACTORY =
        new HtmlPolicyBuilder()
            .allowUrlProtocols("https")
            .toFactory();

    @Override
    public String deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String value = parser.getValueAsString();
        if (StringUtils.isEmpty(value))
            return value;
        String unescaped = unescape(value);
        return unescapeEntities(POLICY_FACTORY.sanitize(unescaped), true);
    }

    private String unescape(String value) {
        String unescaped = unescapeEntities(value, true);
        if (!unescaped.equals(value))
            return unescape(value);
        else return unescaped;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        return this;
    }
}
