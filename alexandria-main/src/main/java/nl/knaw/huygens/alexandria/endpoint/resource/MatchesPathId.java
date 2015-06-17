package nl.knaw.huygens.alexandria.endpoint.resource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.constraints.NotNull;
import javax.ws.rs.PathParam;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MatchesPathId.Validator.class)
public @interface MatchesPathId {
  String message() default "{nl.knaw.huygens.alexandria.endpoint.resource.MatchesPathId.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  // TODO fix this validator, it now only works once, then the paramId is set, the next @MatchesPathId doesn't make a new Validator, so paramId is not updated
  class Validator implements ConstraintValidator<MatchesPathId, ResourcePrototype> {
    final UUID paramId;

    public Validator(@NotNull @PathParam("uuid") UUIDParam uuidParam) {
      this.paramId = uuidParam.getValue();
    }

    @Override
    public void initialize(MatchesPathId constraintAnnotation) {
      // nothing needed.
      Log.trace("MatchesPathId.initialize");
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
