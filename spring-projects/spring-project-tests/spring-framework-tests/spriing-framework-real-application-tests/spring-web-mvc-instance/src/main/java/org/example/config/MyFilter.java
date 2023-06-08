package org.example.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.FormContentFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;

import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;

public class MyFilter extends OncePerRequestFilter {

    private FormContentFilter filter = new FormContentFilter();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String resolve = request.getParameter("enableFormContentResolve");
        if (resolve != null && resolve.equals("true")) {
            filter.doFilter(request, response, filterChain);
        } else {
            filterChain.doFilter(request, response);
        }


        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        documentBuilderFactory.setFeature(FEATURE_SECURE_PROCESSING,true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

    }
}
