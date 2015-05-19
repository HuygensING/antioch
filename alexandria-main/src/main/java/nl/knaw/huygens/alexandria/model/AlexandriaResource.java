package nl.knaw.huygens.alexandria.model;

import java.util.UUID;

public class AlexandriaResource extends AnnotatableObject {
	private final UUID id;

	private String ref;

	private final AlexandriaProvenance provenance;

	public AlexandriaResource(UUID id, TentativeAlexandriaProvenance provenance) {
		this.id = id;
		this.provenance = provenance.bind(this);
	}

	public UUID getId() {
		return id;
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
