package nl.knaw.huygens.alexandria.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public class AlexandriaResource implements Accountable {
	private final UUID id;

	private final Set<AlexandriaAnnotation> annotations = new HashSet<>();

	private String ref;

	private final AlexandriaProvenance provenance;

	public AlexandriaResource(UUID id, TentativeAlexandriaProvenance provenance) {
		this.id = id;
		this.provenance = provenance.bind(this);
	}

	public UUID getId() {
		return id;
	}

	public Set<AlexandriaAnnotation> getAnnotations() {
		return Collections.unmodifiableSet(annotations);
	}

	public Stream<AlexandriaAnnotation> streamAnnotations() {
		return annotations.stream();
	}

	public void addAnnotation(AlexandriaAnnotation annotation) {
		annotations.add(annotation);
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	@Override
	public AlexandriaProvenance getProvenance() {
		return provenance;
	}

}
