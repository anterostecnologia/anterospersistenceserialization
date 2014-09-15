package br.com.anteros.persistence.serialization.jackson;

import br.com.anteros.persistence.proxy.AnterosProxyObject;
import br.com.anteros.persistence.serialization.jackson.AnterosPersistenceJacksonModule.Feature;
import br.com.anteros.persistence.session.SQLSessionFactory;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.CollectionType;

public class AnterosSerializers extends Serializers.Base
{
    protected final boolean _forceLoading;
	private SQLSessionFactory sessionFactory;
    
    public AnterosSerializers(int features, SQLSessionFactory sessionFactory) {
    	this.sessionFactory  = sessionFactory;
        _forceLoading = Feature.FORCE_LAZY_LOADING.enabledIn(features);
    }
    
    @Override
    public JsonSerializer<?> findSerializer(SerializationConfig config,
            JavaType type, BeanDescription beanDesc)
    {
        Class<?> raw = type.getRawClass();
        if (AnterosProxyObject.class.isAssignableFrom(raw)) {
            return new AnterosProxySerializer(_forceLoading, sessionFactory);
        }
        return null;
    }
    
    @Override
    public JsonSerializer<?> findCollectionSerializer(SerializationConfig config, CollectionType type,
    		BeanDescription beanDesc, TypeSerializer elementTypeSerializer,
    		JsonSerializer<Object> elementValueSerializer) {
    	Class<?> raw = type.getRawClass();
        if (AnterosProxyObject.class.isAssignableFrom(raw)) {
            return new AnterosProxySerializer(_forceLoading, sessionFactory);
        }
        return null;
    }
}
