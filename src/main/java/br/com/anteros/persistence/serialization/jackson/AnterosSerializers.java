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