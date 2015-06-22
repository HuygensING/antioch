package nl.knaw.huygens.alexandria.endpoint.resource;

import javax.inject.Inject;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.UUID;

import nl.knaw.huygens.Log;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MatchesPathId.Validator.class)
public @interface MatchesPathId {
  String message() default "{nl.knaw.huygens.alexandria.endpoint.resource.MatchesPathId.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  class Validator implements ConstraintValidator<MatchesPathId, ResourcePrototype> {
    final UriInfo uriInfo;

    @Inject
    public Validator(UriInfo uriInfo) {
      this.uriInfo = uriInfo;
    }

    @Override
    public void initialize(MatchesPathId constraintAnnotation) {
      Log.info("initialize: annotation={}", constraintAnnotation);
    }

    @Override
    public boolean isValid(ResourcePrototype prototype, ConstraintValidatorContext context) {
      if (prototype == null) {
        return true;
      }

      final PathSegment uriId = uriId();
      final UUID prototypeId = prototypeId(prototype);

      return value(uriId).equals(value(prototypeId));
    }

    private String value(PathSegment segment) {
      return segment.getPath();
    }

    private String value(UUID uuid) {
      return String.valueOf(uuid);
    }

    private PathSegment uriId() {
      return uuidSegment(uriSegments());
    }

    private PathSegment uuidSegment(List<PathSegment> pathSegments) {
      final int uuidSegmentIndex = uriSegments().size() - 1;
      return pathSegments.get(uuidSegmentIndex);
    }

    private List<PathSegment> uriSegments() {
      return uriInfo.getPathSegments();
    }

    private UUID prototypeId(ResourcePrototype prototype) {
      return prototype.getId().getValue();
    }

  }

}
