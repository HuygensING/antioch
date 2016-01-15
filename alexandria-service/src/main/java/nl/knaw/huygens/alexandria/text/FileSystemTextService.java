package nl.knaw.huygens.alexandria.text;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Singleton;

@Singleton
public class FileSystemTextService implements TextService {
  File textDir;
  LoadingCache<UUID, Optional<String>> textCache = CacheBuilder.newBuilder().maximumSize(1000).build(new CacheLoader<UUID, Optional<String>>() {
    @Override
    public Optional<String> load(UUID uuid) {
      return loadText(uuid);
    }
  });

  public FileSystemTextService(String textsDirectoryName) {
    this.textDir = new File(textsDirectoryName);
    if (!textDir.isDirectory()) {
      throw new RuntimeException("directory not found: " + textsDirectoryName);
    }
  }

  @Override
  public void set(UUID resourceUUID, String text) {
    textCache.put(resourceUUID, Optional.of(text));
    File textFile = textFile(resourceUUID);
    try {
      FileUtils.writeStringToFile(textFile, text);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Optional<String> get(UUID resourceUUID) {
    try {
      return textCache.get(resourceUUID);

    } catch (ExecutionException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private Optional<String> loadText(UUID uuid) {
    File textFile = textFile(uuid);
    if (!textFile.isFile()) {
      return Optional.empty();
    }
    try {
      String text = FileUtils.readFileToString(textFile);
      return Optional.ofNullable(text);
    } catch (IOException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  private File textFile(UUID uuid) {
    String fileName = uuid + ".txt";
    File textFile = new File(textDir, fileName);
    return textFile;
  }

}
