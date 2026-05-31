/*
 * Copyright 2026 Frans Jacobs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcs.commandStation.net;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.tinylog.Logger;

/**
 * Downloads locomotive function icons from the JMRI icon repository into the local JCS cache directory.<br>
 * The download is skipped when the icons are already present on disk.
 *
 * Idea is the same as for the Marklin and ESu locomotive Images download from the Command Station. Download is done on the Uses PCto avoid copyright issues when storing these images in Github
 *
 * <p>
 * Icons are stored in: {@code <user.home>/jcs/cache/functionicons}
 * </p>
 */
public class FunctionIconDownloader {

  /**
   * Base URL of the JMRI transparent-background function icon directory.
   */
  static final String ICON_BASE_URL = "https://www.jmri.org/resources/icons/functionicons/transparent_background/";
  @SuppressWarnings("unused")
  static final String ICON_BASE_URL2 = "https://github.com/JMRI/JMRI/tree/master/resources/icons/functionicons/transparent_background/";

  /**
   * Local cache directory for downloaded icons.
   */
  static final String CACHE_DIR
          = System.getProperty("user.home")
          + File.separator + "jcs"
          + File.separator + "cache"
          + File.separator + "functionicons";

  /**
   * HTTP connect / request timeout.
   */
  private static final Duration TIMEOUT = Duration.ofSeconds(30);

  /**
   * Matches href values that end with {@code .png} (case-insensitive) in a directory listing page, e.g.: {@code href="someIcon.png"}.
   */
  private static final Pattern PNG_HREF_PATTERN
          = Pattern.compile("href=\"([^\"]+\\.png)\"", Pattern.CASE_INSENSITIVE);

  private final HttpClient httpClient;
  private final Path cacheDirectory;

  /**
   * Creates a downloader using the default cache directory and a shared {@link HttpClient}.
   */
  public FunctionIconDownloader() {
    this(Path.of(CACHE_DIR));
  }

  /**
   * Creates a downloader with a custom cache directory. Intended for unit testing.
   *
   * @param cacheDirectory target directory where icons are stored
   */
  FunctionIconDownloader(Path cacheDirectory) {
    this.cacheDirectory = cacheDirectory;
    this.httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
  }

  /**
   * Returns {@code true} when the cache directory exists <em>and</em> contains at least one {@code .png} file,<br>
   * meaning a previous download has already completed.
   *
   * @return {@code true} if icons are already cached
   */
  public boolean areIconsCached() {
    if (!Files.isDirectory(cacheDirectory)) {
      return false;
    }
    File[] pngFiles = cacheDirectory.toFile().listFiles(
            (dir, name) -> name.toLowerCase().endsWith(".png"));
    return pngFiles != null && pngFiles.length > 0;
  }

  /**
   * Downloads all PNG icons from {@value #ICON_BASE_URL} into the local cache directory.<br>
   * If the icons are already cached this method returns immediately without making any network requests.
   *
   * @return the number of icons that were downloaded; 0 when the cache was already populated or the index page contained no PNG links
   * @throws IOException if the cache directory cannot be created or a file cannot be written
   * @throws InterruptedException if the calling thread is interrupted while waiting for an HTTP response
   */
  public int downloadIcons() throws IOException, InterruptedException {
    if (areIconsCached()) {
      Logger.info("Function icons already cached in {}; skipping download.", cacheDirectory);
      return 0;
    }

    ensureCacheDirectoryExists();

    List<String> iconFileNames = fetchIconFileNames();
    if (iconFileNames.isEmpty()) {
      Logger.warn("No PNG icons found at {}; nothing downloaded.", ICON_BASE_URL);
      return 0;
    }

    Logger.info("Downloading {} function icon(s) from {} ...", iconFileNames.size(), ICON_BASE_URL);

    int downloadCount = 0;
    for (String fileName : iconFileNames) {
      Path target = cacheDirectory.resolve(fileName);
      if (downloadIcon(fileName, target)) {
        downloadCount++;
      }
    }

    Logger.info("Downloaded {} function icon(s) to {}.", downloadCount, cacheDirectory);
    return downloadCount;
  }

