package com.ambergarden.samples.neo4j.repositories;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import javax.sql.DataSource;
import java.sql.Connection;

public class RemoteDataSource {

    private static DataSource ds = new ComboPooledDataSource();
    private static DataSource dshao=new ComboPooledDataSource();
    private static DataSource dsbbpay=new ComboPooledDataSource();
    private static ThreadLocal<Connection> tl;
    private static ThreadLocal<Connection>tlhao;
    private static ThreadLocal<Connection>tlbbpay;
    public static Session init() {
        JSch jsch = new JSch();
        Session session = null;
        try {
            session = jsch.getSession("tangwei", "10.1.5.62", 22);
            session.setPassword("MhxzKhl");
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPortForwardingL(3306, "10.0.16.152", 6606);
            session.connect();

        } catch (JSchException e) {
            e.printStackTrace();
        }
        return session;
    }
}
