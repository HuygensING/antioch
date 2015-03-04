package nl.knaw.huygens.alexandria;

import javax.ws.rs.core.Context;
import java.lang.reflect.Type;

import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;

public class SingletonContextProvider<T> extends SingletonTypeInjectableProvider<Context, T> {
  public SingletonContextProvider(Type t, T instance) {
    super(t, instance);
  }
}
