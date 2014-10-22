package br.com.anteros.persistence.serialization.jackson;

import br.com.anteros.persistence.proxy.AnterosProxyObject;
import br.com.anteros.persistence.serialization.jackson.AnterosPersistenceJacksonModule.Feature;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.Serializers;

public class AnterosSerializers extends Serializers.Base
{
    protected boolean _forceLoading;
    
    public AnterosSerializers(int features) {
        _forceLoading = Feature.FORCE_LAZY_LOADING.enabledIn(features);
    }

    public JsonSerializer<?> findSerializer(SerializationConfig config,
            JavaType type, BeanDescription beanDesc)
    {
        Class<?> raw = type.getRawClass();
        if (AnterosProxyObject.class.isAssignableFrom(raw)) {
            return new AnterosProxySerializer(_forceLoading);
        }
        return null;
    }

	public void setForceLoading(boolean _forceLoading) {
		this._forceLoading = _forceLoading;
	}
}