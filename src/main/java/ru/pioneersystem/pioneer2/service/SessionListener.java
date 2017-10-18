package ru.pioneersystem.pioneer2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.WebApplicationContextUtils;
import ru.pioneersystem.pioneer2.dao.EventDao;
import ru.pioneersystem.pioneer2.model.Event;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionListener extends RequestContextListener implements HttpSessionListener, ServletContextListener {
	private static final ConcurrentHashMap<String, HttpSession> sessions = new ConcurrentHashMap<>();
	public static final String USER_ID = "userId";
	public static final String COMPANY_ID = "companyId";
	public static final String IP_ADDRESS = "IPAddress";

	// TODO: 09.09.2017 Переделать через EventService
	@Autowired
	private EventDao eventDao;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		WebApplicationContextUtils
				.getRequiredWebApplicationContext(sce.getServletContext())
				.getAutowireCapableBeanFactory()
				.autowireBean(this);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		//
	}

	@Override
	public void requestInitialized(ServletRequestEvent requestEvent) {
		super.requestInitialized(requestEvent);

		HttpServletRequest request = (HttpServletRequest) requestEvent.getServletRequest();
		HttpSession session = request.getSession(false);
		if (session == null) {
			return;
		}

		String ipAddress = (String) session.getAttribute(SessionListener.IP_ADDRESS);
		if (ipAddress != null) {
			return;
		}

		ipAddress = request.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
			ipAddress = request.getRemoteAddr();
		}
		session.setAttribute(SessionListener.IP_ADDRESS, ipAddress);
	}

	@Override
	public void sessionCreated(HttpSessionEvent event) {
		HttpSession session = event.getSession();
		sessions.put(session.getId(), session);
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		HttpSession session = event.getSession();
		sessions.remove(session.getId());

		logSignOutEvent(session);
	}

	private void logSignOutEvent(HttpSession session) {
		int userId = session.getAttribute(USER_ID) != null ? (int) session.getAttribute(USER_ID) : 0;
		int companyId = session.getAttribute(COMPANY_ID) != null ? (int) session.getAttribute(COMPANY_ID) : 0;
		String ipAddress = session.getAttribute(IP_ADDRESS) != null ? (String) session.getAttribute(IP_ADDRESS) : "";

		Event signOutEvent = new Event(new Date(), userId, Event.Type.USER_SIGNED_OUT, 0, "IP: " + ipAddress, null);
		eventDao.create(signOutEvent, companyId);
	}

	public void invalidateUserSessions(int userId) {
		for (HttpSession session : getUserSessions(userId)) {
			session.invalidate();
		}
	}

	public void invalidateCompanySessions(int companyId) {
		for (HttpSession session : getCompanySessions(companyId)) {
			session.invalidate();
		}
	}

	public List<HttpSession> getUserSessions(int userId) {
		List<HttpSession> sessionsList = new ArrayList<>();
		for (HttpSession session : sessions.values()) {
			Object obj = session.getAttribute(USER_ID);
			if (obj != null && (int) obj == userId) {
				sessionsList.add(session);
			}
		}
		return sessionsList;
	}

	public List<HttpSession> getCompanySessions(int companyId) {
		List<HttpSession> sessionsList = new ArrayList<>();
		for (HttpSession session : sessions.values()) {
			Object obj = session.getAttribute(COMPANY_ID);
			if (obj != null && (int) obj == companyId) {
				sessionsList.add(session);
			}
		}
		return sessionsList;
	}

	public int getActiveSessionsCount() {
		return sessions.size();
	}
}