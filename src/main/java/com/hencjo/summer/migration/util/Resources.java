package com.hencjo.summer.migration.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class Resources {
  public static URL getResource(String resourceName) {
    URL url = Resources.class.getClassLoader().getResource(resourceName);
    if (url == null) throw new IllegalArgumentException("resource " + resourceName + " not found.");
    return url;
  }
  
  public static String toString(URL url, Charset charset) throws IOException {
    try(FileInputStream stream = new FileInputStream(url.getFile());
    		FileChannel fc = stream.getChannel()) {
      MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
      return charset.decode(bb).toString();
    }
  }
}
