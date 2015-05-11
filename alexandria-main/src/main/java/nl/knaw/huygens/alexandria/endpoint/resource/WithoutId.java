package nl.knaw.huygens.alexandria.endpoint.resource;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = WithoutId.Validator.class)
public @interface WithoutId {
  String message() default "{nl.knaw.huygens.alexandria.endpoint.resource.WithoutId.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  class Validator implements ConstraintValidator<WithoutId, ResourcePrototype> {
    @Override
    public void initialize(WithoutId constraintAnnotation) {
      // nothing needed.
    }

    @Override
    public boolean isValid(ResourcePrototype prototype, ConstraintValidatorContext context) {
      return prototype == null || prototype.getId() == null;
    }
  }
}
