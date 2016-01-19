package nl.knaw.huygens.alexandria.text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.google.inject.Singleton;

@Singleton
public class FileSystemTextService implements TextService {
  File textDir;
  // LoadingCache<UUID, Optional<String>> textCache = CacheBuilder.newBuilder().maximumSize(1000).build(new CacheLoader<UUID, Optional<String>>() {
  // @Override
  // public Optional<String> load(UUID uuid) {
  // return loadText(uuid);
  // }
  // });

  public FileSystemTextService(String textsDirectoryName) {
    this.textDir = new File(textsDirectoryName);
    if (!textDir.isDirectory()) {
      throw new RuntimeException("directory not found: " + textsDirectoryName);
    }
  }

  // @Override
  // public void set(UUID resourceUUID, String text) {
  // textCache.put(resourceUUID, Optional.of(text));
  // File textFile = textFile(resourceUUID);
  // try {
  // FileUtils.writeStringToFile(textFile, text);
  //
  // } catch (IOException e) {
  // throw new RuntimeException(e);
  // }
  // }

  @Override
  public void setFromStream(UUID resourceUUID, InputStream inputStream) {
    File textFile = textFile(resourceUUID);
    try {
      FileOutputStream outputStream = FileUtils.openOutputStream(textFile);
      IOUtils.copy(inputStream, outputStream);
      inputStream.close();
      outputStream.close();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // @Override
  // public Optional<String> get(UUID resourceUUID) {
  // try {
  // return textCache.get(resourceUUID);
  //
  // } catch (ExecutionException e) {
  // e.printStackTrace();
  // throw new RuntimeException(e);
  // }
  // }

  @Override
  public Optional<InputStream> getAsStream(UUID resourceUUID) {
    File textFile = textFile(resourceUUID);
    if (!textFile.isFile()) {
      return Optional.empty();
    }
    try {
      return Optional.ofNullable(FileUtils.openInputStream(textFile));
    } catch (IOException e) {
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
      throw new RuntimeException(e);
    }
  }

  private File textFile(UUID uuid) {
    String fileName = uuid + ".txt";
    File textFile = new File(textDir, fileName);
    return textFile;
  }

}
