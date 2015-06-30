package nl.knaw.huygens.alexandria.concordion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HuygensCommand {
  String name() default "";
  String htmlTag() default "code";

}
