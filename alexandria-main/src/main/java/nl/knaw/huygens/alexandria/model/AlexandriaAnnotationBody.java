package nl.knaw.huygens.alexandria.model;

import java.util.UUID;

public class AlexandriaAnnotationBody implements Accountable {
	private final UUID id;

	private final String type;

	private final String value;

	private final AlexandriaProvenance provenance;

	public AlexandriaAnnotationBody(UUID id, String type, String value, TentativeAlexandriaProvenance provenance) {
		this.id = id;
		this.type = type;
		this.value = value;
		this.provenance = provenance.bind(this);
	}

	public UUID getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	@Override
	public AlexandriaProvenance getProvenance() {
		return provenance;
	}

}
