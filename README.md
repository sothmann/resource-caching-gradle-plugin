# Resource-Caching Gradle Plugin

The plugin allows to implement caching of resource files for web browsers like JavaScript and CSS files.
It aims to implement a technique called ["perfect caching"](http://www.gwtproject.org/doc/latest/DevGuideCompilingAndDebugging.html#perfect_caching) which Google uses for its framework GWT.
This plugin will generate hashes of the contents of (ideally) combined JavaScript files / CSS files and name these files according to the hash.
Additionally, it can inject the required script and link tags into your host page.


## Examples

Several example projects can be found in [/examples](https://github.com/sothmann/resource-caching-gradle-plugin/tree/master/examples).

## Plugin dependency

As this is not a core Gradle plugin, you have to ensure, that Gradle knows how to get the plugin. Do do this, add the following lines to your build.gradle:

	buildscript {
	  repositories {
		jcenter()
	  }
	  dependencies {
		classpath 'de.richsource.gradle.plugins:resource-caching-gradle-plugin:0.4'
	  }
	}

Next, apply the plugin.

	apply plugin: 'resource-caching'

## The Problem solved by this plugin

JavaScript applications - especially Single-Page-Applications - often consist of a lot of JavaScript files (sometimes several hundreds of JS files) and CSS files.

Using a web browser to access the app, every single JavaScript file is fetched using distinct requests.
Browsers support only a limited number of parallel connections/requests.
Older Internet Explorer versions were limited to only two concurrent connections.
Most modern browsers support six to eight concurrent connections, but this is still not enough to effectively fetch several hundreds of files.

Additionally, JavaScript front-ends can be several megabytes in size which need to be fetched from the server.
This results in wait times and high server load.

To reduce the wait time and server load, one could cache these files in the browser.

But how long should these files be cached?
And when they are cached and you deploy a new release of your application, users would have to delete their browser cache manually, which is not very practical.


## Introducing Perfect Caching

Some Google engineers came up with a simple but effective solution to this problem – an approach called ["Perfect Caching"][3].
It is implemented in Google’s web framework [GWT]. But even if you are not using GWT, the approach can be easily adopted for any JavaScript application.

This is how it works:

1. Combine all JavaScript files into one big JavaScript file.
2. Name the combined JS file using the pattern `<hashcode>.cache.js`
	- calculate the hash of the contents of this file
	- use a common hashing algorithm like `MD5` or `SHA1`
3. Instruct the browser to cache files matching the pattern `*.cache.*` as long as possible.
	- You can do this via Apache by adding an entry to the `.htaccess` file.
	- If your app is a Java application you can alternatively use a simple servlet filter.
4. Instruct the browser to *never* cache files matching the pattern `*.nocache.*`.
	- call the resource that includes your JavaScript file (your host page) accordingly, e.g. `index.nocache.html`

Whenever a JavaScript source file is changed, the file name of the combined JavaScript file will change, so the browser is forced to fetch the new file.
It is essential to prevent caching of the host page, as this page contains the URL to the combined JavaScript file.

The result of this approach is as follows:

- When you access the application for the first time, all your JavaScript code is fetched once with a single request.
- Subsequent access to the application leads to no additional requests. As the JavaScript is cached by the browser, the browser doesn't even have to ask the server if the file has been changed since last access.
- When a new release is deployed, the browser will fetch the new JavaScript code. Once. Again, subsequent access doesn't lead to requests to the server.


## Integration into a Gradle Build

First of all, use [Gradle JavaScript Plugin][1] to combine your JavaScript files into a single file, either by simply concatenating the files or by using the [RequireJS Optimizer][2].
If you are using [RequireJS] you just have to point the plugin to your root module and it will traverse the dependency tree and concatenate all the files in the correct order.

Then use Resource-Caching Gradle Plugin [as shown in this example][sample].


## Tasks provided by this plugin

The plugin provides two Gradle tasks: `HashFiles` and `GenerateHostPage`.

### HashFiles Task

The HashFiles task generates hashes of the contents of (ideally) combined JavaScript files / CSS files and names these files according to the hash.

Example:

	task hashJsFiles(type: de.richsource.gradle.plugins.resourcecaching.HashFiles) {
      source = combineJs
      dest = file("$buildDir/hashedJs")
    }

    task hashCssFiles(type: de.richsource.gradle.plugins.resourcecaching.HashFiles) {
      source = files('src/main/css/app.css')
      dest = file("$buildDir/hashedCss")
    }

The task has the following options:

- `source` - The files to hash
- `hashAlgorithm` - (String) The hash algorithm to use. Default to `SHA-1`
- `dest` - (File) the output directory for the hashed files

### GenerateHostPage Task

The GenerateHostPage task is a convenience task to inject the required script and link tags for the hashed files into your host page.
It replaces the token `@generatedScriptTags@` in the template file with the generated script tags and the token `@generatedCssTags@` with the generated link tags.

Example:

	task generateHostPage(type: de.richsource.gradle.plugins.resourcecaching.GenerateHostPage) {
	  source(hashJsFiles, hashCssFiles)
	  dest = file("$buildDir/index_html/index.html")
	  template = file('src/main/webapp/index.html.template')
	}

Here is an example template file:

	<html>
    <head>
      @generatedScriptTags@
      @generatedCssTags@
    </head>
    <body>
    </body>
    </html>

The task has the following options:

- `source` - The JavaScript and CSS files for which script and link tags should be generated
- `template` - (File) the template file to use
- `dest` - (File) the destination file


## Integration into Java Apps Using Servlet Filter

One possibility to instruct the browser to cache files matching the pattern `*.cache.*` and never cache file matching `*.nocache.*` is to use a servlet filter.
Such a servlet filter could look like this:

	public class ResourceCachingServletFilter implements Filter {
	  public void destroy() {}

	  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
		  throws IOException, ServletException {
		HttpServletRequest httpReq = (HttpServletRequest) req;
		HttpServletResponse httpResp = (HttpServletResponse) resp;

		if (httpReq.getRequestURI().contains(".nocache.")) {
		  httpResp.setHeader("Cache-Control", "no-cache");
		} else if (httpReq.getRequestURI().contains(".cache.")) {
		  httpResp.setHeader("Cache-Control", "max-age=2592000");
		}
		chain.doFilter(req, resp);
		}

		public void init(FilterConfig config) throws ServletException {}
	}

[1]: https://github.com/sothmann/resource-caching-gradle-plugin
[2]: http://requirejs.org/docs/optimization.html
[3]: http://www.gwtproject.org/doc/latest/DevGuideCompilingAndDebugging.html#perfect_caching
[RequireJS]: http://requirejs.org/
[sample]: https://github.com/sothmann/resource-caching-gradle-plugin/blob/master/examples/JavaCombinedJsWebapp/build.gradle
[GWT]: http://www.gwtproject.org/
