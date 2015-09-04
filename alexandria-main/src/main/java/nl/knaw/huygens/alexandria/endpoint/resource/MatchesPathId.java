package nl.knaw.huygens.alexandria.endpoint.resource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import javax.inject.Inject;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;

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
        return true; // Non-null validation, if desired, should be enforced via @NotNull
      }

      final UUIDParam prototypeId = prototype.getId();

      // Either no id was passed in prototype, or it MUST equal the id in URI
      return prototypeId == null || value(uriId()).equals(value(prototypeId));
    }

    private String value(PathSegment segment) {
      return segment.getPath();
    }

    private String value(UUIDParam uuid) {
      return String.valueOf(uuid.getValue());
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
  }

}
