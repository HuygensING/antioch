package nl.knaw.huygens.alexandria.textlocator;

import java.io.InputStream;

public interface AlexandriaTextLocator {

  void validate(InputStream textStream) throws TextLocatorValidationException;

}
