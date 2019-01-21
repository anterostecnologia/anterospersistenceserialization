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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.annotation.DiscriminatorColumn;
import br.com.anteros.persistence.metadata.annotation.DiscriminatorValue;
import br.com.anteros.persistence.metadata.annotation.Entity;
import br.com.anteros.persistence.session.SQLSessionFactory;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;

public class AnterosAnnotatedAdapter extends Annotated {

	private Annotated annotated;
	private SQLSessionFactory sessionFactory;

	public AnterosAnnotatedAdapter(SQLSessionFactory sessionFactory, Annotated annotated) {
		this.annotated = annotated;
		this.sessionFactory = sessionFactory;
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> acls) {
		if (acls.equals(JsonTypeInfo.class)) {
			return processJsonTypeInfo(acls);
		}
		if (acls.equals(JsonSubTypes.class)) {
			return processJsonSubTypes(acls);
		}
		if (acls.equals(JsonIdentityInfo.class)) {
			return processJsonIdentityInfo(acls);
		}
		return annotated.getAnnotation(acls);
	}

	protected <A extends Annotation> A processJsonIdentityInfo(Class<A> acls) {
		if (annotated.getAnnotation(Entity.class) != null)
			return  (A) new JsonIdentityInfoImpl();
		else
			return null;
	}

	protected <A extends Annotation> A processJsonSubTypes(Class<A> acls) {
		DiscriminatorColumn discriminatorColumn = annotated.getAnnotation(DiscriminatorColumn.class);
		DiscriminatorValue discriminatorValue = annotated.getAnnotation(DiscriminatorValue.class);
		if ((discriminatorColumn != null) || (discriminatorValue != null)) {
			return (A) new JsonSubTypesImpl(discriminatorColumn, discriminatorValue);
		} else
			return annotated.getAnnotation(acls);
	}

	protected <A extends Annotation> A processJsonTypeInfo(Class<A> acls) {
		DiscriminatorColumn discriminatorColumn = annotated.getAnnotation(DiscriminatorColumn.class);
		DiscriminatorValue discriminatorValue = annotated.getAnnotation(DiscriminatorValue.class);
		if ((discriminatorColumn != null) || (discriminatorValue != null)) {
			return (A) new JsonTypeInfoImpl();
		} else
			return annotated.getAnnotation(acls);
	}

//	@Override
//	public Annotated withAnnotations(AnnotationMap fallback) {
//		return annotated.withAnnotations(fallback);
//	}

	@Override
	public AnnotatedElement getAnnotated() {
		return annotated.getAnnotated();
	}

	@Override
	protected int getModifiers() {
		return 0;
	}

	@Override
	public String getName() {
		return annotated.getName();
	}

	@Override
	public Type getGenericType() {
		return annotated.getGenericType();
	}

	@Override
	public Class<?> getRawType() {
		return annotated.getRawType();
	}

	@Override
	public Iterable<Annotation> annotations() {
		return annotated.annotations();
	}

//	@Override
//	protected AnnotationMap getAllAnnotations() {
//		throw new RuntimeException("Not implemented method.");
//	}

	class JsonTypeInfoImpl implements JsonTypeInfo {

		public Class<? extends Annotation> annotationType() {
			return JsonTypeInfo.class;
		}

		public Id use() {
			return JsonTypeInfo.Id.NAME;
		}

		public As include() {
			return JsonTypeInfo.As.PROPERTY;
		}

		public String property() {
			return "type";
		}

		public Class<?> defaultImpl() {
			return None.class;
		}

		public boolean visible() {
			return false;
		}

	}

	class JsonIdentityInfoImpl implements JsonIdentityInfo {

		public Class<? extends Annotation> annotationType() {
			return JsonIdentityInfo.class;
		}

		public String property() {
			return "@id";
		}

		public Class<? extends ObjectIdGenerator<?>> generator() {
			return ObjectIdGenerators.UUIDGenerator.class;
		}

		public Class<?> scope() {
			return Object.class;
		}

		@Override
		public Class<? extends ObjectIdResolver> resolver() {
			return SimpleObjectIdResolver.class;
		}

	}

	class JsonSubTypesImpl implements JsonSubTypes {

		private DiscriminatorColumn discriminatorColumn;
		private DiscriminatorValue discriminatorValue;

		public JsonSubTypesImpl(DiscriminatorColumn discriminatorColumn, DiscriminatorValue discriminatorValue) {
			this.discriminatorColumn = discriminatorColumn;
			this.discriminatorValue = discriminatorValue;
		}

		public Class<? extends Annotation> annotationType() {
			return JsonSubTypes.class;
		}

		public Type[] value() {
			List<Type> result = new ArrayList<Type>();
			Class<?> rawType = annotated.getRawType();
			if (discriminatorValue != null) {
				EntityCache entityCache = sessionFactory.getEntityCacheManager().getEntitySuperClass(rawType);
				rawType = entityCache.getEntityClass();
			}
			EntityCache[] entities = sessionFactory.getEntityCacheManager().getEntitiesBySuperClass(rawType);
			for (EntityCache entity : entities) {
				TypeImpl t = new TypeImpl(entity.getEntityClass(), entity.getDiscriminatorValue());
				result.add(t);
			}
			return result.toArray(new Type[] {});
		}

	}

	class TypeImpl implements JsonSubTypes.Type {

		private Class<?> _value;
		private String _name;

		public TypeImpl(Class<?> value, String name) {
			this._value = value;
			this._name = name;
		}

		public Class<? extends Annotation> annotationType() {
			return JsonSubTypes.Type.class;
		}

		public Class<?> value() {
			return _value;
		}

		public String name() {
			return _name;
		}

	}

	@Override
	public boolean hasAnnotation(Class<?> acls) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasOneOf(Class<? extends Annotation>[] annoClasses) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public JavaType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

}
