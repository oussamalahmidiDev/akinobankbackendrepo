package com.akinobank.app.models.logs;

import com.akinobank.app.models.Agent;

import javax.persistence.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@Entity
public class AgentLogs implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String path;

    @OneToOne
    private Agent agent;


    public void logAgent(){
        Logger logger = Logger.getLogger("AgentIdLog "+id);
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