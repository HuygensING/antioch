package nl.knaw.huygens.alexandria.service;

import java.util.Optional;
import java.util.UUID;

import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;

public class TinkerpopService implements AlexandriaService {

	@Override
	public boolean createOrUpdateResource(UUID uuid, String ref, TentativeAlexandriaProvenance provenance) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AlexandriaResource readResource(UUID uuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AlexandriaAnnotationBody createAnnotationBody(UUID uuid, Optional<String> type, String value, TentativeAlexandriaProvenance provenance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AlexandriaAnnotationBody readAnnotationBody(UUID uuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AlexandriaAnnotation annotate(AlexandriaResource resource, AlexandriaAnnotationBody annotationbody, TentativeAlexandriaProvenance provenance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AlexandriaAnnotation annotate(AlexandriaAnnotation annotation, AlexandriaAnnotationBody annotationbody, TentativeAlexandriaProvenance provenance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AlexandriaAnnotation readAnnotation(UUID uuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AlexandriaAnnotationBody findAnnotationBodyWithTypeAndValue(Optional<String> type, String value) {
		// TODO Auto-generated method stub
		return null;
	}

}
