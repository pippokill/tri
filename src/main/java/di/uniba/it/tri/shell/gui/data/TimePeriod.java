/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.shell.gui.data;

import java.util.Date;
import java.util.Objects;

/**
 *
 * @author pierpaolo
 */
public class TimePeriod {

    private String key;

    private Date start;

    private Date end;

    public TimePeriod(String key) {
        this.key = key;
        this.start = new Date(System.currentTimeMillis());
        this.end = new Date(System.currentTimeMillis());
    }

    public TimePeriod(String key, Date start, Date end) {
        this.key = key;
        this.start = start;
        this.end = end;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.key);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TimePeriod other = (TimePeriod) obj;
        if (!Objects.equals(this.key, other.key)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return key;
    }

    public String printToFile() {
        return key + "\t" + start.getTime() + "\t" + end.getTime();
    }

}
