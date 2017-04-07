package ru.pioneersystem.pioneer2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@Scope(value="session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TestService {

    private String mess = "Сообщение";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private HttpSession httpSession;

    public TestService() {

    }

    public boolean getDbType() {
        boolean DB_MSSQL = true;
        try {
            Connection conn = dataSource.getConnection();
            String url = conn.getMetaData().getURL();
            conn.close();
            if (url.contains("sqlserver")) {
                DB_MSSQL = true;
            }
            else {
                DB_MSSQL = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return DB_MSSQL;
    }

    public void closeSession() {
//        httpSession.invalidate();
        String qqq = (String) httpSession.getAttribute("dada");
        httpSession.setAttribute("dada", "115");
    }

    public String getMess() {
        return mess;
    }

    public void setMess(String mess) {
        this.mess = mess;
    }
}
