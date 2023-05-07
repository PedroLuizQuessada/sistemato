package com.quesssystems.sistemato.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Calendar;

@Service
public class DatabaseUtil {
    @Value("${sistemato.fusohorario.horas}")
    private Integer horasFuso;

    public Timestamp recuperarHoraAtualComFuso() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.add(Calendar.HOUR, horasFuso);

        return new Timestamp(cal.getTimeInMillis());
    }
}
