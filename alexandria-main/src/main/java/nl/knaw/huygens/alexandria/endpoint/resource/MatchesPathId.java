package nl.knaw.huygens.alexandria.endpoint.resource;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.constraints.NotNull;
import javax.ws.rs.PathParam;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

import nl.knaw.huygens.alexandria.endpoint.UUIDParam;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MatchesPathId.Validator.class)
public @interface MatchesPathId {
  String message() default "{nl.knaw.huygens.alexandria.endpoint.resource.MatchesPathId.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  class Validator implements ConstraintValidator<MatchesPathId, ResourcePrototype> {
    final UUID paramId;

    public Validator(@NotNull @PathParam("uuid") UUIDParam uuidParam) {
      this.paramId = uuidParam.getValue();
    }

    @Override
    public void initialize(MatchesPathId constraintAnnotation) {
      // nothing needed.
    }

    @Override
    public boolean isValid(ResourcePrototype prototype, ConstraintValidatorContext context) {
      if (prototype == null) {
        return true;
      }

      final UUID protoId = prototype.getId().getValue();
      return protoId.equals(paramId);
    }
  }
}
