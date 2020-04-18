package com.akinobank.app.models.logs;

import com.akinobank.app.models.Admin;
import com.akinobank.app.models.Agent;
import com.akinobank.app.models.Client;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@Entity
public class Logs  implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String path;

    @OneToOne
    private Admin admin;

    @OneToOne
    private Agent agent;

    @OneToOne
    private Client client;

//    protected Logs(String name, String resourceBundleName) {
//        super(name, resourceBundleName);
//    }
//
//    protected Logs (String name) {
//        super(name);
//    }
//
//    @Override
//    public void info(Object message) {
//        super.info(message);
//        db.save(message);
//    }


        public void log(){
        Logger logger = Logger.getLogger("IdLog "+id);
        FileHandler fh;
        try {

            fh = new FileHandler(path);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}