/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package br.com.anteros.persistence.serialization.jackson;

import java.io.IOException;

import br.com.anteros.persistence.proxy.AnterosProxyObject;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;

public class AnterosProxySerializer extends JsonSerializer<AnterosProxyObject> {
	protected final BeanProperty _property;

	protected final boolean _forceLazyLoading;

	protected PropertySerializerMap _dynamicSerializers;

	public AnterosProxySerializer(boolean forceLazyLoading) {
		_forceLazyLoading = forceLazyLoading;
		_dynamicSerializers = PropertySerializerMap.emptyMap();
		_property = null;
	}

	@Override
	public boolean isEmpty(AnterosProxyObject value) {
		try {
			return (value == null) || (findProxied(value) == null);
		} catch (Exception e) {
			throw new JacksonSerializationException(e);
		}
	}

	@Override
	public void serialize(AnterosProxyObject value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		Object proxiedValue;
		try {
			proxiedValue = findProxied(value);
		} catch (Exception e) {
			throw new JacksonSerializationException(e);
		}
		if (proxiedValue == null) {
			provider.defaultSerializeNull(jgen);
			return;
		}
		findSerializer(provider, proxiedValue).serialize(proxiedValue, jgen, provider);
	}

	@Override
	public void serializeWithType(AnterosProxyObject value, JsonGenerator jgen, SerializerProvider provider,
			TypeSerializer typeSer) throws IOException, JsonProcessingException {
		Object proxiedValue;
		try {
			proxiedValue = findProxied(value);
		} catch (Exception e) {
			throw new JacksonSerializationException(e);
		}
		if (proxiedValue == null) {
			provider.defaultSerializeNull(jgen);
			return;
		}
		findSerializer(provider, proxiedValue).serializeWithType(proxiedValue, jgen, provider, typeSer);
	}

	protected JsonSerializer<Object> findSerializer(SerializerProvider provider, Object value) throws IOException,
			JsonProcessingException {
		Class<?> type = value.getClass();
		PropertySerializerMap.SerializerAndMapResult result = _dynamicSerializers.findAndAddPrimarySerializer(type,
				provider, _property);
		if (_dynamicSerializers != result.map) {
			_dynamicSerializers = result.map;
		}
		return result.serializer;
	}

	protected Object findProxied(AnterosProxyObject value) throws Exception {
		if (value instanceof AnterosProxyObject) {
			if (!_forceLazyLoading && !((AnterosProxyObject) (value)).isInitialized()) {
				return null;
			}
			return ((AnterosProxyObject) (value)).initializeAndReturnObject();
		}
		return null;
	}
}