package br.com.anteros.persistence.serialization.jackson;

import br.com.anteros.persistence.session.SQLSessionFactory;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;

public class AnterosSerializerModifier extends BeanSerializerModifier {
	protected int _features;
	protected final SQLSessionFactory _sessionFactory;

	public AnterosSerializerModifier(int features, SQLSessionFactory sessionFactory) {
		_features = features;
		_sessionFactory = sessionFactory;
	}

	@Override
	public JsonSerializer<?> modifyCollectionSerializer(SerializationConfig config, CollectionType valueType,
			BeanDescription beanDesc, JsonSerializer<?> serializer) {
		return new AnterosProxyCollectionSerializer(serializer, _features, _sessionFactory);
	}

	@Override
	public JsonSerializer<?> modifyMapSerializer(SerializationConfig config, MapType valueType,
			BeanDescription beanDesc, JsonSerializer<?> serializer) {
		return new AnterosProxyCollectionSerializer(serializer, _features, _sessionFactory);
	}


	public int getFeatures() {
		return _features;
	}

	public void setFeatures(int _features) {
		this._features = _features;
	}
}