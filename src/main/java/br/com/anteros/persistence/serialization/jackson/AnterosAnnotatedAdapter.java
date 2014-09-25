package br.com.anteros.persistence.serialization.jackson;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.annotation.DiscriminatorColumn;
import br.com.anteros.persistence.metadata.annotation.DiscriminatorValue;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.session.SQLSessionFactory;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
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
		} else if (acls.equals(JsonManagedReference.class)) {
			if (annotated instanceof AnnotatedMember) {
				Member member = ((AnnotatedMember) annotated).getMember();
				if (member instanceof Field) {
					EntityCache entityCache = sessionFactory.getEntityCacheManager().getEntityCache(
							member.getDeclaringClass());
					if (entityCache != null) {
						DescriptionField descriptionField = entityCache.getDescriptionField(((Field) member).getName());
						if (descriptionField.isMappedBy()){
							System.out.println("mapped by "+member.getName()+" -> "+member.getDeclaringClass());
						}

					}
				}
			}
			return annotated.getAnnotation(acls);

		} else if (acls.equals(JsonBackReference.class)) {
			if (annotated instanceof AnnotatedMember) {
				Member member = ((AnnotatedMember) annotated).getMember();
				EntityCache entityCache = sessionFactory.getEntityCacheManager().getEntityCache(
						member.getDeclaringClass());
				if (entityCache != null) {
					DescriptionField descriptionField = entityCache.getDescriptionField(((Field) member).getName());
					if (descriptionField.isRelationShip()){
						System.out.println("foreignKey "+member.getName()+" -> "+member.getDeclaringClass());
					}
				}
			}
			return annotated.getAnnotation(acls);
		}
		return annotated.getAnnotation(acls);
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

	@Override
	public Annotated withAnnotations(AnnotationMap fallback) {
		return annotated.withAnnotations(fallback);
	}

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

	@Override
	protected AnnotationMap getAllAnnotations() {
		throw new RuntimeException("Not implemented method.");
	}

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

}
