package nl.knaw.huygens.alexandria.endpoint;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.base.Strings;
import nl.knaw.huygens.alexandria.endpoint.param.UUIDParam;
import nl.knaw.huygens.alexandria.exception.BadRequestException;
import nl.knaw.huygens.alexandria.service.AnnotationService;

public class AnnotationRequestValidator {
  public static final String MISSING_TYPE_MESSAGE = "Annotation MUST have a type";
  public static final String NO_SUCH_ANNOTATION_FORMAT = "No such annotation: %s";

  private final AnnotationService service;

  public static AnnotationRequestValidator servedBy(AnnotationService service) {
    return new AnnotationRequestValidator(service);
  }

  protected AnnotationRequestValidator(AnnotationService service) {
    this.service = checkNotNull(service, "AnnotationService must not be null");
  }

  public final void validate(AnnotationCreationRequest request) {
    if (request == null) {
      throw new BadRequestException("Missing required AnnotationCreationRequest body");
    }

    validateId(request);
    validateType(request);
    validateValue(request);
    validateCreatedOn(request);
    validateAnnotations(request);
  }

  protected void validateId(AnnotationCreationRequest request) {
    return; // missing or empty Id means client does not override server generated value.
  }

  protected void validateType(AnnotationCreationRequest request) {
    if (Strings.isNullOrEmpty(request.type)) {
      throw new BadRequestException(MISSING_TYPE_MESSAGE);
    }
  }

  protected void validateValue(AnnotationCreationRequest request) {
    return; // missing or empty value is ok (means annotation is a Tag)
  }

  protected void validateCreatedOn(AnnotationCreationRequest request) {
    return; // missing or empty createdOn means client does not override server set value.
  }

  protected void validateAnnotations(AnnotationCreationRequest request) {
    request.getAnnotations().ifPresent(annotations -> stream(annotations) //
        .map(UUIDParam::getValue) //
        .forEach(uuid -> Optional.ofNullable(service.readAnnotation(uuid)) //
            .orElseThrow(() -> new BadRequestException(format(NO_SUCH_ANNOTATION_FORMAT, uuid.toString())))));
  }

  protected <T> Stream<T> stream(Collection<T> collection) {
    return collection.parallelStream(); // protected in case subclass wants, e.g., annotations.stream() instead
  }

}
