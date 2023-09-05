package bisq.web.base;


import bisq.i18n.Res;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*")
public class SetupFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession(false);

        // Setup for logging
        final String sessionId = session == null ? null : session.getId();
        MDC.put("session-ID", sessionId);

        // Setup for translation (to translation into which language)
        Res.setLocale(req.getLocale());

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

}
