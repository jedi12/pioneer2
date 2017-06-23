package ru.pioneersystem.pioneer2.service;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@WebListener
@Component
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionListener implements HttpSessionListener {
	private static final ConcurrentHashMap<String, HttpSession> sessions = new ConcurrentHashMap<>();
	public static final String USER_ID = "userId";
	public static final String COMPANY_ID = "companyId";

	@Override
	public void sessionCreated(HttpSessionEvent event) {
		HttpSession session = event.getSession();
		sessions.put(session.getId(), event.getSession());
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		sessions.remove(event.getSession().getId());
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
			try {
				if (userId == (int) session.getAttribute(USER_ID)) {
					sessionsList.add(session);
				}
			} catch (Exception e) {
				continue;
			}
		}
		return sessionsList;
	}

	public List<HttpSession> getCompanySessions(int companyId) {
		List<HttpSession> sessionsList = new ArrayList<>();
		for (HttpSession session : sessions.values()) {
			try {
				if (companyId == (int) session.getAttribute(COMPANY_ID)) {
					sessionsList.add(session);
				}
			} catch (Exception e) {
				continue;
			}
		}
		return sessionsList;
	}
	
	public int getActiveSessionsCount() {
		return sessions.size();
	}
}