package at.sparklingscience.urbantrees.cms;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

import at.sparklingscience.urbantrees.exception.CmsElementDeserializationException;

/**
 * Custom JSON type resolver for {@link CmsElement}s.
 * All classes in packages "at.sparklingscience.urbantrees.cms.layout"
 * and "at.sparklingscience.urbantrees.cms.component" that implement
 * {@link CmsElement} may be used to resolve dynamic JSON objects.
 * 
 * @author Laurenz Fiala
 * @since 2021/02/02
 */
public class CmsElementResolver extends TypeIdResolverBase {

	/**
	 * Holds all classes which are candidates for {@link CmsElement}.
	 */
	private static Set<Class<?>> elements;
	
	private JavaType baseType;
	
	static {
		
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		try {
			Resource[] resourceArray = resolver.getResources("classpath*:at/sparklingscience/urbantrees/cms/*/*.class");
			MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resolver);
			
			elements = Arrays.asList(resourceArray).stream()
					.filter(r -> {
						try {
							MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(r);
							return Arrays.asList(metadataReader.getClassMetadata().getInterfaceNames())
									.contains(CmsElement.class.getName());
						} catch (IOException e) {
							throw new RuntimeException("Could not load class metadata for CMS elements.", e);
						}
					})
					.map(r -> {
						try {
							MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(r);
							return Class.forName(metadataReader.getClassMetadata().getClassName());
						} catch (IOException e) {
							throw new RuntimeException("Could not load class metadata for CMS elements.", e);
						} catch (ClassNotFoundException e) {
							throw new RuntimeException("Could not find class for resource: " + r, e);
						}
					})
					.collect(Collectors.toSet());
		} catch (IOException e) {
			throw new RuntimeException("Could not load CMS element classes for dynamic JSON deserialization.", e);
		}
		
		
	}
	
	@Override
	public void init(JavaType baseType) {
		this.baseType = baseType;
	}
	
	@Override
	public String idFromValue(Object value) {
		return this.idFromValueAndType(value, value.getClass());
	}

	@Override
	public String idFromValueAndType(Object value, Class<?> suggestedType) {
		return suggestedType.getSimpleName();
	}
	
	@Override
	public JavaType typeFromId(DatabindContext context, String id) throws IOException {
		
		var classOptional = elements.stream()
			.filter(c -> c.getSimpleName().equals(id))
			.findFirst();
		
		if (!classOptional.isPresent()) {
			throw new CmsElementDeserializationException("Illegal CMS element found.");
		}
		
        return context.constructSpecializedType(this.baseType, classOptional.get());
		
	}

	@Override
	public Id getMechanism() {
		return Id.NAME;
	}

}
