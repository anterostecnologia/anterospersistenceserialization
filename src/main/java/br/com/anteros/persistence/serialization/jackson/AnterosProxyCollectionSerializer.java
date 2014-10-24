package br.com.anteros.persistence.serialization.jackson;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;
import br.com.anteros.persistence.metadata.annotation.Fetch;
import br.com.anteros.persistence.metadata.annotation.type.FetchMode;
import br.com.anteros.persistence.metadata.annotation.type.FetchType;
import br.com.anteros.persistence.proxy.AnterosProxyLob;
import br.com.anteros.persistence.proxy.AnterosProxyObject;
import br.com.anteros.persistence.proxy.JavassistLazyLoadInterceptor;
import br.com.anteros.persistence.proxy.collection.AnterosPersistentCollection;
import br.com.anteros.persistence.proxy.lob.BlobLazyLoadProxy;
import br.com.anteros.persistence.proxy.lob.ClobLazyLoadProxy;
import br.com.anteros.persistence.proxy.lob.NClobLazyLoadProxy;
import br.com.anteros.persistence.serialization.jackson.AnterosPersistenceJacksonModule.Feature;
import br.com.anteros.persistence.session.SQLSessionFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

public class AnterosProxyCollectionSerializer extends JsonSerializer<Object> implements ContextualSerializer {
	protected final int _features;

	protected final JsonSerializer<Object> _serializer;
	protected final SQLSessionFactory _sessionFactory;

	@SuppressWarnings("unchecked")
	public AnterosProxyCollectionSerializer(JsonSerializer<?> serializer, int features, SQLSessionFactory sessionFactory) {
		_serializer = (JsonSerializer<Object>) serializer;
		_features = features;
		_sessionFactory = sessionFactory;
	}

	@Override
	public boolean isEmpty(Object value) {
		if (value == null) {
			return true;
		}
		if (AnterosProxyObject.class.isAssignableFrom(value.getClass())) {

		} else if (value instanceof AnterosPersistentCollection) {
			return findLazyValue((AnterosPersistentCollection) value) == null;
		}
		return false;
	}

	@Override
	public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonProcessingException {
		if (AnterosProxyObject.class.isAssignableFrom(value.getClass())) {
			Object proxiedValue;
			try {
				proxiedValue = findProxied(value);
			} catch (Exception e) {
				throw new JacksonSerializationException(e);
			}
			value = proxiedValue;
			if (value == null) {
				provider.defaultSerializeNull(jgen);
				return;
			}
		} else if (value instanceof AnterosPersistentCollection) {
			AnterosPersistentCollection coll = (AnterosPersistentCollection) value;
			if (!Feature.FORCE_LAZY_LOADING.enabledIn(_features) && !coll.isInitialized()) {
				provider.defaultSerializeNull(jgen);
				return;
			}
			value = coll;
			if (value == null) {
				provider.defaultSerializeNull(jgen);
				return;
			}
		}
		if (_serializer == null) {
			throw new JsonMappingException("PersistentCollection does not have serializer set");
		}
		_serializer.serialize(value, jgen, provider);
	}

	@Override
	public void serializeWithType(Object value, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer)
			throws IOException, JsonProcessingException {
		if (AnterosProxyObject.class.isAssignableFrom(value.getClass())) {
			Object proxiedValue;
			try {
				proxiedValue = findProxied(value);
			} catch (Exception e) {
				throw new JacksonSerializationException(e);
			}
			value = proxiedValue;
			if (value == null) {
				provider.defaultSerializeNull(jgen);
				return;
			}
		} else if (value instanceof AnterosPersistentCollection) {
			AnterosPersistentCollection coll = (AnterosPersistentCollection) value;
			if (!Feature.FORCE_LAZY_LOADING.enabledIn(_features) && !coll.isInitialized()) {
				provider.defaultSerializeNull(jgen);
				return;
			}
			value = coll;
			if (value == null) {
				provider.defaultSerializeNull(jgen);
				return;
			}
		}
		if (_serializer == null) {
			throw new JsonMappingException("PersistentCollection does not have serializer set");
		}
		_serializer.serializeWithType(value, jgen, provider, typeSer);
	}

	protected Object findLazyValue(AnterosPersistentCollection coll) {
		if (!Feature.FORCE_LAZY_LOADING.enabledIn(_features) && !coll.isInitialized()) {
			return null;
		}

		if (_sessionFactory != null) {
			coll.initialize();
		}

		return coll;
	}

	protected boolean usesLazyLoading(BeanProperty property) {
		if (property != null) {
			Fetch fetch = property.getAnnotation(Fetch.class);
			if (fetch != null) {
				if ((fetch.mode() == FetchMode.ELEMENT_COLLECTION) || (fetch.mode() == FetchMode.ONE_TO_MANY)
						|| (fetch.mode() == FetchMode.MANY_TO_MANY)) {
					return (fetch.type() == FetchType.LAZY);
				}
			}
			return !Feature.REQUIRE_EXPLICIT_LAZY_LOADING_MARKER.enabledIn(_features);
		}
		return false;
	}

	public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property)
			throws JsonMappingException {
		JsonSerializer<?> ser = provider.handlePrimaryContextualization(_serializer, property);
		if (!usesLazyLoading(property)) {
			return ser;
		}
		if (ser != _serializer) {
			return new AnterosProxyCollectionSerializer(ser, _features, _sessionFactory);
		}
		return this;
	}

	protected Object findProxied(Object value) throws Exception {
		if (value instanceof AnterosProxyObject) {
			if (!Feature.FORCE_LAZY_LOADING.enabledIn(_features)
					&& !((AnterosProxyObject) (value)).isInitialized()) {
				return null;
			}
			return ((AnterosProxyObject) (value)).initializeAndReturnObject();
		}
		
		return null;
	}
}