  /**
   * Returns the list of PNG file names available on the remote index page.<br>
   * Only the bare file name (no path) is returned.
   *
   * @return unmodifiable list of icon file names; never {@code null}
   * @throws IOException on HTTP or I/O error
   * @throws InterruptedException if interrupted while waiting for the response
   */
  public List<String> fetchIconFileNames() throws IOException, InterruptedException {
    String html = fetchPageContent(ICON_BASE_URL);
    return parseIconFileNames(html);
  }

  /**
   * Returns the local {@link Path} to a cached icon, or an empty {@link java.util.Optional} when the icon has not been downloaded yet.
   *
   * @param fileName icon file name, e.g. {@code "F0.png"}
   * @return {@code Path} pointing to the cached file, or {@code null} if absent
   */
  public Path getCachedIcon(String fileName) {
    Path p = cacheDirectory.resolve(fileName);
    return Files.exists(p) ? p : null;
  }

  /**
   * Returns the resolved local cache directory path.
   *
   * @return
   */
  public Path getCacheDirectory() {
    return cacheDirectory;
  }

  /**
   * Creates {@link #cacheDirectory} including any missing parent directories.
   *
   * @throws IOException if the directory cannot be created
   */
  private void ensureCacheDirectoryExists() throws IOException {
    if (!Files.exists(cacheDirectory)) {
      Files.createDirectories(cacheDirectory);
      Logger.info("Created cache directory: {}", cacheDirectory);
    }
  }

  /**
   * Performs an HTTP GET for the given URL and returns the response body as a {@code String}.
   *
   * @param url target URL
   * @return response body text
   * @throws IOException on non-200 status or I/O error
   * @throws InterruptedException if interrupted
   */
  private String fetchPageContent(String url) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(TIMEOUT)
            .GET()
            .build();

    HttpResponse<String> response
            = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throw new IOException(
              "HTTP " + response.statusCode() + " fetching index page: " + url);
    }
    return response.body();
  }

  /**
   * Scans raw HTML for {@code href="*.png"} anchors and returns the plain file names. Relative sub-path references (containing {@code /}) are skipped so only flat file names in the current directory
   * are returned.
   *
   * @param html raw HTML of the directory index page
   * @return immutable list of PNG file names
   */
  List<String> parseIconFileNames(String html) {
    List<String> names = new ArrayList<>();
    Matcher m = PNG_HREF_PATTERN.matcher(html);
    while (m.find()) {
      String href = m.group(1);
      // Skip any links that point outside the current directory
      if (!href.contains("/")) {
        names.add(href);
      }
    }
    return Collections.unmodifiableList(names);
  }

  /**
   * Downloads a single icon file and saves it to {@code target}.
   *
   * @param fileName icon file name (used to build the download URL)
   * @param target local destination path
   * @return {@code true} on success, {@code false} on error (the error is logged but not rethrown so that remaining icons can still download)
   */
  private boolean downloadIcon(String fileName, Path target) {
    String iconUrl = ICON_BASE_URL + fileName;
    try {
      HttpRequest request = HttpRequest.newBuilder()
              .uri(URI.create(iconUrl))
              .timeout(TIMEOUT)
              .GET()
              .build();

      HttpResponse<InputStream> response
              = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

      if (response.statusCode() != 200) {
        Logger.warn("Skipping {} — HTTP {}", fileName, response.statusCode());
        return false;
      }

      try (InputStream in = response.body()) {
        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
      }

      Logger.debug("Downloaded: {}", fileName);
      return true;

    } catch (IOException | InterruptedException ex) {
      Logger.error(ex, "Failed to download icon: {}", iconUrl);
      if (ex instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      return false;
    }
  }

  public static void main(String[] a) {
    FunctionIconDownloader fid = new FunctionIconDownloader();
    try {
      fid.downloadIcons();
    } catch (IOException | InterruptedException ex) {
      Logger.error(ex);
    }
  }
}
