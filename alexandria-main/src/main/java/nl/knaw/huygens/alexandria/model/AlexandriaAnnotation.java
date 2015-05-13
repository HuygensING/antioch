package nl.knaw.huygens.alexandria.model;

import java.util.UUID;

public class AlexandriaAnnotation implements Accountable {
	private final UUID id;

	private final AlexandriaAnnotationBody body;

	private final AlexandriaProvenance provenance;	

	public AlexandriaAnnotation(UUID id, AlexandriaAnnotationBody body, TentativeAlexandriaProvenance provenance) {
		this.id = id;
		this.body = body;
		this.provenance = provenance.bind(this);
	}

	public UUID getId() {
		return id;
	}

	public AlexandriaAnnotationBody getBody() {
		return body;
	}

	@Override
	public AlexandriaProvenance getProvenance() {
		return provenance;
	}

}
