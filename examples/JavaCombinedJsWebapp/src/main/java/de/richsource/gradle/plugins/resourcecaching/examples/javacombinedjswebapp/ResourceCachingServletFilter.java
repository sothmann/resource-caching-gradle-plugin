package de.richsource.gradle.plugins.resourcecaching.examples.javacombinedjswebapp;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ResourceCachingServletFilter implements Filter {

	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		if (httpServletRequest.getRequestURI().contains(".nocache.")) {
			httpServletResponse.setHeader("Cache-Control", "no-cache");
		} else if (httpServletRequest.getRequestURI().contains(".cache.")) {
			httpServletResponse.setHeader("Cache-Control", "max-age=2592000");
		}

		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {

	}
}