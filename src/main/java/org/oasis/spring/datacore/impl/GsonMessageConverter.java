package org.oasis.spring.datacore.impl;

import com.google.gson.Gson;
import org.oasis.spring.datacore.model.DCResource;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;

/**
 * User: schambon
 * Date: 1/3/14
 */
public class GsonMessageConverter
        extends AbstractHttpMessageConverter<Object>
        implements GenericHttpMessageConverter<Object> {

    Gson gson;

    public GsonMessageConverter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        return MediaType.APPLICATION_JSON.equals(mediaType);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return MediaType.APPLICATION_JSON.equals(mediaType);
    }

    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return gson.fromJson(new InputStreamReader(inputMessage.getBody()), type);
    }


    @Override
    protected boolean supports(Class<?> clazz) {

        return clazz.equals(DCResource.class);
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return gson.fromJson(new InputStreamReader(inputMessage.getBody()), clazz);
    }

    @Override
    protected void writeInternal(Object resource, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {

        OutputStreamWriter writer = new OutputStreamWriter(outputMessage.getBody());
        gson.toJson(resource, writer);
        writer.flush();

    }
}
