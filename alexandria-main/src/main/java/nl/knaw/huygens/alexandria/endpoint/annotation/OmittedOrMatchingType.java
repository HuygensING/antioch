package nl.knaw.huygens.alexandria.endpoint.annotation;

import static nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationsEndpoint.annotationNotFoundForId;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Inject;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.ws.rs.PathParam;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OmittedOrMatchingType.Validator.class)
public @interface OmittedOrMatchingType {
  String message() default "{nl.knaw.huygens.alexandria.endpoint.annotation.OmittedOrMatchingType.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  class Validator implements ConstraintValidator<OmittedOrMatchingType, AnnotationPrototype> {
    private final String currentType;

    @Inject
    public Validator(AlexandriaService service, @PathParam("uuid") UUIDParam uuidParam) {
      currentType = service.readAnnotation(uuidParam.getValue()) //
          .map(AlexandriaAnnotation::getBody) //
          .map(AlexandriaAnnotationBody::getType) //
          .orElseThrow(annotationNotFoundForId(uuidParam));
    }

    @Override
    public void initialize(OmittedOrMatchingType constraintAnnotation) {
    }

    @Override
    public boolean isValid(AnnotationPrototype prototype, ConstraintValidatorContext context) {
      if (prototype == null) {
        return true; // Non-null validation, if desired, should be enforced via @NotNull
      }

      return prototype.getType().map(currentType::equals).orElse(true);
    }
  }
}
