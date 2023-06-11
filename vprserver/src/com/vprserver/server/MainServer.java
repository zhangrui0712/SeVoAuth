package com.vprserver.server;

import com.vprserver.sql.MySQLconnect;

public class MainServer {
    public static void main(String[] args) {
    	MySQLconnect.creatDatabase();
		MySQLconnect.creatTable_IdP();
        Runnable run_IdP = new IdP_Run();
        Runnable run_RP = new RP_Run();
        Thread thread_IdP = new Thread(run_IdP);
        Thread thread_RP = new Thread(run_RP);
        thread_IdP.start();
        thread_RP.start();
    }
}

class IdP_Run implements Runnable {
    public void run() {
    	IdentityProvider IdP = new IdentityProvider();
		IdP.IdP_Run();
    }
}

class RP_Run implements Runnable {
    public void run() {
    	RelyingParty rp = new RelyingParty();
		rp.RP_Run();
    }
}
