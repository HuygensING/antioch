package nl.knaw.huygens.alexandria.gutenberg;

/*
 * #%L
 * alexandria-performance-tests
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFReader;
import org.apache.jena.rdf.model.impl.RDFDefaultErrorHandler;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */
public class Catalogue implements Iterable<Model>, Closeable {

  private final URL source;

  private InputStream stream = null;

  public static Catalogue cached(File cacheFile) throws IOException {
    if (!cacheFile.isFile()) {
      try (
        final InputStream in = new Catalogue().source.openStream();
        final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(cacheFile))
      ) {
        final byte[] buf = new byte[8192];
        while (true) {
          final int read = in.read(buf);
          if (read < 0) {
            break;
          }
          out.write(buf, 0, read);
        }

      }
    }

    return new Catalogue(cacheFile.toURI().toURL());
  }

  public Catalogue() throws MalformedURLException {
    this(new URL("https://www.gutenberg.org/cache/epub/feeds/rdf-files.tar.bz2"));
  }

  public Catalogue(URL source) {
    this.source = source;
  }

  @Override
  public Iterator<Model> iterator() {
    try {
      stream = source.openStream();
      final BZip2CompressorInputStream compressedStream = new BZip2CompressorInputStream(stream);
      final TarArchiveInputStream catalogArchiveStream = new TarArchiveInputStream(compressedStream);

      return new Iterator<Model>() {

        ArchiveEntry nextEntry = null;

        @Override
        public boolean hasNext() {
          try {
            if (nextEntry == null) {
              nextEntry = catalogArchiveStream.getNextEntry();
            }
            return (nextEntry != null);
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        }

        @Override
        public Model next() {
          final Model model = ModelFactory.createDefaultModel();

          final RDFReader modelReader = model.getReader();
          modelReader.setErrorHandler(new RDFDefaultErrorHandler() {
            @Override
            public void warning(Exception e) {
              // ignore warnings
            }
          });
          modelReader.read(model, new FilterInputStream(catalogArchiveStream) {

            @Override
            public void close() throws IOException {
              // no-op
            }
          }, null);

          nextEntry = null;
          return model;
        }
      };
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void close() throws IOException {
    if (stream != null) {
      stream.close();
      stream = null;
    }
  }
}
